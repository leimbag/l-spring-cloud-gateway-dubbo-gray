package com.leimbag.trace.dubbo.core.filter;

import com.leimbag.demo.core.constant.GatewayConstant;
import com.leimbag.trace.dubbo.core.context.TraceHeaderHolder;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Objects;

/**
 * 使用独立生产者过滤器，主要解决生产者最后清空跟踪头问题
 *
 * @author leimbag
 */
@Activate(group = {CommonConstants.PROVIDER}, order = -31000)
public class TraceHeaderProviderFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String gid = invocation.getAttachment(GatewayConstant.GID);// 获取跟踪id姿势

        if (Objects.nonNull(gid)) {
            MDC.put(GatewayConstant.GID, gid);
            RpcContext.getClientAttachment().setAttachment(GatewayConstant.GID, gid); // 调用后续消费者传递姿势
            RpcContext.getServerAttachment().setAttachment(GatewayConstant.GID, gid); // 生产者端本地使用的参数
            TraceHeaderHolder.setTraceHeader(gid);
        }

        try {
            return invoker.invoke(invocation);
        } finally {
            TraceHeaderHolder.removeTraceHeader();
            MDC.clear();
        }

    }

}
