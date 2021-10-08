package com.leimbag.gateway.demo.loadbalancer;

import com.leimbag.demo.core.constant.GatewayConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author leimbag
 */
public class GrayRoundRobinLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;
    private final AtomicInteger position;

    public GrayRoundRobinLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.position = new AtomicInteger(new Random().nextInt(1000));
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next().map(list -> processInstanceResponse(list, request));
    }

    private Response<ServiceInstance> processInstanceResponse(List<ServiceInstance> instances, Request request) {
        if (instances.isEmpty()) {
            logger.warn("No servers available for service: " + this.serviceId);
            return new EmptyResponse();
        }

        DefaultRequestContext requestContext = (DefaultRequestContext) request.getContext();
        RequestData clientRequest = (RequestData) requestContext.getClientRequest();
        HttpHeaders headers = clientRequest.getHeaders();

        String grayHeader = headers.getFirst(GatewayConstant.REQUEST_HEADER_GRAY);

        if (StringUtils.isEmpty(grayHeader)) {
            return processRibbonInstanceResponse(instances);
        }

        List<ServiceInstance> serviceInstances = instances.stream()
                .filter(instance -> grayHeader.equals(instance.getMetadata().get("version")))
                .collect(Collectors.toList());
        // 策略 目前使用1
        // 1. 指定灰度必须走灰度服务
        if (serviceInstances.isEmpty()) {
            logger.warn("No gray servers available for service: " + this.serviceId);
            return new EmptyResponse();
        } else {
            return processRibbonInstanceResponse(serviceInstances);
        }

        // 2. 指定灰度有灰度走灰度，没灰度走默认服务
//        if(serviceInstances.size() > 0){
//            return processRibbonInstanceResponse(serviceInstances);
//        }else{
//            return processRibbonInstanceResponse(instances);
//        }

    }

    /**
     * 负载均衡器
     * 参考 org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer#getInstanceResponse
     *
     */
    private Response<ServiceInstance> processRibbonInstanceResponse(List<ServiceInstance> instances) {
        int pos = Math.abs(this.position.incrementAndGet());
        ServiceInstance instance = instances.get(pos % instances.size());
        return new DefaultResponse(instance);
    }

}
