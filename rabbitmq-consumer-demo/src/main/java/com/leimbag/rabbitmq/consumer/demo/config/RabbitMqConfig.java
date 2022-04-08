package com.leimbag.rabbitmq.consumer.demo.config;

import com.leimbag.rabbitmq.consumer.demo.constant.MessageConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author leimbag
 */
@Configuration
public class RabbitMqConfig {
    @Value("${rabbitmq.hostname}")
    private String hostname;

    @Value("${rabbitmq.port}")
    private int port;

    @Value("${rabbitmq.username}")
    private String username;

    @Value("${rabbitmq.addresses}")
    private String addresses;

    @Value("${rabbitmq.password}")
    private String password;



    @Bean
    @Primary
    public ConnectionFactory taskConnectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(addresses);
        if (StringUtils.isNotBlank(username)) {
            connectionFactory.setUsername(username);
        }
        if (StringUtils.isNotBlank(password)) {
            connectionFactory.setPassword(password);
        }
        connectionFactory.setVirtualHost(MessageConstant.VIRTUAL_HOST);
        return connectionFactory;
    }

    @Bean
    public AmqpTemplate taskAmqpTemplate() {
        return new RabbitTemplate(taskConnectionFactory());
    }

    @Bean
    public AmqpAdmin taskAmqpAdmin() {
        RabbitAdmin amqpAdmin = new RabbitAdmin(taskConnectionFactory());
        return amqpAdmin;
    }

    @Bean
    public TopicExchange taskExchange() {
        TopicExchange topicExchange = new TopicExchange(MessageConstant.EXCHANGE_NAME_DEFAULT);
        topicExchange.setShouldDeclare(true);
        return topicExchange;
    }
}
