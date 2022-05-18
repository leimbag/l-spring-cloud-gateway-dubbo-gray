package com.leimbag.trace.dubbo.core.context;

/**
 * @author leimbag
 */
public class TraceHeaderHolder {
    private static final ThreadLocal<String> TAG_TRACE_HEADER_HOLDER = new InheritableThreadLocal<>();

    public static String getTraceHeader() {
        return TAG_TRACE_HEADER_HOLDER.get();
    }

    public static void setTraceHeader(String traceHeader) {
        TAG_TRACE_HEADER_HOLDER.set(traceHeader);
    }

    public static void removeTraceHeader() {
        TAG_TRACE_HEADER_HOLDER.remove();
    }
}
