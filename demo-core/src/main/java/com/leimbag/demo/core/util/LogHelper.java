package com.leimbag.demo.core.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author leimbag
 */
public class LogHelper {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static Logger logger = LoggerFactory.getLogger(LogHelper.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    static {
        objectMapper.setDateFormat(DATE_FORMAT);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
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

    public static void main(String[] args) {

    }
}

