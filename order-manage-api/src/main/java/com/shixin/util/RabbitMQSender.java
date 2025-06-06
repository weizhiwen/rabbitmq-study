package com.shixin.util;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class RabbitMQSender {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQSender.class);

    private static final ThreadLocal<List<RabbitMQMessage>> messageBuffer = ThreadLocal.withInitial(ArrayList::new);

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void send(RabbitMQMessage message) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            List<RabbitMQMessage> messages = messageBuffer.get();
            // 只需要注册一次事务同步器，在事务提交或回滚时，统一发送消息
            if (CollectionUtils.isEmpty(messages)) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        List<RabbitMQMessage> messages = messageBuffer.get();
                        try {
                            log.info("事务结束，消息数量：{}，消息内容：{}", messages.size(), messages);
                            if (status == STATUS_COMMITTED) {
                                log.info("事务提交成功，发送消息");
                                for (RabbitMQMessage m : messageBuffer.get()) {
                                    rabbitTemplate.convertAndSend(m.getExchange(), m.getRoutingKey(), m.getMessage());
                                }
                            } else {
                                log.error("发生异常，事务回滚，消息未发送");
                            }
                        } finally {
                            log.info("事务状态：{}，清空线程本地消息缓存", status);
                            messageBuffer.remove();
                        }
                    }
                });
            }

            messages.add(message);
        } else {
            log.info("没有事务，直接发送消息");
            rabbitTemplate.convertAndSend(message.getExchange(), message.getRoutingKey(), message.getMessage());
        }
    }
}
