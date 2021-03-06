package com.leimbag.web.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * @author leimbag
 */
@ImportResource(value = {"classpath:META-INF/spring/bean-service-dubbo.xml"})
@SpringBootApplication
public class WebDemoApplication {
    private static final Logger logger = LoggerFactory.getLogger(WebDemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WebDemoApplication.class, args);
        logger.info("启动完毕");
    }
}
