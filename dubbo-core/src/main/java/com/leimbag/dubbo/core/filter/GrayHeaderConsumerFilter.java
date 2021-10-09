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
 * @author leimbag
 */
@Activate(group = {CommonConstants.CONSUMER}, order = -30000)
public class GrayHeaderConsumerFilter implements Filter {
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
        logger.info("[服务消费者]灰度头过滤, attHeader={}, tagHeader={}, holderHeader={}, ConsumerSide={}, ProviderSide={}, interface={}, serviceName={}, methodName={}",
                RpcContext.getContext().getAttachment(ServiceConstant.TAG_GRAY),
                RpcContext.getContext().getAttachment(CommonConstants.TAG_KEY),
                GrayHeaderHolder.getGrayHeader(),
                RpcContext.getContext().isConsumerSide(), RpcContext.getContext().isProviderSide(),
                invoker.getInterface().getName(), invocation.getServiceName(), invocation.getMethodName());
        String grayHeader = GrayHeaderHolder.getGrayHeader();
        if (Objects.nonNull(grayHeader)) {
            logger.info("[服务消费者]设置灰度头到RpcContext上下文，grayHeader={}, serviceName={}, serviceMethod={}",
                    grayHeader, invocation.getServiceName(), invocation.getMethodName());
            RpcContext.getContext().setAttachment(ServiceConstant.TAG_GRAY, grayHeader);
            RpcContext.getContext().setAttachment(CommonConstants.TAG_KEY, grayHeader);
            String forceUseGrayTag = environment.getProperty(ServiceConstant.DUBBO_FORCE_GRAY_TAG_KEY);
            if (ServiceConstant.FORCE_USE_TAG_VALUE.equals(forceUseGrayTag)) {
                RpcContext.getContext().setAttachment(Constants.FORCE_USE_TAG, ServiceConstant.FORCE_USE_TAG_VALUE);
            }
        }

        return invoker.invoke(invocation);

    }
}
