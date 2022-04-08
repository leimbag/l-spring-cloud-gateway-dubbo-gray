# 生命周期辅助

## 一、Dubbo

### 1.目的

为解决spring优雅停机时，Dubbo会直接调用jmv的hook关闭，导致RabbitMq在停机时会调用已注销的Dubbo服务，输出异常日志。

### 2.原理

1. 清除Dubbo自动注册shutdownhook的jvm钩子
2. 设置spring关闭时依然运行Dubbo
3. 待其他程序优雅关闭后，再手动关闭Dubbo

### 3.注意

暂时只支持Dubbo 3.0.5版本，后续Dubbo官方可能会调整优雅关机姿势，需要在新版从新测试

## 二、项目说明

```
    <module>rabbitmq-provider-demo</module>
    <module>rabbitmq-consumer-demo</module>
```

rabbitmq这两个项目，主要是为了模拟在使用rabbitmq，优雅停机时，dubbo会优先在消息处理完毕前关闭的问题，导致消息处理失败。

通过增加dubbo生命周期处理，解决提前关闭的问题，优先关闭rabbitmq消费者线程，再关闭dubbo，做到真正的优雅停机

