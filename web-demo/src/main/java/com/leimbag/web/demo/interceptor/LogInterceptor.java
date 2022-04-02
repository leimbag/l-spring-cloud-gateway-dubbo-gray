package com.leimbag.web.demo.interceptor;

import com.leimbag.web.demo.constant.Constants;
import com.leimbag.web.demo.util.TraceIdUtil;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author leimbag
 */
public class LogInterceptor implements HandlerInterceptor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果有上层调用就用上层的ID
        String traceId = request.getHeader(Constants.TID);
        if (traceId == null) {
            traceId = TraceIdUtil.getTid();
        }

        MDC.put(Constants.TID, traceId);
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        //调用结束后删除
        MDC.remove(Constants.TID);
    }
}
