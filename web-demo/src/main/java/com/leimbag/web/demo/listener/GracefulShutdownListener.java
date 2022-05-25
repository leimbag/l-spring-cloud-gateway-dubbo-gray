package com.leimbag.web.demo.listener;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.naming.NamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * @author leimbag
 */
@Component
public class GracefulShutdownListener implements ApplicationListener<ContextClosedEvent> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private NacosServiceManager nacosServiceManager;
    private NacosDiscoveryProperties discoveryProperties;

    @Value("${deregister.instance.wait.second:3}")
    private Integer deregisterInstanceWaitSecond = 3;

    public GracefulShutdownListener(NacosServiceManager nacosServiceManager, NacosDiscoveryProperties discoveryProperties) {
        this.nacosServiceManager = nacosServiceManager;
        this.discoveryProperties = discoveryProperties;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        logger.info("收到关闭Instance的信号事件");
        try {
            NamingService namingService = nacosServiceManager.getNamingService(discoveryProperties.getNacosProperties());
            logger.info("关闭服务，service:{}, group:{}, cluster:{}, ip:{}, port:{}", discoveryProperties.getService(),
                    discoveryProperties.getGroup(), discoveryProperties.getClusterName(),
                    discoveryProperties.getIp(), discoveryProperties.getPort());
            namingService.deregisterInstance(discoveryProperties.getService(), discoveryProperties.getIp(), discoveryProperties.getPort());
            TimeUnit.SECONDS.sleep(deregisterInstanceWaitSecond);
            logger.info("nacos注销实例完毕");
        } catch (Exception e) {
            logger.error("nacos实例注销出现异常", e);
        }

        logger.info("Instance Shutdown 完成");
        logger.info("优雅关机");
    }

}
