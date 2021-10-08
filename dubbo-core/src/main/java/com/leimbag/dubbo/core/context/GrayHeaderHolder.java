package com.leimbag.dubbo.core.context;

/**
 * @author leimbag
 */
public class GrayHeaderHolder {
    private static final ThreadLocal<String> TAG_GRAY_HEADER_HOLDER = new InheritableThreadLocal<>();

    public static String getGrayHeader() {
        return TAG_GRAY_HEADER_HOLDER.get();
    }

    public static void setGrayHeader(String grayHeader) {
        TAG_GRAY_HEADER_HOLDER.set(grayHeader);
    }

    public static void removeGrayHeader() {
        TAG_GRAY_HEADER_HOLDER.remove();
    }
}
