package com.github.ratelimiter4c.limiter.algorithm;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FixedTimeWindowRateLimitAlg implements LimitAlg {

  private static final long TRY_LOCK_TIMEOUT = 200L;  // 200ms.
  private final Stopwatch stopwatch;
  private final AtomicInteger currentCount = new AtomicInteger(0);

  /* the max permitted access count per second */
  private final int limit;

  private final Lock lock = new ReentrantLock();

  public FixedTimeWindowRateLimitAlg(int limit) {
    this(limit, Stopwatch.createStarted());
  }

  @VisibleForTesting
  protected FixedTimeWindowRateLimitAlg(int limit, Stopwatch stopwatch) {
    this.limit = limit;
    this.stopwatch = stopwatch;
  }


  @Override
  public boolean tryAcquire() {
    int updatedCount = currentCount.incrementAndGet();
    if (updatedCount <= limit) {
      return true;
    }

    try {
      if (lock.tryLock(TRY_LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
        try {
          if (stopwatch.elapsed(TimeUnit.MILLISECONDS) > TimeUnit.SECONDS.toMillis(1)) {
            currentCount.set(0);
            stopwatch.reset();
            stopwatch.start();
            updatedCount = currentCount.incrementAndGet();
          }
          return updatedCount <= limit;
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
