package com.leimbag.web.demo.component.lifecycle;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author leimbag
 */
@Component
public class InstanceLifecycle implements ApplicationContextAware, SmartLifecycle {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile boolean running = false;

    private NacosServiceManager nacosServiceManager;
    private NacosDiscoveryProperties discoveryProperties;

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String discoveryUrl;

    @Autowired
    private Environment environment;

    private ApplicationContext applicationContext;

    public InstanceLifecycle(NacosServiceManager nacosServiceManager, NacosDiscoveryProperties discoveryProperties) {
        this.nacosServiceManager = nacosServiceManager;
        this.discoveryProperties = discoveryProperties;
    }

    public int getPort() {
        return Integer.parseInt(Objects.requireNonNull(environment.getProperty("server.port")));
    }

    @Override
    public void start() {
        logger.info("实例启动后自定义的操作");
        // 无需手工注册实例，使用系统自动注册的即可
//        logger.info("discoveryUrl:{}", discoveryUrl);
//        try {
//            NamingService namingService = NacosFactory.createNamingService(discoveryUrl);
//            logger.info("实例注册，service:{}, group:{}, cluster:{}, ip:{}, port:{}", discoveryProperties.getService(),
//                    discoveryProperties.getGroup(), discoveryProperties.getClusterName(),
//                    discoveryProperties.getIp(), getPort());
//            namingService.registerInstance(discoveryProperties.getService(), discoveryProperties.getIp(), getPort());
//        } catch (NacosException e) {
//            logger.error(e.getMessage(), e);
//        }

        running = true;
    }

    @Override
    public void stop() {
        logger.info("收到关闭Instance的信号自定义的操作");
        try {
            NamingService namingService = nacosServiceManager.getNamingService(discoveryProperties.getNacosProperties());
            logger.info("关闭服务，service:{}, group:{}, cluster:{}, ip:{}, port:{}", discoveryProperties.getService(),
                    discoveryProperties.getGroup(), discoveryProperties.getClusterName(),
                    discoveryProperties.getIp(), discoveryProperties.getPort());
            namingService.deregisterInstance(discoveryProperties.getService(), discoveryProperties.getIp(), discoveryProperties.getPort());
            logger.info("nacos注销实例完毕");
        } catch (NacosException e) {
            logger.error("nacos实例注销出现异常", e);
        }

        logger.info("Instance Shutdown 完成");
        running = false;
    }

    @Override
    public boolean isRunning() {
        logger.info("检查Instance的LifeCycle运行状态:{}", running);
        return running;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 启动时，越小越先执行
     * 关闭时，越大越先执行
     *
     * @return
     */
    @Override
    public int getPhase() {
        return 200;
    }

}
