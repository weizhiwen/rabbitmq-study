package com.shixin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shixin.po.OutboxMessage;
import com.shixin.po.OutboxMessageStatus;
import com.shixin.repo.OutBoxMessageRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.time.LocalDateTime;

@Slf4j
@Configuration
public class RabbitMQConfig {
    public static final String ORDER_EXCHANGE = "exchange.order";
    public static final String ORDER_QUEUE = "queue.order";
    public static final String ORDER_ROUTING_KEY = "key.order";

    @Resource
    private OutBoxMessageRepo outBoxMessageRepo;
    @Resource
    private ObjectMapper objectMapper;

//    @Bean
//    public ConnectionFactory connectionFactory() {
//        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
//        connectionFactory.setHost("192.168.3.21");
//        connectionFactory.setPort(15672);
//        connectionFactory.setUsername("admin");
//        connectionFactory.setPassword("admin");
//        connectionFactory.setVirtualHost("/");
//        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
//        connectionFactory.setPublisherReturns(true);
//        return connectionFactory;
//    }

//    @Bean
//    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory);
//        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
//
//        factory.setPrefetchCount(1);
//        factory.setConcurrentConsumers(3);
//        factory.setMaxConcurrentConsumers(10);
//        return factory;
//    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);

//        // 支持发送方重试，通过定时任务重试
//        RetryTemplate retryTemplate = new RetryTemplate();
//        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
//        retryPolicy.setMaxAttempts(3);
//        retryTemplate.setRetryPolicy(retryPolicy);
//        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
//        backOffPolicy.setInitialInterval(1000);
//        backOffPolicy.setMultiplier(2.0);
//        backOffPolicy.setMaxInterval(5000);
//        retryTemplate.setBackOffPolicy(backOffPolicy);
//        retryTemplate.registerListener(new RetryListener() {
//            @Override
//            public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
//                log.info("开始重试open，context={}, callback={}, retryCount={}", context, callback, context.getRetryCount());
//                return RetryListener.super.open(context, callback);
//            }
//
//            @Override
//            public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
//                log.info("开始重试close，context={}, callback={}, retryCount={}", context, callback, context.getRetryCount());
//            }
//
//            @Override
//            public <T, E extends Throwable> void onSuccess(RetryContext context, RetryCallback<T, E> callback, T result) {
//                log.info("开始重试onSuccess，context={}, callback={}, retryCount={}", context, callback, context.getRetryCount());
//                RetryListener.super.onSuccess(context, callback, result);
//            }
//
//            @Override
//            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
//                log.info("开始重试onError，context={}, callback={}, retryCount={}", context, callback, context.getRetryCount());
//                RetryListener.super.onError(context, callback, throwable);
//            }
//        });
//        rabbitTemplate.setRetryTemplate(retryTemplate);

        // 开启消息确认回调
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            String messageId = correlationData != null ? correlationData.getId() : null;
            if (ack) {
                log.info("消息投递到交换机成功，correlationData={}", correlationData);
                // 将状态改为发送成功
                updateMessageStatus(messageId, OutboxMessageStatus.SEND_SUCCESS, null);
            } else {
                log.error("消息投递到交换机失败，correlationData={}，cause={}", correlationData, cause);
                // 将状态改为发送失败
                String error = "消息投递到交换机失败：" + cause;
                updateMessageStatus(messageId, OutboxMessageStatus.SEND_FAILED, error);
            }
        });

        rabbitTemplate.setReturnsCallback((returned) -> {
            log.error("消息投递到队列失败，returned={}", returned);
            // 将状态改为发送失败
            String messageId = returned.getMessage().getMessageProperties().getMessageId();
            String error = "消息投递到队列失败：" + returned.getReplyCode() + "," + returned.getReplyText();
            updateMessageStatus(messageId, OutboxMessageStatus.SEND_FAILED, error);
        });

        return rabbitTemplate;
    }

    private void updateMessageStatus(String messageId, OutboxMessageStatus status, String error) {
        if (messageId == null) {
            return;
        }
        OutboxMessage exist = outBoxMessageRepo.findById(messageId).orElse(null);
        if (exist == null || exist.getStatus() != OutboxMessageStatus.SENT) {
            return;
        }
        exist.setCreateTime(LocalDateTime.now());
        exist.setStatus(status);
        exist.setError(error);
        outBoxMessageRepo.save(exist);
    }

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.topicExchange(ORDER_EXCHANGE).build();
    }

    @Bean
    public Queue queue() {
        return QueueBuilder.durable(ORDER_QUEUE).build();
    }

    @Bean
    public Binding binding(Queue queue, Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ORDER_ROUTING_KEY).noargs();
    }
}
