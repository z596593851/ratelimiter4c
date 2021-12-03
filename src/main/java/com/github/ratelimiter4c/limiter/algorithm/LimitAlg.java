package com.github.ratelimiter4c.limiter.algorithm;

public interface LimitAlg {
    boolean tryAcquire();
}
