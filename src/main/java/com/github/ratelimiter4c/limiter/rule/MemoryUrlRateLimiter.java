package com.github.ratelimiter4c.limiter.rule;


import com.github.ratelimiter4c.limiter.UrlRateLimiter;
import com.github.ratelimiter4c.limiter.algorithm.LimitAlg;
import com.github.ratelimiter4c.limiter.algorithm.TokenBucketRateLimitAlg;

public class MemoryUrlRateLimiter extends AbstractUrlRateLimiter implements UrlRateLimiter {
    @Override
    protected LimitAlg createRateLimitAlgorithm(String limitKey, int limit) {
        return new TokenBucketRateLimitAlg(limitKey,limit);
    }
}
