package com.example.demo;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPool;

/**
 * Created by dujinkai on 2018/5/9.
 * 限制请求
 */
@RestController
@Slf4j
public class LimitController {

    /**
     * 令牌桶每秒钟1个令牌，也就是每秒钟一个请求
     */
    private RateLimiter rateLimiter = RateLimiter.create(1.0);

    /**
     * 注入redis限流
     */
    @Autowired
    private RedisRaterLimiter redisRaterLimiter;


    /**
     * google 限流
     */
    @GetMapping("/limittest")
    public String limitTest() {

        // 获取令牌
        if (rateLimiter.tryAcquire()) {
            log.debug("success....");
            return "success";
        }

        log.error("young man slower.... ");
        return "fail";
    }

    /**
     * redis 全局限流
     */
    @GetMapping("/redislimit")
    public String redisLimit() {
        if (StringUtils.isEmpty(redisRaterLimiter.acquireTokenFromBucket("Test", 1, 5000))) {
            log.error("young man slower.... ");
            return "fail";
        }

        return "success";
    }


    /**
     * redis 方法限流
     */
    @GetMapping("/redismethodlimit")
    @com.example.demo.RateLimiter(limit = 2, timeout = 5000)
    public String redisMethodLimit() {
        return "success";
    }

}
