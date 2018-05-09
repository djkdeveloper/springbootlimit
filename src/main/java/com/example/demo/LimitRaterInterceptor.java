package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 限流拦截器
 */
@Slf4j
@Component
public class LimitRaterInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisRaterLimiter redisRaterLimiter;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        RateLimiter rateLimiter = method.getAnnotation(RateLimiter.class);
        if (rateLimiter != null) {
            if (StringUtils.isEmpty(redisRaterLimiter.acquireTokenFromBucket(method.getName(), rateLimiter.limit(), rateLimiter.timeout()))) {
                throw new RuntimeException("young man slower....");
            }
        }
        return true;
    }

}
