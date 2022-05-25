# 消息队列消费者消息跟踪

## 一、利用Spring的MessageListenerAdapter作为切面点实现

注意：必须使用@Bean的方式创建MessageListenerAdapter的bean，才能做到切面，直接在Container中new不可以切到

```
@Around("execution(* org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter.onMessage(..))")
```

切面内部可以获取消息头，设置到MDC上下文实现跟踪以及执行完后的MDC消除

## 二、利用注解切面切具体handleMessage方法

1. 创建TraceMessageConverter类，继承SimpleMessageConverter类，内部实现消息头解析

```
    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        logger.info("接收消息：{}", LogHelper.toJsonWithObject(message));
        MessageProperties properties = message.getMessageProperties();
        if (properties != null) {
            String gid = properties.getHeader(GatewayConstant.GID);
            if (StringUtils.isNotBlank(gid)) {
                logger.info("接收gid={}的消息", gid);
                MDC.put(GatewayConstant.GID, gid);
            }
        }
        return super.fromMessage(message);
    }
```

2. 创建 @TraceableMq 注解， 使用切面方法内部实现MDC.clear()
```
@Around("@annotation(com.leimbag.rabbitmq.consumer.demo.annotation.TraceableMq)")
```

## 三、利用自定义接口做切面，接口定义方法 handleMessage

1. 创建 TraceableMessageHandler 接口，增加handleMessage方法

2. 所有消息处理类继承 TraceableMessageHandler 接口

3. 创建TraceMessageConverter类，继承SimpleMessageConverter类，内部实现消息头解析

4. 利用切面实现 实现类似 @TraceableMq 注解 处理方式，解决 MDC.clear() 问题




