package com.example.demo.config;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String CACHE_RETRY_QUEUE = "cache_retry_queue";
    public static final String CACHE_RETRY_EXCHANGE = "cache_retry_exchange";

    @Bean
    public Queue cacheRetryQueue() {
        return new Queue(CACHE_RETRY_QUEUE,true);
    }
    @Bean
    public DirectExchange cacheRetryExchange() {
        return new DirectExchange(CACHE_RETRY_EXCHANGE,true,false);
    }

}
