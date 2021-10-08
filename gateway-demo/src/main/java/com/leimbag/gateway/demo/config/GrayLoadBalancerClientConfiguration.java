package com.leimbag.gateway.demo.config;

import com.leimbag.gateway.demo.loadbalancer.GrayRoundRobinLoadBalancer;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author leimbag
 */
public class GrayLoadBalancerClientConfiguration {
    @Bean
    ReactorLoadBalancer<ServiceInstance> grayRoundRobinLoadBalancer(Environment environment,
                                                                 LoadBalancerClientFactory loadBalancerClientFactory) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new GrayRoundRobinLoadBalancer(loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
    }
}
