package com.shixin.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shixin.po.OutboxMessage;
import com.shixin.po.OutboxMessageStatus;
import com.shixin.repo.OutBoxMessageRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class RabbitMQSender {

    private static final ThreadLocal<List<OutboxMessage>> messageBuffer = ThreadLocal.withInitial(ArrayList::new);

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private OutBoxMessageRepo outBoxMessageRepo;
    @Resource
    private ObjectMapper objectMapper;


    public void send(RabbitMQMessage message) {
        // 插入本地消息表，即使暂时无法发送消息，也能通过定时任务再次发送
        log.info("插入本地消息表，消息内容：{}", message);
        OutboxMessage outboxMessage = buildOutboxMessage(message);
        outBoxMessageRepo.save(outboxMessage);
        send(outboxMessage);
    }

    public void send(OutboxMessage outboxMessage) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            List<OutboxMessage> messages = messageBuffer.get();
            // 只需要注册一次事务同步器，在事务提交或回滚时，统一发送消息
            if (CollectionUtils.isEmpty(messages)) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.info("事务提交成功，发送消息，，消息数量：{}", messages.size());
                        for (OutboxMessage outboxMessage : messages) {
                            doSend(outboxMessage);
                        }
                    }

                    @Override
                    public void afterCompletion(int status) {
                        log.info("事务状态：{}，清空线程本地消息缓存", status);
                        messageBuffer.remove();
                    }
                });
            }
            messages.add(outboxMessage);
        } else {
            log.info("没有事务，直接发送消息");
            doSend(outboxMessage);
        }
    }

    private void doSend(OutboxMessage outboxMessage) {
        CorrelationData correlationData = new CorrelationData(outboxMessage.getId());
        Message message = new Message(outboxMessage.getMessageBody().getBytes(), new MessageProperties());
        message.getMessageProperties().setMessageId(outboxMessage.getId());
        log.info("发送消息，消息内容：{}", message);
        rabbitTemplate.convertAndSend(outboxMessage.getExchange(), outboxMessage.getRoutingKey(), message, correlationData);
    }

    private OutboxMessage buildOutboxMessage(RabbitMQMessage message) {
        OutboxMessage outboxMessage = new OutboxMessage();
        outboxMessage.setId(UUID.randomUUID().toString());
        outboxMessage.setExchange(message.getExchange());
        outboxMessage.setRoutingKey(message.getRoutingKey());
        String messageBody;
        try {
            messageBody = objectMapper.writeValueAsString(message.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        outboxMessage.setMessageBody(messageBody);
        outboxMessage.setCreateTime(LocalDateTime.now());
        outboxMessage.setStatus(OutboxMessageStatus.SENT);
        outboxMessage.setUpdateTime(LocalDateTime.now());
        return outboxMessage;
    }
}
