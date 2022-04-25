package com.leimbag.dubbo.user.util;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.StringReader;
import java.util.Properties;

/**
 * @author leimbag
 */
public class NacosHelper {
    protected static final Logger logger = LoggerFactory.getLogger(NacosHelper.class);

    public static Properties getCommonProperties(ConfigurableEnvironment environment) {
        Properties commonProperties = new Properties();
        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", environment.getProperty("spring.cloud.nacos.config.server-addr"));
            properties.setProperty("namespace", environment.getProperty("spring.cloud.nacos.config.namespace"));
//            properties.setProperty("username", "nacos");
//            properties.setProperty("password", "nacos");
            ConfigService configService = NacosFactory.createConfigService(properties);

            String commonData = configService.getConfig("common.properties", "common", 3000);
            commonProperties.load(new StringReader(commonData));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return commonProperties;
    }

    public static Properties getEmptyProperties(ConfigurableEnvironment environment) {
        return new Properties();
    }
}
