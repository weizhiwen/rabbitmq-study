package com.shixin.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCreatedListener {

    @RabbitListener(queues = "queue.restaurant", errorHandler = "rabbitListenerErrorHandler")
    public void handleMessage(Message message) {
//        try {
        byte[] body = message.getBody();
        log.info("OrderCreatedListener handleMessage: {}", new String(body));
        // 业务逻辑
        int i = 1 / 0; // 触发异常，测试消息是否会被重新消费
//        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//            OrderMessageDTO messageDTO = objectMapper.readValue(body, OrderMessageDTO.class);
//            messageDTO.setStatus(OrderStatus.RESTAURANT_ACCEPTED);
//            messageDTO.setConfirm(true);
//            rabbitTemplate.convertAndSend("exchange.order","key.order", messageDTO);
//        } catch (Exception e) {
//            log.error("OrderCreatedListener handleMessage error: ", e);
//            MessageProperties messageProperties = message.getMessageProperties();
//            if (messageProperties.isRedelivered()) {
//                log.error("OrderCreatedListener handleMessage redelivered: {}, 丢弃消息", messageProperties.getRedelivered());
//                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
//            } else {
//                log.error("OrderCreatedListener handleMessage nack: {}，尝试消息再次消费", messageProperties.getDeliveryTag());
//                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
//            }
//        }
    }
}
