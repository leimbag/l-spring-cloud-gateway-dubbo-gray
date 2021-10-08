package com.leimbag.web.core.config;

import com.leimbag.web.core.interceptor.WebGrayInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author leimbag
 */
@Configuration
@ConditionalOnProperty(value = "web.gray.interceptor.enable", havingValue = "true")
public class WebGrayConfig implements WebMvcConfigurer {

    @Bean
    WebGrayInterceptor webGrayInterceptor() {
        return new WebGrayInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 灰度
        registry.addInterceptor(webGrayInterceptor()).addPathPatterns("/**");
    }
}
