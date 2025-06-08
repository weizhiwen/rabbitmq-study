package com.shixin.service;

import com.shixin.dto.OrderMessageDTO;
import com.shixin.po.Order;
import com.shixin.po.OrderStatus;
import com.shixin.repo.OrderRepo;
import com.shixin.util.RabbitMQMessage;
import com.shixin.util.RabbitMQSender;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    @Resource
    private OrderRepo orderRepo;
    @Resource
    private RabbitMQSender rabbitMQSender;

    @Transactional
    public Order createOrder(Order order) {
        order.setStatus(OrderStatus.CREATED);
        Order save = orderRepo.save(order);

        OrderMessageDTO message = new OrderMessageDTO();
        message.setId(save.getId());
        message.setAccountId(save.getAccountId());
        message.setAddress(save.getAddress());
        message.setRestaurantId(save.getRestaurantId());
        message.setPrice(save.getPrice());
        message.setConfirm(false);
        rabbitMQSender.send(new RabbitMQMessage("exchange.restaurant", "key.restaurant", message));
        rabbitMQSender.send(new RabbitMQMessage("exchange.restaurant", "key.restaurant", message));
        rabbitMQSender.send(new RabbitMQMessage("exchange.restaurant", "key.restaurant", message));

        // 注册事务提交后的回调，事务提交成功后才发送消息
//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
//            @Override
//            public void afterCommit() {
//                // 事务提交成功后才发送消息
//                log.error("事务提交，订单消息已发送");
//            }
//
//            @Override
//            public void afterCompletion(int status) {
//                if (status == TransactionSynchronization.STATUS_COMMITTED) {
//                    log.error("事务提交成功后才发送消息");
//                    rabbitTemplate.convertAndSend("exchange.restaurant", "key.restaurant", message);
//                } else if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
//                    log.error("事务回滚，订单消息未发送");
//                }
//            }
//        });

//        int i = 1 / 0; // 模拟异常
        return save;
    }
}
