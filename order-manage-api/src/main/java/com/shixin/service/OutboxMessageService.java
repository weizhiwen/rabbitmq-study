package com.shixin.service;

import com.shixin.po.OutboxMessage;
import com.shixin.po.OutboxMessageStatus;
import com.shixin.repo.OutBoxMessageRepo;
import com.shixin.util.RabbitMQSender;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OutboxMessageService {
    @Resource
    private OutBoxMessageRepo outboxMessageRepo;
    @Resource
    private RabbitMQSender rabbitMQSender;

    @Transactional
    public void retryMessages(String messageId) {
        OutboxMessage message = outboxMessageRepo.findById(messageId).orElse(null);
        if (message == null) {
            return;
        }
        message.setRetryCount(message.getRetryCount() + 1);
        message.setStatus(OutboxMessageStatus.SENT);
        message.setUpdateTime(LocalDateTime.now());
        rabbitMQSender.send(message);
    }
}
