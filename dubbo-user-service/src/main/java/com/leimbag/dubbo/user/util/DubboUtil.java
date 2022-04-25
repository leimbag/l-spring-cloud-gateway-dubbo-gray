package com.leimbag.dubbo.user.util;

import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.common.utils.NetUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author leimbag
 */
public class DubboUtil {
    private static final Logger logger = LoggerFactory.getLogger(DubboUtil.class);

    public static String devIpList = "172.16.0.0/16,192.168.0.0/16,10.99.0.0/16";

    /**
     * 是否开启本地dubbo开发环境监测
     */
    public static String LOCAL_DEV_DUBBO_ENABLE_NAME = "local.dev.dubbo.enable";
    public static String DEV_IP_LIST_NAME = "dev.ip.list";

    public static String DUBBO_REGISTRY_REGISTER_NAME = "dubbo.registry.register";
    public static String DUBBO_REGISTRY_SUBSCRIBE_NAME = "dubbo.registry.subscribe";

    public static boolean isInRange(String ip, String cidr) {
        if (StringUtils.isBlank(ip)) {
            logger.error("待检测ip为空，不在范围内，ip={}", ip);
            return false;
        }
        if (StringUtils.isBlank(cidr)) {
            logger.error("待检测cidr为空，无法确认范围，ip={}", ip);
            return false;
        }
        if (!cidr.contains("/")) {
            return ip.equals(cidr);
        }
        String[] ips = ip.split("\\.");
        int ipAddr = (Integer.parseInt(ips[0]) << 24)
                | (Integer.parseInt(ips[1]) << 16)
                | (Integer.parseInt(ips[2]) << 8) | Integer.parseInt(ips[3]);
        int type = Integer.parseInt(cidr.replaceAll(".*/", ""));
        int mask = 0xFFFFFFFF << (32 - type);
        String cidrIp = cidr.replaceAll("/.*", "");
        String[] cidrIps = cidrIp.split("\\.");
        int cidrIpAddr = (Integer.parseInt(cidrIps[0]) << 24)
                | (Integer.parseInt(cidrIps[1]) << 16)
                | (Integer.parseInt(cidrIps[2]) << 8)
                | Integer.parseInt(cidrIps[3]);
        return (ipAddr & mask) == (cidrIpAddr & mask);
    }

    public static String getLocalHost() {
        return NetUtils.getLocalHost();
    }

    public static boolean checkDeveloper(String ip, String ipList) {
        if (StringUtils.isBlank(ipList)) {
            // 未配置ip段，默认为非开发者，继续注册服务
            return false;
        }
        String[] devIpArray = ipList.split(",");
        for (String devIp : devIpArray) {
            boolean result = DubboUtil.isInRange(ip, devIp.trim());
            if (result) {
                return true;
            }
        }
        return false;
    }

    public static void localDevDubboCheck(ConfigurableEnvironment environment, Object logger) {
        // 优先读取本地开关配置
        String localDevEnableValue = environment.getProperty(LOCAL_DEV_DUBBO_ENABLE_NAME);
        printLog(logger, String.format("本地%s配置:%s", LOCAL_DEV_DUBBO_ENABLE_NAME, localDevEnableValue));
        if ("false".equalsIgnoreCase(localDevEnableValue)) {
            return;
        }

        // 读取nacos开关配置  此处使用空属性读取，是由于连接nacos配置中心读取耗时较长，连接需要5s+
        Properties commonProperties = NacosHelper.getEmptyProperties(environment);
        localDevEnableValue = commonProperties.getProperty(LOCAL_DEV_DUBBO_ENABLE_NAME);
        if ("false".equalsIgnoreCase(localDevEnableValue)) {
            return;
        }

        String devIpListValue = commonProperties.getProperty(DEV_IP_LIST_NAME);
        if (StringUtils.isBlank(devIpListValue)) {
            // 使用本地默认值
            devIpListValue = devIpList;
        }

        // 本地运行路径
//        printLog(logger, "user.dir:" + System.getProperty("user.dir"));

        String localHost = getLocalHost();
        boolean isDeveloper = checkDeveloper(localHost, devIpListValue);
        if (isDeveloper) {
            Map<String, Object> map = new HashMap<>();
            String name = "Dubbo_dev";
            String dubboRegistryRegisterValue = environment.getProperty(DUBBO_REGISTRY_REGISTER_NAME);
            if (StringUtils.isBlank(dubboRegistryRegisterValue)) {
                map.put(DUBBO_REGISTRY_REGISTER_NAME, false);
                dubboRegistryRegisterValue = "false";
            }
            String dubboRegistrySubscribeValue = environment.getProperty(DUBBO_REGISTRY_SUBSCRIBE_NAME);
            if (StringUtils.isBlank(dubboRegistrySubscribeValue)) {
                map.put(DUBBO_REGISTRY_SUBSCRIBE_NAME, true);
                dubboRegistrySubscribeValue = "true";
            }
            environment.getPropertySources().addFirst(new MapPropertySource(name, map));
            String message = String.format("current host: %s, devIpList:%s, 确认为本地开发者, 服务注册:%s, 服务订阅:%s",
                    localHost, devIpListValue, dubboRegistryRegisterValue, dubboRegistrySubscribeValue);
            printLog(logger, message);
        } else {
            String message = String.format("current host: %s, devIpList:%s, 非本地开发者，继续注册服务", localHost, devIpListValue);
            printLog(logger, message);
        }

        printLog(logger, "Dubbo本地开发环境校验完成");

    }

    public static void printLog(Object logger, String message) {
        if (Objects.isNull(logger)) {
            return;
        }
        if (logger instanceof DeferredLog) {
            DeferredLog log = (DeferredLog) logger;
            log.info(message);
        } else if (logger instanceof Logger) {
            Logger log = (Logger) logger;
            log.info(message);
        }
    }

    public static void main(String[] args) {
        System.out.println(isInRange("192.168.1.127", "192.168.1.64/26"));
        System.out.println(isInRange("192.168.1.2", "192.168.0.0/23"));
        System.out.println(isInRange("192.168.0.1", "192.168.0.0/24"));
        System.out.println(isInRange("192.168.0.0", "192.168.0.0/32"));
        System.out.println(isInRange("192.168.0.0", "192.168.0.1"));
    }
}
