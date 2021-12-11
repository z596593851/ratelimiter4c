package com.github.ratelimiter4c.limiter.algorithm;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TokenBucketRateLimitAlg implements LimitAlg{
    /**
     * 每秒处理数（放入令牌数量）
     */
    private final long putTokenRate;

    /**
     * 最后刷新时间
     */
    private volatile long refreshTime;

    /**
     * 令牌桶容量
     */
    private final int capacity;

    /**
     * 当前桶内令牌数
     */
    private long currentToken = 0L;
    private final Lock lock = new ReentrantLock();
    private static final long TRY_LOCK_TIMEOUT = 200L;

    public TokenBucketRateLimitAlg(int capacity){
        this.capacity=capacity;
        this.putTokenRate=capacity;
    }

    @Override
    public boolean tryAcquire() {
        long currentTime = System.currentTimeMillis();
        try {
            if (lock.tryLock(TRY_LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                try {
                    //生成的令牌 =(当前时间-上次刷新时间)* 放入令牌的速率
                    long generateToken = (currentTime - refreshTime) / 1000 * putTokenRate;
                    // 当前令牌数量 = 之前的桶内令牌数量+放入的令牌数量
                    if(generateToken>0){
                        currentToken = Math.min(capacity, generateToken + currentToken);
                        // 刷新时间
                        refreshTime = currentTime;
                    }
                    //桶里面还有令牌，请求正常处理
                    if (currentToken > 0) {
                        currentToken--; //令牌数量-1
                        return true;
                    }
                    return false;
                } finally {
                    lock.unlock();
                }
            }
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
