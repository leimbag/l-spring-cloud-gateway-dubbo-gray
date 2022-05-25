package com.leimbag.rabbitmq.consumer.demo.listener;

import com.leimbag.dubbo.user.service.UserService;
import com.leimbag.rabbitmq.consumer.demo.annotation.TraceableMq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @author leimbag
 */
public class DemoTestHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected UserService userService;

    @TraceableMq
    public void handleMessage(String message) {
        logger.info("开始处理消息:{}", message);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("message:{}, 调用user服务: {}", message, userService.getUserName(System.currentTimeMillis()));
    }
}
