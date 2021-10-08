package com.leimbag.gateway.demo.component.ratelimit;

import com.leimbag.gateway.demo.util.IpUtil;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author leimbag
 */
@Component("ipKeyResolver")
public class IpKeyResolver implements KeyResolver {
    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return Mono.just(IpUtil.getClientIp(exchange.getRequest()));
    }
}
