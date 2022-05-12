package com.leimbag.gateway.demo.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author leimbag
 */
@Configuration
@EnableConfigurationProperties({GatewayRequestLogProperties.class})
public class GatewayConfig {
}
