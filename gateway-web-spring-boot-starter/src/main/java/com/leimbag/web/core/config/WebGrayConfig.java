package com.leimbag.web.core.config;

import com.leimbag.web.core.bean.WebGrayProperties;
import com.leimbag.web.core.interceptor.WebGrayInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@ConditionalOnProperty(value = "web.gray.interceptor.enable", havingValue = "true")
@EnableConfigurationProperties(WebGrayProperties.class)
public class WebGrayConfig {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void init(){
        logger.info("灰度Web拦截开启");
    }

    @Bean
    WebGrayInterceptor webGrayInterceptor(WebGrayProperties webGrayProperties) {
        return new WebGrayInterceptor(webGrayProperties);
    }

    @Bean
    public WebMvcConfigurer webGrayConfigWebMvcConfigurer(WebGrayInterceptor webGrayInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                InterceptorRegistration interceptorRegistration = registry.addInterceptor(webGrayInterceptor);
                interceptorRegistration.addPathPatterns("/**");
            }
        };
    }

}
