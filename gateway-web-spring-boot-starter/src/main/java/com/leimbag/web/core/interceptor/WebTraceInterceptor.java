package com.leimbag.web.core.interceptor;

import com.leimbag.demo.core.constant.GatewayConstant;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author leimbag
 */
public class WebTraceInterceptor implements HandlerInterceptor {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String API_GW_REQUEST_ID = "API-GW-Request-Id";

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String gwRequestId = request.getHeader(API_GW_REQUEST_ID);

        if (StringUtils.hasText(gwRequestId)) {
            MDC.put(GatewayConstant.GID, gwRequestId);
            MDC.put(GatewayConstant.G_HOLDER_KEY, GatewayConstant.G_HOLDER_WEB);
            // 增加dubbo上下文
            RpcContext.getClientAttachment().setAttachment(GatewayConstant.GID, gwRequestId);// 设置消费者调用附件参数
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        RpcContext.getClientAttachment().clearAttachments();
        MDC.clear();
    }
}
