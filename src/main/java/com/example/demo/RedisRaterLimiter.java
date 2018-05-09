package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.ZParams;

import java.util.List;
import java.util.UUID;

/**
 * redis限流
 */
@Component
public class RedisRaterLimiter {

    private static final String BUCKET = "BUCKET_";

    private static final String BUCKET_COUNT = "BUCKET_COUNT";

    private static final String BUCKET_MONITOR = "BUCKET_MONITOR_";

    /**
     * 注入jedis
     */
    @Autowired
    private JedisPool jedisPool;

    /**
     * 从redis的桶中获取令牌
     *
     * @param jedis   jedis实列
     * @param point   令牌点
     * @param limit   令牌数量
     * @param timeout 时间
     * @return 返回令牌
     */
    public String acquireTokenFromBucket(String point, int limit, long timeout) {

        //UUID令牌
        String token = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        //开启事务
        Transaction transaction = jedisPool.getResource().multi();

        //删除信号量 移除有序集中指定区间(score)内的所有成员 ZREMRANGEBYSCORE key min max
        transaction.zremrangeByScore((BUCKET_MONITOR + point).getBytes(), "-inf".getBytes(), String.valueOf(now - timeout).getBytes());
        //为每个有序集分别指定一个乘法因子(默认设置为 1) 每个成员的score值在传递给聚合函数之前都要先乘以该因子
        ZParams params = new ZParams();
        params.weightsByDouble(1.0, 0.0);
        //计算给定的一个或多个有序集的交集
        transaction.zinterstore(BUCKET + point, params, BUCKET + point, BUCKET_MONITOR + point);

        //计数器自增
        transaction.incr(BUCKET_COUNT);
        List<Object> results = transaction.exec();
        long counter = (Long) results.get(results.size() - 1);

        transaction = jedisPool.getResource().multi();
        //Zadd 将一个或多个成员元素及其分数值(score)加入到有序集当中
        transaction.zadd(BUCKET_MONITOR + point, now, token);
        transaction.zadd(BUCKET + point, counter, token);
        transaction.zrank(BUCKET + point, token);
        results = transaction.exec();
        //获取排名，判断请求是否取得了信号量
        long rank = (Long) results.get(results.size() - 1);
        if (rank < limit) {
            return token;
        } else {
            //没有获取到信号量，清理之前放入redis中垃圾数据
            transaction = jedisPool.getResource().multi();
            //Zrem移除有序集中的一个或多个成员
            transaction.zrem(BUCKET_MONITOR + point, token);
            transaction.zrem(BUCKET + point, token);
            transaction.exec();
        }
        jedisPool.getResource().close();
        return null;
    }
}

