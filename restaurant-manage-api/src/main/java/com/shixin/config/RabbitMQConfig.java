package com.shixin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {
    public static final String RESTAURANT_ROUTING_KEY = "key.restaurant";
    public static final String RESTAURANT_EXCHANGE = "exchange.restaurant";
    public static final String RESTAURANT_QUEUE = "queue.restaurant";

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
    public RabbitListenerErrorHandler rabbitListenerErrorHandler() {
        return (amqpMessage, channel, message, exception) -> {
            log.error("消息处理失败，记录重试次数更新数据库，amqpMessage={}, channel={}, message={}", amqpMessage, channel, message);
            channel.basicReject(amqpMessage.getMessageProperties().getDeliveryTag(), false);
            return exception;
        };
    }

    @Bean
    public MessageRecoverer messageRecoverer() {
        return (message, cause) -> {
            log.info("消息处理重试多次仍然失败，放弃处理，message={}", message, cause);
        };
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);

        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息投递到交换机成功，correlationData={}", correlationData);
            } else {
                log.info("消息投递到交换机失败，correlationData={}，cause={}", correlationData, cause);
            }
        });

        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnsCallback((returned) -> {
            log.info("消息投递到队列失败，returned={}", returned);
        });

        return rabbitTemplate;
    }

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.topicExchange(RESTAURANT_EXCHANGE).build();
    }

    @Bean
    public Queue queue() {
        return QueueBuilder.durable(RESTAURANT_QUEUE).build();
    }

    @Bean
    public Binding binding(Queue queue, Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RESTAURANT_ROUTING_KEY).noargs();
    }
}
