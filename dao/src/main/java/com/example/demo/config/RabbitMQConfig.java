package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 注册相关队列
    public static final String REGISTRATION_QUEUE = "user.registration.queue";
    public static final String EMAIL_VERIFICATION_QUEUE = "email.verification.queue";

    // 交换机
    public static final String USER_EXCHANGE = "user.exchange";

    // 路由键
    public static final String REGISTRATION_ROUTING_KEY = "user.registration";
    public static final String EMAIL_VERIFICATION_ROUTING_KEY = "email.verification";

    @Bean
    public Queue registrationQueue() {
        return new Queue(REGISTRATION_QUEUE, true);
    }

    @Bean
    public Queue emailVerificationQueue() {
        return new Queue(EMAIL_VERIFICATION_QUEUE, true);
    }

    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(USER_EXCHANGE);
    }

    @Bean
    public Binding registrationBinding(Queue registrationQueue, DirectExchange userExchange) {
        return BindingBuilder
                .bind(registrationQueue)
                .to(userExchange)
                .with(REGISTRATION_ROUTING_KEY);
    }

    @Bean
    public Binding emailVerificationBinding(Queue emailVerificationQueue, DirectExchange userExchange) {
        return BindingBuilder
                .bind(emailVerificationQueue)
                .to(userExchange)
                .with(EMAIL_VERIFICATION_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}