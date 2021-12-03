package com.github.ratelimiter4c.limiter.rule;

import com.github.ratelimiter4c.limiter.algorithm.DistributedTokenBucketRateLimitAlg;
import com.github.ratelimiter4c.limiter.algorithm.LimitAlg;
import com.github.ratelimiter4c.redis.JedisAdapter;

public class DistributedUrlRateLimiter extends AbstractUrlRateLimiter{

    private final JedisAdapter jedis;

    public DistributedUrlRateLimiter(){
        super();
        this.jedis=new JedisAdapter(config.getRedis());
    }
    @Override
    protected LimitAlg createRateLimitAlgorithm(String limitKey, int limit) {
        return new DistributedTokenBucketRateLimitAlg(limitKey,limit,this.jedis);
    }
}
