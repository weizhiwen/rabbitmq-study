package com.shixin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

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
        return ExchangeBuilder.topicExchange("exchange.order").build();
    }

    @Bean
    public Queue queue() {
        return QueueBuilder.durable("queue.order").build();
    }

    @Bean
    public Binding binding(Queue queue, Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("key.order").noargs();
    }
}
