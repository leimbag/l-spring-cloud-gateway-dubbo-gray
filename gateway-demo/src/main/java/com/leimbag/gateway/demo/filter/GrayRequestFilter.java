package com.leimbag.gateway.demo.filter;

import com.leimbag.demo.core.constant.GatewayConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author leimbag
 */
@Component
@ConditionalOnProperty(value = "gray.request.filter.enable", havingValue = "true")
public class GrayRequestFilter implements GlobalFilter, Ordered {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            ServerHttpRequest request = exchange.getRequest();
            String grayHeader = request.getHeaders().getFirst(GatewayConstant.REQUEST_HEADER_GRAY);
            // 原始请求头不为空，原样执行请求
            if (Objects.nonNull(grayHeader)) {
                return chain.filter(exchange);
            }

            grayHeader = processGrayHeader(request);

            // 未处理到灰度头，原样执行请求
            if (Objects.isNull(grayHeader)) {
                return chain.filter(exchange);
            }

            // 设置灰度头
            ServerHttpRequest serverHttpRequest = exchange.getRequest().mutate().header(GatewayConstant.REQUEST_HEADER_GRAY, grayHeader).build();
            return chain.filter(exchange.mutate().request(serverHttpRequest).build());

        } catch (Exception e) {
            logger.error("修改灰度头信息异常", e);
            return chain.filter(exchange);
        }

    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    /**
     * 自定义处理灰度头
     *
     * @param request
     * @return
     */
    protected String processGrayHeader(ServerHttpRequest request) {
        String uidString = request.getQueryParams().getFirst("uid");
        if (Objects.isNull(uidString)) {
            return null;
        }
        long uid = Long.parseLong(uidString);
        if (uid % 2 == 0) {
            return GatewayConstant.REQUEST_HEADER_GRAY_VALUE;
        } else {
            return null;
        }
    }
}
