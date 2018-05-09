package com.example.demo;

import java.lang.annotation.*;

/**
 * Created by dujinkai on 2018/5/9.
 * 限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /**
     * 限制流量的个数（桶里令牌的个数）
     */
    int limit() default 1;

    /**
     * 单位时间
     */
    int timeout() default 1000;
}
