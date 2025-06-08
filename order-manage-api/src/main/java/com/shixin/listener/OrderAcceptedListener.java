package com.shixin.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OrderAcceptedListener {
    
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "queue.order")
    public void handleMessage(Message message, Channel channel) throws IOException {
        byte[] body = message.getBody();
        log.info("OrderCreatedListener handleMessage: {}", new String(body));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
