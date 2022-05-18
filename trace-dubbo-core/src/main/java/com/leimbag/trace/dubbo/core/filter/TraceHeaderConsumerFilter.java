package com.leimbag.trace.dubbo.core.filter;

import com.leimbag.demo.core.constant.GatewayConstant;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @author leimbag
 */
@Activate(group = {CommonConstants.CONSUMER}, order = -31000)
public class TraceHeaderConsumerFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String gid = RpcContext.getClientAttachment().getAttachment(GatewayConstant.GID);// 消费者获取姿势
        if (Objects.isNull(gid)) {
            // 判断上下文中是否有GID
            if (StringUtils.hasText(MDC.get(GatewayConstant.GID))) {
                gid = MDC.get(GatewayConstant.GID);
            }
        }
        if (Objects.nonNull(gid)) {
            MDC.put(GatewayConstant.GID, gid);
            RpcContext.getClientAttachment().setAttachment(GatewayConstant.GID, gid);// 消费者传递gid给生产者姿势
        }

        return invoker.invoke(invocation);

    }

}
