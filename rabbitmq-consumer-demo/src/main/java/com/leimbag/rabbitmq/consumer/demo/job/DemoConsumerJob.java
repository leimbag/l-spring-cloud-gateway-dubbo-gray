package com.leimbag.rabbitmq.consumer.demo.job;

import com.leimbag.rabbitmq.consumer.demo.constant.MessageConstant;
import com.leimbag.rabbitmq.consumer.demo.listener.DemoTestHandler;
import com.leimbag.rabbitmq.consumer.demo.util.CustomConditionalRejectingErrorHandler;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoConsumerJob {
    @Autowired
    @Qualifier("taskConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Autowired
    @Qualifier("taskAmqpAdmin")
    private AmqpAdmin amqpAdmin;

    @Autowired
    @Qualifier("taskExchange")
    private TopicExchange topicExchange;

    @Bean
    public SimpleMessageListenerContainer userRechargeProcessingContainer() {
        Queue queue = new Queue(MessageConstant.QUEUE_NAME_DEMO_TEST);
        amqpAdmin.declareQueue(queue);

        Binding binding = BindingBuilder.bind(queue).to(topicExchange).with(MessageConstant.ROUTING_KEY_DEMO_TEST);
        amqpAdmin.declareBinding(binding);

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setErrorHandler(new CustomConditionalRejectingErrorHandler());
        container.setConnectionFactory(connectionFactory);
        container.setQueues(queue);
        container.setDefaultRequeueRejected(false);
        container.setAutoStartup(true);
        container.setMessageListener(new MessageListenerAdapter(demoTestHandler()));
        return container;
    }


    @Bean
    public DemoTestHandler demoTestHandler() {
        return new DemoTestHandler();
    }
}
