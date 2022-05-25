package com.leimbag.rabbitmq.consumer.demo.annotation;

import java.lang.annotation.*;

/**
 * @author leimbag
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TraceableMq {
}
