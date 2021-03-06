package com.leimbag.dubbo.core.filter;

import com.leimbag.demo.core.constant.ServiceConstant;
import com.leimbag.dubbo.core.context.GrayHeaderHolder;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.Objects;

/**
 * 使用独立生产者过滤器，主要解决生产者最后清空灰度头问题
 * @author leimbag
 */
@Activate(group = {CommonConstants.PROVIDER}, order = -30000)
public class GrayHeaderProviderFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Environment environment;

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        logger.info("[服务提供者]灰度头过滤, attHeader={}, tagHeader={}, holderHeader={}, interface={}, serviceName={}, methodName={}",
                RpcContext.getContext().getAttachment(ServiceConstant.TAG_GRAY),
                RpcContext.getContext().getAttachment(CommonConstants.TAG_KEY),
                GrayHeaderHolder.getGrayHeader(),
                invoker.getInterface().getName(), invocation.getServiceName(), invocation.getMethodName());

        // 读取dubbo.tag会出现null，应该是传递中被销毁，使用自定义灰度标签
        String grayHeader = RpcContext.getContext().getAttachment(ServiceConstant.TAG_GRAY);
        if (Objects.nonNull(grayHeader)) {
            logger.info("读取灰度头从RpcContext上下文，grayHeader={}, serviceName={}, serviceMethod={}",
                    grayHeader, invoker.getInterface(), invocation.getMethodName());
            GrayHeaderHolder.setGrayHeader(grayHeader);
            logger.info("[服务提供者]设置灰度头到RpcContext上下文，grayHeader={}, serviceName={}, serviceMethod={}",
                    grayHeader, invocation.getServiceName(), invocation.getMethodName());
            RpcContext.getContext().setAttachment(ServiceConstant.TAG_GRAY, grayHeader);
            RpcContext.getContext().setAttachment(CommonConstants.TAG_KEY, grayHeader);
            String forceUseGrayTag = environment.getProperty(ServiceConstant.DUBBO_FORCE_GRAY_TAG_KEY);
            if (ServiceConstant.FORCE_USE_TAG_VALUE.equals(forceUseGrayTag)) {
                RpcContext.getContext().setAttachment(Constants.FORCE_USE_TAG, ServiceConstant.FORCE_USE_TAG_VALUE);
            }
        }

        try {
            return invoker.invoke(invocation);
        } finally {
            logger.info("清除灰度头，grayHeader={}", GrayHeaderHolder.getGrayHeader());
            GrayHeaderHolder.removeGrayHeader();
        }

    }
}
