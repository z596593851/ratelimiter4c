package com.github.ratelimiter4c.limiter.algorithm;

import com.github.ratelimiter4c.redis.JedisAdapter;

public class DistributedTokenBucketRateLimitAlg implements LimitAlg{
    /**
     * 每秒处理数（放入令牌数量）
     */
    private final long putTokenRate;
    /**
     * 令牌桶容量
     */
    private final int capacity;

    /**
     * 当前桶内令牌数
     */
    private long currentToken = 0L;
    private JedisAdapter jedis;
    private final String limitKey;
    private static String REDIS_STR=
            "local key = KEYS[1] " +
            "local putTokenRate = tonumber(ARGV[1]) " +
            "local currentTime = tonumber(ARGV[2]) " +
            "local capacity = tonumber(ARGV[3]) " +

            "local bucket = redis.call('hgetall', key) " +
            "local exists = redis.call('exists', key) " +
            "local currentToken " +

            "if exists==0 then " +
            "   redis.call('hset', key, 'refreshTime', currentTime) " +
            "   currentToken = capacity " +
            "   redis.call('hset', key, 'currentToken', currentToken) " +
            "else " +
            "   local refreshTime=tonumber(bucket[2]) " +
            "   currentToken = tonumber(bucket[4]) " +
            "   local generateToken = math.floor((currentTime - refreshTime) / 1000) * putTokenRate " +
            "   if generateToken>0 then " +
            "       currentToken = math.min(capacity, generateToken + currentToken) " +
            "       redis.call('hset', key, 'currentToken', currentToken) " +
            "       redis.call('hset', key, 'refreshTime', currentTime) " +
            "   end " +
            "end " +

            "if currentToken > 0 then " +
            "   redis.call('hset', key, 'currentToken', currentToken-1) " +
            "   return 1 " +
            "else " +
            "   return 0 " +
            "end ";

    public DistributedTokenBucketRateLimitAlg(String limitKey,int capacity,JedisAdapter jedis){
        this.capacity=capacity;
        this.putTokenRate=capacity;
        this.limitKey=limitKey;
        this.jedis=jedis;
    }
    @Override
    public boolean tryAcquire() {
        long result=(long)jedis.eval(REDIS_STR,limitKey,
                String.valueOf(putTokenRate),
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(capacity));
        return result==1;
    }

}
