package com.github.ratelimiter4c.limiter.algorithm;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SlidingWindowRateLimitAlg implements LimitAlg{
    /**
     * 限流周期(秒)
     */
    private static final int CYCLE =10;
    /**
     * 限流最小周期(秒)
     */
    private static final long MIN_CYCLE = 5;

    /**
     * 每分钟限流请求数
     */
    private final int limit;

    /**
     * 计数器, k-为当前窗口的开始时间值秒，value为当前窗口的计数
     */
    private static final long TRY_LOCK_TIMEOUT = 200L;
    private final Map<Long, Integer> counters = new TreeMap<>();
    private final Lock lock = new ReentrantLock();

    public SlidingWindowRateLimitAlg(int limit){
        this.limit=limit;
    }

    @Override
    public boolean tryAcquire() {
        //获取当前时间在哪个小周期窗口
        long currentWindowTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) / MIN_CYCLE * MIN_CYCLE;
        //计算窗口开始位置
        long startTime = currentWindowTime - MIN_CYCLE* (CYCLE/MIN_CYCLE-1);
        try {
            if (lock.tryLock(TRY_LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                try {
                    //当前窗口总请求数
                    int count = 0;
                    //遍历存储的计数器
                    Iterator<Map.Entry<Long, Integer>> iterator = counters.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Long, Integer> entry = iterator.next();
                        System.out.println("key-"+entry.getKey()+"value-"+entry.getValue());
                        // 删除无效过期的子窗口计数器
                        if (entry.getKey() < startTime) {
                            iterator.remove();
                        } else {
                            //累加当前窗口的所有计数器之和
                            count = count + entry.getValue();
                        }
                    }
                    //超过阀值限流
                    if (count >= limit) {
                        System.out.println(count);
                        return false;
                    }
                    //计数器+1
                    int curr=counters.getOrDefault(currentWindowTime,0);
                    curr++;
                    counters.put(currentWindowTime,curr);
                    return true;
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
