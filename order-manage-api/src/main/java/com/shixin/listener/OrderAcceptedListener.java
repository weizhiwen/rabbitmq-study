package com.shixin.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OrderAcceptedListener {
    private static final Logger log = LoggerFactory.getLogger(OrderAcceptedListener.class);
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
