## 网关服务

网关感知服务平滑下线,用户端无感知

### 网关端

1. 在网关增加Nacos监听器

Subscriber订阅InstancesChangeEvent事件，实现事件监听。

```
NotifyCenter.registerSubscriber(this);
```

gateway集成了ribbon的负载均衡功能，其默认是定时一定的时间间隔去nacos拉取最新的服务实例数据到本地缓存

可以设置ribbon.ServerListRefreshInterval，增加拉取nacos中最新服务实例的频率，默认30秒
```
ribbon.ServerListRefreshInterval=3000
```

本实现网关没有使用调小ServerListRefreshInterval频率方式，而是监听到实例变更后，利用反射清理LoadBalancerCacheManager的cacheMap缓存。

2. 网关失败重试
spring gateway的失败重置在路由中配置，以Filter的形式实现。default-filters，对所以路由服务提供失败重试机制

在application.yml增加默认重试配置，针对BAD_GATEWAY状态，Get，Post请求方式，重试1次

其他参考状态见：org.springframework.http.HttpStatus

```
spring:
  cloud:
    gateway:
      default-filters:
        - name: Retry
          args:
            retries: 1
            methods: GET,POST
            statuses:
              - BAD_GATEWAY
```

3. 其他  

如果不使用spring cloud自定义的ServiceInstanceListSupplier，可以通过一下方式自定义

在自定义的路由配置GrayLoadBalancerClientConfiguration中增加自定义的customDiscoveryClientServiceInstanceListSupplier

```
public class GrayLoadBalancerClientConfiguration {
    @Bean
    ReactorLoadBalancer<ServiceInstance> grayRoundRobinLoadBalancer(Environment environment,
                                                                 LoadBalancerClientFactory loadBalancerClientFactory) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new GrayRoundRobinLoadBalancer(loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
    }

    @Bean
    public ServiceInstanceListSupplier customDiscoveryClientServiceInstanceListSupplier(
            ConfigurableApplicationContext context) {
        return ServiceInstanceListSupplier.builder().withDiscoveryClient().build(context);
    }
}
```

### Web实例端

网关路由的实例注销，通过手工注销通知，具体见对应的web-demo的InstanceLifecycle，可以提升服务下线通知时间效率

```
NamingService namingService = nacosServiceManager.getNamingService(discoveryProperties.getNacosProperties());
logger.info("关闭服务，service:{}, group:{}, cluster:{}, ip:{}, port:{}", discoveryProperties.getService(),
            discoveryProperties.getGroup(), discoveryProperties.getClusterName(),
            discoveryProperties.getIp(), discoveryProperties.getPort());
namingService.deregisterInstance(discoveryProperties.getService(), discoveryProperties.getIp(), discoveryProperties.getPort());
```

### Nacos服务端  配置改动(可选)

web端推送事件会有一个delay配置，nacos/conf/application.properties，内部配置
```
### The delay time before push task to execute from service changed, unit: milliseconds.
nacos.naming.push.pushTaskDelay=500
```
系统默认配置为500ms，因此会有500ms的感知延迟，如果想通知的更为及时，可以修改该值为100ms

配合网关服务失败重试配置，可以做到实时服务下线，无感发布


### 其他方式 

网关直接关闭缓存，一般用于开发环境

```
spring.cloud.loadbalancer.cache.enabled=false 
```
spring.cloud.loadbalancer.cache.enabled值设置为 false 来完全禁用 loadBalancer 缓存

注意：尽管不开启缓存对于开发和测试很有用，但其效率远低于将缓存开启，因此建议在生产环境始终启用缓存

### Nacos客户端配置（可选）

1. 微服务的nacos的心跳配置时间
```
preserved.heart.beat.interval: 1000 #该实例在客户端上报心跳的间隔时间。（单位:毫秒）
preserved.heart.beat.timeout: 3000 #该实例在不发送心跳后，从健康到不健康的时间。（单位:毫秒）
preserved.ip.delete.timeout: 3000 #该实例在不发送心跳后，被nacos下掉该实例的时间。（单位:毫秒）
```

配置样例如下：
```
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        metadata: 
          preserved.heart.beat.interval: 1000
          preserved.heart.beat.timeout: 3000
          preserved.ip.delete.timeout: 3000
```