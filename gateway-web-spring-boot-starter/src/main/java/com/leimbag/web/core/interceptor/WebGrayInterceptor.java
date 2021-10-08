package com.leimbag.web.core.interceptor;

import com.leimbag.demo.core.constant.GatewayConstant;
import com.leimbag.demo.core.constant.ServiceConstant;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author leimbag
 */
public class WebGrayInterceptor implements HandlerInterceptor {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String grayHeader = request.getHeader(GatewayConstant.REQUEST_HEADER_GRAY);
        if (Objects.isNull(grayHeader)) {
            return true;
        }
        // 增加dubbo上下文
        RpcContext.getContext().setAttachment(ServiceConstant.TAG_GRAY, grayHeader);
        RpcContext.getContext().setAttachment(CommonConstants.TAG_KEY, grayHeader);
        logger.info("设置dubbo灰度标签，grayHeader={}", grayHeader);
        return true;
    }

}
