package com.leimbag.web.demo.component.lifecycle;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.spring.util.DubboBeanUtils;
import org.apache.dubbo.rpc.model.ModelConstants;
import org.apache.dubbo.rpc.model.ModuleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

/**
 * @author leimbag
 */
@Component
public class DubboLifecycle implements ApplicationContextAware, SmartLifecycle {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile boolean running = false;

    private ApplicationContext applicationContext;

    @Override
    public void start() {
        logger.info("Dubbo启动后自定义的操作");
        ModuleModel moduleModel = DubboBeanUtils.getModuleModel(applicationContext);
        moduleModel.setAttribute(ModelConstants.KEEP_RUNNING_ON_SPRING_CLOSED, Boolean.TRUE);
        // 取消Dubbo的jvm钩子
        removeJvmDubboShutdownHook();
        running = true;
    }

    @Override
    public void stop() {
        logger.info("收到关闭Dubbo的信号自定义的操作");
        try {
            ModuleModel moduleModel = DubboBeanUtils.getModuleModel(applicationContext);
            moduleModel.destroy();
        } catch (Exception e) {
            logger.error("停止dubbo module发生错误：" + e.getMessage(), e);
        }

        logger.info("Dubbo Shutdown 完成");
        running = false;
    }

    @Override
    public boolean isRunning() {
        logger.info("检查Dubbo的LifeCycle运行状态:{}", running);
        return running;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public int getPhase() {
        return 100;
    }

    protected void removeJvmDubboShutdownHook() {
        try {
            Class<?> clazz = Class.forName("java.lang.ApplicationShutdownHooks");
            Field field = clazz.getDeclaredField("hooks");
            field.setAccessible(true);
            Object hooks = field.get(null);
            Map<Thread, Thread> hooksMap = (Map<Thread, Thread>) hooks;
            Iterator<Map.Entry<Thread, Thread>> it = hooksMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Thread, Thread> entry = it.next();
                Thread t = entry.getKey();
                if (StringUtils.equals("DubboShutdownHook", t.getName())) {
                    it.remove();
                    logger.info("remove DubboShutdownHook success");
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
