package com.leimbag.demo.core.util;

import okhttp3.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author leimbag
 */
public class HttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static final OkHttpClient okHttpClient = new OkHttpClient();

    public static final int DEFAULT_TIMEOUT_SECOND = 15;

    public static String get(String url) throws IOException {
        return get(url, StandardCharsets.UTF_8.name(), DEFAULT_TIMEOUT_SECOND);
    }

    public static String get(String url, String encoding, int timeout) throws IOException {
        return getWithHeaders(url, null, encoding, timeout);
    }

    public static String getWithHeaders(String url, Map<String, String> headerMap, int timeout) throws IOException {
        return getWithHeaders(url, headerMap, StandardCharsets.UTF_8.name(), timeout);
    }

    public static String getWithHeaders(String url, Map<String, String> headerMap, String encoding, int timeout) throws IOException {
        if (url == null || url.isEmpty()) {
            throw new IOException("HttpUtil GET String error : url must be not null");
        }

        if (encoding == null || encoding.isEmpty()) {
            throw new IOException("HttpUtil GET String error : encoding must be not null");
        }

        if (timeout < 0) {
            throw new IOException("HttpUtil GET String error : timeout must be greater than 0");
        }

        OkHttpClient clientWithTimeout = okHttpClient.newBuilder()
                .readTimeout(timeout, TimeUnit.SECONDS)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .build();

        logger.debug("HttpUtil GET String Executing Request : {}", url);

        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (headerMap != null && !headerMap.isEmpty()) {
            headerMap.keySet().forEach(key -> requestBuilder.addHeader(key, headerMap.get(key)));
        }

        Request request = requestBuilder.build();

        try (Response response = clientWithTimeout.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HttpUtil GET String ResponseBody error : " + response);
            }

            ResponseBody responseBody = response.body();
            String result = new String(responseBody.bytes(), encoding);
            logger.debug("HttpUtil GET String ResponseBody : {}", result);
            return result;
        }
    }

    public static String postUrlEncodedForm(String url, Map<String, String> bodyMap) throws IOException {
        return postWithUrlEncodedFormBody(url, null, bodyMap, StandardCharsets.UTF_8.name(), DEFAULT_TIMEOUT_SECOND);
    }

    public static String postWithUrlEncodedFormBody(String url, Map<String, String> headerMap, Map<String, String> bodyMap, String encoding, int timeout) throws IOException {
        if (url == null || url.isEmpty()) {
            throw new IOException("HttpUtil POST UrlEncodedForm error : url must be not null");
        }

        if (encoding == null || encoding.isEmpty()) {
            throw new IOException("HttpUtil POST UrlEncodedForm error : encoding must be not null");
        }

        if (timeout < 0) {
            throw new IOException("HttpUtil POST UrlEncodedForm error : timeout must be greater than 0");
        }

        if (bodyMap == null || bodyMap.isEmpty()) {
            throw new IOException("HttpUtil POST UrlEncodedForm error : bodyMap must be not null");
        }

        OkHttpClient clientWithTimeout = okHttpClient.newBuilder()
                .readTimeout(timeout, TimeUnit.SECONDS)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .build();

        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        bodyMap.keySet().forEach(key -> formBodyBuilder.add(key, bodyMap.get(key)));

        RequestBody formBody = formBodyBuilder.build();

        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (headerMap != null && !headerMap.isEmpty()) {
            headerMap.keySet().forEach(key -> requestBuilder.addHeader(key, headerMap.get(key)));
        }

        Request request = requestBuilder.post(formBody).build();

        logger.debug("HttpUtil POST UrlEncodedForm Executing Request : {}", request.url().toString());
        logger.debug("HttpUtil POST UrlEncodedForm Request body : {}", formBody.toString());

        try (Response response = clientWithTimeout.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HttpUtil POST UrlEncodedForm ResponseBody error : " + response);
            }
            ResponseBody responseBody = response.body();
            String result = new String(responseBody.bytes(), encoding);
            logger.debug("HttpUtil POST UrlEncodedForm ResponseBody : {}", result);
            return result;
        }
    }
}
