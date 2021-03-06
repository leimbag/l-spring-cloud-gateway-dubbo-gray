package com.leimbag.gateway.demo.filter;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.leimbag.demo.core.util.IdGenerateUtil;
import com.leimbag.gateway.demo.bean.Log;
import com.leimbag.gateway.demo.config.GatewayRequestLogProperties;
import com.leimbag.gateway.demo.constant.HeaderConstant;
import com.leimbag.gateway.demo.util.IpUtil;
import com.leimbag.gateway.demo.util.LogHelper;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * @author leimbag
 */
@Component
@ConditionalOnProperty(value = "request.log.filter.enable", havingValue = "true")
public class RequestLogFilter implements GlobalFilter, Ordered {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GatewayRequestLogProperties gatewayRequestLogProperties;

    private final List<HttpMessageReader<?>> messageReaders;

    private final MeterRegistry meterRegistry;

    public final String DEFAULT_ERROR_HTTP_CODE = "500";

    private static final Joiner joiner = Joiner.on("");

    public RequestLogFilter(ServerCodecConfigurer codecConfigurer, GatewayRequestLogProperties gatewayRequestLogProperties, MeterRegistry meterRegistry) {
        this.messageReaders = codecConfigurer.getReaders();
        this.gatewayRequestLogProperties = gatewayRequestLogProperties;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            ServerHttpRequest request = exchange.getRequest();
            // ????????????????????????,??????????????????
            if (gatewayRequestLogProperties.getIgnorePaths().contains(request.getURI().getPath())) {
                logger.info("???????????????????????????????????????path={}, ignorePath={}", request.getURI().getPath(), LogHelper.toJsonWithObject(gatewayRequestLogProperties.getIgnorePaths()));
                return chain.filter(exchange);
            }

            final Log log = new Log();
            LogHelper.initWithRequest(log, request);
            LogHelper.setRoute(log, exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR));

            if (gatewayRequestLogProperties.getSaveRequestHeader()) {
                log.setRequestHeaders(request.getHeaders());
            }

            // ??????????????????, ??????requestId
            String requestId = request.getHeaders().getFirst(HeaderConstant.GW_REQUEST_ID);
            ServerHttpRequest serverHttpRequest = exchange.getRequest();
            if (StringUtils.isBlank(requestId)) {
                ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
                requestId = IdGenerateUtil.generateId();
                requestBuilder.header(HeaderConstant.GW_REQUEST_ID, requestId);
                serverHttpRequest = requestBuilder.build();
            }
            log.setRequestId(requestId);

            String contentType = request.getHeaders().getFirst(HeaderConstant.CONTENT_TYPE);
            log.setRequestContentType(contentType);

