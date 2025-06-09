package com.shixin.controller;

import com.shixin.service.OutboxMessageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/outboxMessage")
public class OutboxMessageController {
    @Resource
    private OutboxMessageService outboxMessageService;

    @PostMapping("/retrySend/{id}")
    public void retrySend(@PathVariable("id") String id) {
        outboxMessageService.retryMessages(id);
    }
}
