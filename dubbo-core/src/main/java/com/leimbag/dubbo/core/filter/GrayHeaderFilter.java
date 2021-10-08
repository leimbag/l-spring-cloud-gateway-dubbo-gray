package com.leimbag.dubbo.core.filter;

import com.leimbag.demo.core.constant.ServiceConstant;
import com.leimbag.dubbo.core.context.GrayHeaderHolder;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 不推荐同时激活生产和消费在同一个过滤中，不方便处理GrayHeaderHolder上下文清理情况
 *
 * @author leimbag
 */
//@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER}, order = -30000)
public class GrayHeaderFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        logger.info("灰度头过滤, attHeader={}, tagHeader={}, holderHeader={}, ConsumerSide={}, ProviderSide={}, interface={}, serviceName={}, methodName={}",
                RpcContext.getContext().getAttachment(ServiceConstant.TAG_GRAY),
                RpcContext.getContext().getAttachment(CommonConstants.TAG_KEY),
                GrayHeaderHolder.getGrayHeader(),
                RpcContext.getContext().isConsumerSide(), RpcContext.getContext().isProviderSide(),
                invoker.getInterface().getName(), invocation.getServiceName(), invocation.getMethodName());

        if (RpcContext.getContext().isConsumerSide()) {
            if (Objects.nonNull(GrayHeaderHolder.getGrayHeader())) {
                logger.info("设置灰度头到RpcContext上下文，grayHeader={}, serviceName={}, serviceMethod={}",
                        GrayHeaderHolder.getGrayHeader(), invocation.getServiceName(), invocation.getMethodName());
                RpcContext.getContext().setAttachment(ServiceConstant.TAG_GRAY, GrayHeaderHolder.getGrayHeader());
                RpcContext.getContext().setAttachment(CommonConstants.TAG_KEY, GrayHeaderHolder.getGrayHeader());
            }
        }
        if (RpcContext.getContext().isProviderSide()) {
            String grayHeader = RpcContext.getContext().getAttachment(ServiceConstant.TAG_GRAY);
            if (Objects.nonNull(grayHeader)) {
                logger.info("读取灰度头从RpcContext上下文，grayHeader={}, serviceName={}, serviceMethod={}",
                        grayHeader, invoker.getInterface(), invocation.getMethodName());
                GrayHeaderHolder.setGrayHeader(grayHeader);
                RpcContext.getContext().setAttachment(ServiceConstant.TAG_GRAY, grayHeader);
                RpcContext.getContext().setAttachment(CommonConstants.TAG_KEY, grayHeader);
            }
        }
//        try {
        return invoker.invoke(invocation);
//        } finally {
//            logger.info("finally执行,ConsumerSide={}, ProviderSide={}", RpcContext.getContext().isConsumerSide(), RpcContext.getContext().isProviderSide());
//            if (RpcContext.getContext().isProviderSide()) {
//                logger.info("清除灰度头，grayHeader={}", GrayHeaderHolder.getGrayHeader());
//                GrayHeaderHolder.removeGrayHeader();
//            }
//        }

    }
}
