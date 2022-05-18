package com.leimbag.web.core.config;

import com.leimbag.web.core.interceptor.WebTraceInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;

/**
 * @author leimbag
 */
@Configuration
@ConditionalOnProperty(value = "web.trace.interceptor.enable", havingValue = "true")
public class WebTraceConfig {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void init() {
        logger.info("Web跟踪拦截开启");
    }

    @Bean
    WebTraceInterceptor webTraceInterceptor() {
        return new WebTraceInterceptor();
    }

    @Bean
    public WebMvcConfigurer webTraceConfigWebMvcConfigurer(WebTraceInterceptor webTraceInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                InterceptorRegistration interceptorRegistration = registry.addInterceptor(webTraceInterceptor);
                interceptorRegistration.addPathPatterns("/**");
            }
        };
    }

}
