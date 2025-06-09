package com.shixin.util;

import com.rabbitmq.client.Channel;
import com.shixin.po.InboxMessage;
import com.shixin.po.InboxMessageStatus;
import com.shixin.repo.InboxMessageRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Slf4j
@Component
public class MessageListenerHandler {
    @Resource
    private InboxMessageRepo inboxMessageRepo;

    public void handleMessage(Message message, Channel channel, Consumer<Message> consumer) throws Exception {
        String messageId = message.getMessageProperties().getMessageId();
        if (messageId == null) {
            log.error("messageId is null，message:{}", message);
            consumer.accept(message);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        InboxMessage inboxMessage = inboxMessageRepo.findById(messageId).orElse(null);
        if (inboxMessage == null) {
            log.info("新消息：{}，插入本地事件表", message);
            inboxMessage = new InboxMessage();
            inboxMessage.setId(messageId);
            inboxMessage.setExchange(message.getMessageProperties().getReceivedExchange());
            inboxMessage.setRoutingKey(message.getMessageProperties().getReceivedRoutingKey());
            inboxMessage.setMessageBody(new String(message.getBody()));
            inboxMessage.setStatus(InboxMessageStatus.PENDING);
            inboxMessage.setCreateTime(LocalDateTime.now());
            inboxMessage.setUpdateTime(LocalDateTime.now());
            inboxMessageRepo.save(inboxMessage);
        } else if (inboxMessage.getStatus() == InboxMessageStatus.HANDLE_SUCCESS) {
            log.info("messageId:{} 已经处理成功，跳过", messageId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        log.info("messageId:{} 开始处理", messageId);
        consumer.accept(message);
        inboxMessage.setStatus(InboxMessageStatus.HANDLE_SUCCESS);
        inboxMessage.setUpdateTime(LocalDateTime.now());
        inboxMessageRepo.save(inboxMessage);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
