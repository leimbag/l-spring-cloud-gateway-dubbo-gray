package com.leimbag.gateway.demo;

import com.leimbag.gateway.demo.config.GrayLoadBalancerClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;

/**
 * @author leimbag
 */
@LoadBalancerClients(defaultConfiguration = GrayLoadBalancerClientConfiguration.class)
@SpringBootApplication
public class GatewayDemoApplication {
    private static final Logger logger = LoggerFactory.getLogger(GatewayDemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(GatewayDemoApplication.class, args);
        logger.info("启动完毕");
    }
}
