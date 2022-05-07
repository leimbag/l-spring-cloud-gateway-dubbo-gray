package com.leimbag.gateway.demo.listener;

import cn.hutool.core.util.ReflectUtil;
import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.leimbag.demo.core.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.RouteRefreshListener;
import org.springframework.cloud.loadbalancer.cache.LoadBalancerCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * 路由刷新监听器
 */
@Component
public class InstancesChangeEventListener extends Subscriber<InstancesChangeEvent> implements ApplicationEventPublisherAware {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private RouteRefreshListener routeRefreshListener;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private RouteLocator routeLocator;

    @PostConstruct
    private void init() {
        NotifyCenter.registerSubscriber(this);
    }

    @Override
    public void onEvent(InstancesChangeEvent event) {
        logger.info("接收到InstancesChangeEvent订阅事件：{}", LogHelper.toJsonWithObject(event));
        publishEvent();
    }

    @Override
    public Class<? extends Event> subscribeType() {
        return InstancesChangeEvent.class;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishEvent() {
//        CachingRouteLocator cachingRouteLocator = (CachingRouteLocator) routeLocator;
//        cachingRouteLocator.refresh();
//        applicationEventPublisher.publishEvent(new RefreshRoutesEvent(new Object()));
//        routeRefreshListener.onApplicationEvent(new ContextRefreshedEvent(applicationContext));
//        cachingRouteLocator.onApplicationEvent(new RefreshRoutesEvent(new Object()));

        cleanLoadBalancerCache();

        logger.info("刷新发布事件完毕");
    }

    public void cleanLoadBalancerCache() {
        LoadBalancerCacheManager loadBalancerCacheManager = applicationContext.getBean(LoadBalancerCacheManager.class);
        ConcurrentMap<String, Cache> cacheMap = (ConcurrentMap<String, Cache>) ReflectUtil.getFieldValue(loadBalancerCacheManager, "cacheMap");
        for (Map.Entry<String, Cache> stringCacheEntry : cacheMap.entrySet()) {
            String key = stringCacheEntry.getKey();
            Cache cache = stringCacheEntry.getValue();
            logger.info("清理LoadBalancer缓存，key:{}, value:{}", key, cache);
            cache.clear();
        }
    }

}
