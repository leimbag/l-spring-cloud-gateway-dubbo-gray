## 网关服务

### 主要功能

#### 一、请求日志

记录所有请求日志及响应时间

#### 二、限流控制

1. 基于参数限流
2. 基于路径限流
3. 基于ip限流

后期可自定义限流规则发布，并可结合sentinel进行动态限流控制

#### 三、灰度发布

支持灰度路由调度，配置gray-dubbo-core组合使用，实现全链路灰度控制

**灰度发布说明**

1. web类项目

需要灰度发布的web项目，增加依赖
```
    <dependency>
        <groupId>com.leimbag.gray</groupId>
        <artifactId>gateway-web-spring-boot-starter</artifactId>
        <classifier>${branch}</classifier>
    </dependency>
```

开启灰度配置开关
```
web.gray.interceptor.enable=true
```

动态指定发布的版本，启动参数VM Options增加
```
-Dspring.cloud.nacos.discovery.metadata.version=gray
```

gateway-web-spring-boot-starter 项目主要是为dubbo服务项目增加灰度标识

2. dubbo服务项目

需要灰度发布的dubbo服务provider项目，增加依赖
```
    <dependency>
        <groupId>com.leimbag.gray</groupId>
        <artifactId>dubbo-core</artifactId>
        <classifier>${branch}</classifier>
    </dependency>
```

同时在vm启动动态参数增加 dubbo.provider.tag 参数
```
java -jar xxx-provider.jar -Ddubbo.provider.tag={the tag you want, may come from OS ENV}
```

dubbo上下文传递灰度标识，通过TagRouter路由器实现灰度路由。

目前路由策略是有灰度路由的服务provider，优先路由到灰度的provider，否则调用服务降级，采用robin轮训方式路由调用普通服务。

#### 四、强制使用灰度服务

1. web端配置

```
web.gray.forceUseTag=true
```

web端会强制调用的服务使用灰度服务，但是没有主动传递整个调用链，如果要强制灰度调用传递整个调用链，可以与设置灰度头的方式一样，强制传递，一般这种需求比较少

2. dubbo服务配置

```
dubbo.force.gray.tag=true
```

强制路由到灰度provider

具体描述可以看[dubbo官方文档](https://dubbo.apache.org/zh/docs/v2.7/user/examples/routing-rule/)

一般dubbo需要强制灰度的地方，可以独立配置是否强制灰度，否则进行自由灰度选择


### 部署策略

一般线上提供2套环境，一套正式环境，一套灰度相关服务，哪些服务需要灰度发布，独立发布对应的灰度服务到线上，统一线上的灰度标签值，保证灰度测试的准确路由即可。

### 优雅停机

nacos增加以下配置

```
# 优雅停机
# Enable gracefule shutdown
server.shutdown=graceful
# Allow grace timeout period
spring.lifecycle.timeout-per-shutdown-phase=20s
# Force enable health probes. Would be enabled on kubernetes platform by default
management.health.probes.enabled=true
# dubbo.properties
dubbo.service.shutdown.wait=15000
```

测试方法

1. 分别启动 UserService，WalletService，web-demo服务
2. 访问地址：http://localhost:19830/user/getBalance?uid=5，获得响应结果Wallet:5
3. 再次访问http://localhost:19830/user/getBalance?uid=4，同时直接停掉wallet-service服务
4. 直接获得请求结果，wallet：4， wallet-service服务停止下线

问题：
1. dubbo在3.0.5版本会出现当停掉wallet-service后，每隔60秒重连wallet-service，无限重连
2. dubbo在2.7.15无上述问题