package com.leimbag.demo.core.util;

import java.util.UUID;

/**
 * @author leimbag
 */
public class IdGenerateUtil {
    public static String requestIdWithUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String generateId() {
        return requestIdWithUUID();
    }
}