            if (null != contentType && HttpMethod.POST.equals(request.getMethod()) && gatewayRequestLogProperties.getSavePostRequestBody()) {
                if (contentType.contains(HeaderConstant.CONTENT_TYPE_JSON) || contentType.contains(HeaderConstant.CONTENT_TYPE_FORM_URLENCODED)) {
                    ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);
                    // ???????????????
                    Mono<String> modifiedBody = serverRequest.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.setRequestBody(handlePostBody(log.getUri(), body, contentType, gatewayRequestLogProperties));
                                return Mono.just(body);
                            });

                    BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, String.class);

                    HttpHeaders headers = new HttpHeaders();
                    headers.putAll(exchange.getRequest().getHeaders());
                    headers.remove(HttpHeaders.CONTENT_LENGTH);

                    CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
                    return bodyInserter.insert(outputMessage, new BodyInserterContext())
                            .then(Mono.defer(() -> chain.filter(exchange.mutate().request(decorateRequest(exchange, headers, outputMessage, log)).response(decorateResponse(exchange, log)).build())
                                    .then(Mono.fromRunnable(() -> handleLog(exchange, log)))))
                            .onErrorResume((Function<Throwable, Mono<Void>>) throwable -> release(exchange,
                                    outputMessage, throwable));
                }
            }
            if (HttpMethod.GET.equals(request.getMethod()) && gatewayRequestLogProperties.getSaveGetRequestBody()) {
                log.setRequestBody(request.getQueryParams().toString());
            }

            ServerWebExchange mutateExchange = exchange.mutate().request(serverHttpRequest).response(decorateResponse(exchange, log)).build();
            return mutateExchange.getSession().flatMap(webSession -> chain.filter(mutateExchange).then(Mono.fromRunnable(() -> handleLog(exchange, log))));
        } catch (Exception e) {
            printErrorLog(exchange);
            logger.error("????????????????????????", e);
            return chain.filter(exchange);
        }

    }

    protected ServerHttpRequestDecorator decorateRequest(ServerWebExchange exchange, HttpHeaders headers,
                                                         CachedBodyOutputMessage outputMessage, Log log) {
        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                long contentLength = headers.getContentLength();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(headers);
                // ????????????id
                httpHeaders.put(HeaderConstant.GW_REQUEST_ID, ImmutableList.of(log.getRequestId()));
                if (contentLength > 0) {
                    httpHeaders.setContentLength(contentLength);
                } else {
                    httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, HeaderConstant.DEFAULT_TRANSFER_ENCODING_VALUE);
                }
                return httpHeaders;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return outputMessage.getBody();
            }
        };
    }

    protected ServerHttpResponseDecorator decorateResponse(ServerWebExchange exchange, Log log) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        return new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (!gatewayRequestLogProperties.getSaveResponseBody()) {
                    // ?????????????????????????????????
                    return super.writeWith(body);
                }

                String originalResponseContentType = exchange.getAttribute(ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
                log.setResponseContentType(originalResponseContentType);
                if (gatewayRequestLogProperties.getSaveResponseHeader()) {
                    log.setResponseHeaders(exchange.getResponse().getHeaders());
                }
                // Objects.equals(getStatusCode(), HttpStatus.OK) &&  ??????????????????response??????????????????
                if (body instanceof Flux) {
                    DataBufferFactory bufferFactory = originalResponse.bufferFactory();
                    // ????????????ContentType?????????JSON???HTML????????????
                    if (StringUtils.isNotBlank(originalResponseContentType)) {
                        if (originalResponseContentType.contains(HeaderConstant.CONTENT_TYPE_JSON) ||
                                originalResponseContentType.contains(HeaderConstant.CONTENT_TYPE_TEXT_HTML) ||
                                originalResponseContentType.contains(HeaderConstant.CONTENT_TYPE_TEXT_PLAIN)
                        ) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                                // ???????????????????????????
                                List<String> list = Lists.newArrayList();
                                dataBuffers.forEach(dataBuffer -> {
                                    try {
                                        byte[] content = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(content);
                                        list.add(new String(content, StandardCharsets.UTF_8.name()));
                                    } catch (Exception e) {
                                        logger.warn("??????ResponseBody????????????????????????{}", Throwables.getStackTraceAsString(e));
                                    } finally {
                                        DataBufferUtils.release(dataBuffer);
                                    }
                                });
                                String responseData = joiner.join(list);
                                // ??????ResponseData
                                log.setResponseBody(LogHelper.parseJsonToObject(responseData));
                                byte[] uppedContent = new String(responseData.getBytes(), StandardCharsets.UTF_8).getBytes();
                                exchange.getResponse().getHeaders().setContentLength(uppedContent.length);
                                return bufferFactory.wrap(uppedContent);
                            }));
                        }
                    }
                }
                return super.writeWith(body);
            }

            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body).flatMapSequential(p -> p));
            }
        };
    }

    protected Mono<Void> release(ServerWebExchange exchange, CachedBodyOutputMessage outputMessage,
                                 Throwable throwable) {
        return outputMessage.getBody().map(DataBufferUtils::release).then(Mono.error(throwable));
    }

    protected Object handlePostBody(String uri, String body, String contentType, GatewayRequestLogProperties gatewayRequestLogProperties) {
        if (!gatewayRequestLogProperties.getDecodeRequestBody()) {
            return body;
        }
        try {
            // a=1&b=2 ??????
            if (contentType.contains(HeaderConstant.CONTENT_TYPE_FORM_URLENCODED)) {
//                logger.info("uri={}, ??????Post??????Body:{}", uri, body);
                boolean isCommandRequest = false;
                // ??????map
                Map<String, Object> bodyMap = new TreeMap<>();
                String[] paramArray = body.split("&");
                for (String param : paramArray) {
                    String[] paramItem = param.split("=");
                    if (paramItem.length == 1) {
                        bodyMap.put(paramItem[0], "");
                    } else if (paramItem.length == 2) {
                        String value = paramItem[1];
                        try {
                            value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
                        } catch (UnsupportedEncodingException e) {
                            logger.warn("URLdecode??????PostBody???value?????????param={}, value={}", paramItem[0], paramItem[1]);
                            logger.warn(e.getMessage(), e);
                        }
                        Object parseValue = LogHelper.parseJsonToObject(value);
                        bodyMap.put(paramItem[0], parseValue);
                    } else {
                        logger.warn("??????{}?????????????????????????????????????????????????????????{}", paramItem[0], paramItem.length);
                    }
                }
                return bodyMap;
            }
        } catch (Exception e) {
            logger.error("??????PostBody?????????contentType={}, postBody={}", contentType, body);
            logger.error(e.getMessage(), e);
        }
        return body;
    }

    protected void handleLog(ServerWebExchange exchange, Log log) {
        ServerHttpResponse response = exchange.getResponse();
        HttpStatus statusCode = response.getStatusCode();
        log.setHttpStatus(statusCode == null ? DEFAULT_ERROR_HTTP_CODE : String.valueOf(statusCode.value()));
        long handleTime = LogHelper.getHandleTime(log);
        log.setHandleTime(handleTime);
        LogHelper.record(log);
        collectMetrics(meterRegistry, log);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    protected void collectMetrics(MeterRegistry meterRegistry, Log log) {
        meterRegistry.summary("tkl_gateway_http_request_summary",
                "httpMethod", log.getHttpMethod(),
                "uri", log.getUri(),
                "routeId", log.getRouteId(),
                "statusCode", log.getHttpStatus()
        ).record(log.getHandleTime());
    }

    protected void printErrorLog(ServerWebExchange exchange) {
        try {
            ServerHttpRequest request = exchange.getRequest();
            URI requestUri = request.getURI();
            logger.error("?????????????????????????????????, uri:{}, uriQuery:{}, httpMethod:{}, ip:{}, userAgent:{}",
                    requestUri.getPath(), requestUri.getQuery(), request.getMethodValue(), IpUtil.getClientIp(request),
                    request.getHeaders().getFirst(HeaderConstant.USER_AGENT));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

}
