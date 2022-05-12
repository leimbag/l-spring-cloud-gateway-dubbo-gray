package com.leimbag.gateway.demo.util;

import java.util.UUID;

/**
 * @author leimbag
 */
public class GenerateIdUtil {
    public static String requestIdWithUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String generateId() {
        return requestIdWithUUID();
    }
}
