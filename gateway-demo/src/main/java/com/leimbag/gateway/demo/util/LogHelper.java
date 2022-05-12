package com.leimbag.gateway.demo.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leimbag.gateway.demo.bean.Log;
import com.leimbag.gateway.demo.constant.HeaderConstant;
import com.leimbag.gateway.demo.enums.LogLevel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author leimbag
 */
public class LogHelper {
    private final static ObjectMapper objectMapper = new ObjectMapper();
//    private final static Logger logger = LoggerFactory.getLogger("AccessLogger");
    private final static Logger logger = LoggerFactory.getLogger(LogHelper.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    static {
        objectMapper.setDateFormat(DATE_FORMAT);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    public static String toJsonString(@NonNull Log log) {
        try {
            return objectMapper.writeValueAsString(log);
        } catch (JsonProcessingException e) {
            logger.error("Log转换JSON异常", e);
            return null;
        }
    }

    public static String toJsonWithObject(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("object转换JSON异常", e);
            return null;
        }
    }

    public static <T> T parseJson(String json, Class<T> targetType) {
        T object = null;
        try {
            object = objectMapper.readValue(json, targetType);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
        return object;
    }

    /**
     * 解析json字符串为对象，如果是json，将解析为JsonNode对象，其他格式一律原样返回
     *
     * @param json
     * @return
     */
    public static Object parseJsonToObject(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonParseException e) {
            return json;
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
        return json;
    }

    public static void record(Log log) {
        logger.info(toJsonString(log));
    }

    public static long getHandleTime(HttpHeaders headers) {
        String startTimestamp = headers.getFirst(HeaderConstant.GW_REQUEST_TIME);
        long startTime = StringUtils.isNotBlank(startTimestamp) ? Long.parseLong(startTimestamp) : System.currentTimeMillis();
        return System.currentTimeMillis() - startTime;
    }

    public static long getHandleTime(Log log) {
        return System.currentTimeMillis() - log.getRequestTime();
    }

    public static void initWithRequest(Log log, ServerHttpRequest request) {
        URI requestUri = request.getURI();

        log.setLevel(LogLevel.INFO);
        log.setUri(requestUri.getPath());
        log.setUriQuery(requestUri.getQuery());
        log.setHttpMethod(request.getMethodValue());

        log.setIp(IpUtil.getClientIp(request));

        log.setUserAgent(request.getHeaders().getFirst(HeaderConstant.USER_AGENT));

    }

    public static void setRoute(Log log, Route route) {
        if (route == null || route.getId() == null) {
            log.setRouteId("");
            return;
        }
        log.setRouteId(route.getId());
    }

    public static void main(String[] args) {

        String json = "{\"data\":{\"phase\":0,\"orderId\":\"3607760444891110807\",\"orderType\":1,\"lotteryType\":71,\"playType\":\"710100\",\"multiple\":1,\"addition\":0,\"amount\":100,\"betCount\":1,\"originalContent\":\"71~710100%1%bar\",\"orderPayType\":1,\"couponId\":null},\"clientInfo\":{\"platform\":4,\"product\":61}}";
        Object node = parseJsonToObject(json);

        System.out.println(toJsonWithObject(node));

        String content = "abcd";
        Object abcd = parseJsonToObject(content);
        System.out.println(toJsonWithObject(abcd));

    }
}

