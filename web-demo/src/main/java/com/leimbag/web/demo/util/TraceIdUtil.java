package com.leimbag.web.demo.util;

import java.util.UUID;

/**
 * @author leimbag
 */
public class TraceIdUtil {
    public static String getTid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
