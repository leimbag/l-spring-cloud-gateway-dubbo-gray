package com.leimbag.rabbitmq.consumer.demo.component.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;

import java.util.Map;

/**
 * 不启用的原因是因为，通过去除dubbo的jvm钩子，使用dubbo自定义的生命周期控制，已经支持预先销毁mq了，
 * <p>
 * 如果有其他需要提前销毁的，可以再增加其他的生命周期
 *
 * @author leimbag
 */
//@Component
public class RabbitMqLifecycle implements ApplicationContextAware, SmartLifecycle {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile boolean running = false;

    private ApplicationContext applicationContext;

    @Override
    public void start() {
        logger.info("容器启动后自定义的操作");
        running = true;
    }

    @Override
    public void stop() {
        logger.info("收到关闭容器的信号自定义的操作");
        Map<String, SimpleMessageListenerContainer> mqListenerContainerMap = applicationContext.getBeansOfType(SimpleMessageListenerContainer.class);
        for (Map.Entry<String, SimpleMessageListenerContainer> entry : mqListenerContainerMap.entrySet()) {
            logger.info("停止SimpleMessageListenerContainer容器名称: {}", entry.getKey());
            SimpleMessageListenerContainer container = entry.getValue();
            container.stop();
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        logger.info("检查LifeCycle运行状态:{}", running);
        return running;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
