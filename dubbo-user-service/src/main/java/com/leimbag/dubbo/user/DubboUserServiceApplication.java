package com.leimbag.dubbo.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@ImportResource(value = {"classpath:META-INF/spring/bean-service-dubbo.xml"})
@SpringBootApplication
public class DubboUserServiceApplication {
	private static final Logger logger = LoggerFactory.getLogger(DubboUserServiceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DubboUserServiceApplication.class, args);
		logger.info("启动完毕");
	}

}
