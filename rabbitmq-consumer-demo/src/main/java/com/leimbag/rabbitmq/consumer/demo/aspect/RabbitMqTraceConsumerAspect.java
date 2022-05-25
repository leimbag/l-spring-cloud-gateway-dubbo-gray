package com.leimbag.rabbitmq.consumer.demo.aspect;

import com.leimbag.demo.core.util.LogHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author leimbag
 */
@Component
@Aspect
@Order(value = 110)
public class RabbitMqTraceConsumerAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    private void init() {
        logger.info("RabbitMq跟踪消费者切面初始化");
    }

    @Pointcut("execution(* org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter.onMessage(..))")
    public void pointcut() {
    }

    //    @Around("execution(* org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter.onMessage(..))")
    //    @Around("@annotation(com.leimbag.rabbitmq.consumer.demo.annotation.TraceableMq)")
    public Object handleMessage(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] paramValues = joinPoint.getArgs();

        logger.info("消息结构：{}", LogHelper.toJsonWithObject(paramValues));
        try {
            return joinPoint.proceed();
        } finally {
            // 清理信息
            MDC.clear();
        }
    }

}
