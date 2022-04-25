# local-dev-spring-boot-starter

本地开发环境插件

## 一、Dubbo本地开发环境自动监测

### 1.目的

由于本地开发的dubbo服务会自动注册到开发环境，影响正常服务调用。通过检测本地环境信息，自动取消本地开发的服务注册。

### 2.原理

Dubbo服务暴露注册会依赖URL的dubbo.registry.register参数，通过配置方式可以禁止注册

本地dubbo服务 不注册，只订阅，通过以下配置：
```
dubbo.registry.register=false
dubbo.registry.subscribe=true
```

* 实现方式1：

通过org.springframework.boot.env.EnvironmentPostProcessor接ConfigFileApplicationListener口，扩展属性配置，并在resources目录下新建META-INF/spring.factories文件

spring.factories文件内容配置接口实现类，其中ApplicationListener配置仅仅为了回放日志而配置

```
org.springframework.boot.env.EnvironmentPostProcessor=com.leimbag.dubbo.user.processor.DubboLocalDevEnvironmentPostProcessor
org.springframework.context.ApplicationListener=com.leimbag.dubbo.user.processor.DubboLocalDevEnvironmentPostProcessor
```


* 实现方式2：

通过ApplicationListener<ApplicationEnvironmentPreparedEvent>事件监听方式，维护扩展属性，在resources目录下新建META-INF/spring.factories文件中增加配置

备注：由于ApplicationListener的Ordered执行顺序问题，可能会导致加载的配置文件不完全，一般情况下建议使用方式1(EnvironmentPostProcessor)

```
org.springframework.context.ApplicationListener=com.leimbag.dubbo.user.listener.DubboLocalDevEnvListener
```

**原理**

1. 读取本地系统配置后，检查是否有本地覆盖属性
2. 监测是否是本地开发，目前使用IP段方式
3. 针对本地开发，自动注入dubbo.registry.register=false属性配置
4. 若有覆盖值，优先使用本地覆盖值

具体逻辑可参看DubboUtil

### 3.其他

覆盖自动配置方式

开发人员如果需要强制注册本地服务到注册中心，可以通过在对应项目的本地的bootstrap.properties文件增加如下配置，即可实现注册服务，

```
dubbo.registry.register=true
```

### 4.注意

(1) 由于环境属性执行阶段在读取Nacos配置中心配置之前，导致配置中心配置无法生效，可以通过手工强制读取Nacos配置，具体见NacosHelper

未开启Nacos配置读取，是因为连接Nacos配置中心时间较久，影响启动速度，所以未开启

(2) EnvironmentPostProcessor 不能直接用 Slf4j 进行输出日志，改为使用DeferredLog回放日志

原因：ApplicationEnvironmentPreparedEvent事件监听顺序，先ConfigFileApplicationListener，后LoggingApplicationListener

因为本身ConfigFileApplicationListener执行时会去spring.factories扫描所有的EnvironmentPostProcessor，并执行postProcessEnvironment方法；

此时的LoggingApplicationListener还没有被执行，日志为初始化，因此无法使用

### 5.关闭本地dubbo开发环境监测配置插件

在 bootstrap.properties 文件中，增加以下配置，将直接关闭 本地dubbo开发环境监测

```
local.dev.dubbo.enable=false
```


