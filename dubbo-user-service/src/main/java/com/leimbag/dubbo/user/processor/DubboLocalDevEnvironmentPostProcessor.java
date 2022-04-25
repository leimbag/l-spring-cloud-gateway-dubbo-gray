package com.leimbag.dubbo.user.processor;

import com.leimbag.dubbo.user.util.DubboUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author leimbag
 */
public class DubboLocalDevEnvironmentPostProcessor implements EnvironmentPostProcessor, ApplicationListener<ApplicationEvent>, Ordered {
    private static final DeferredLog LOGGER = new DeferredLog();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        DubboUtil.localDevDubboCheck(environment, LOGGER);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationPreparedEvent) {
            // 回放日志
            LOGGER.replayTo(DubboLocalDevEnvironmentPostProcessor.class);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
