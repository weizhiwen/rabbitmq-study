package com.shixin.schedule;

import com.shixin.po.OutboxMessage;
import com.shixin.po.OutboxMessageStatus;
import com.shixin.repo.OutBoxMessageRepo;
import com.shixin.util.RabbitMQSender;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OutBoxMessageSentScheduledTask {

    @Resource
    private OutBoxMessageRepo outBoxMessageRepo;
    @Resource
    private RabbitMQSender rabbitMQSender;

    @Scheduled(cron = "0/5 * * * * *")
    public void outboxMessageSendScheduledTask() {
        List<OutboxMessage> list = outBoxMessageRepo.findByStatus(OutboxMessageStatus.SENT);
        log.info("定时任务发送待发送的消息数量：{}", list.size());
        for (OutboxMessage message : list) {
            outBoxMessageRepo.save(message);
            rabbitMQSender.send(message);
        }
    }
}