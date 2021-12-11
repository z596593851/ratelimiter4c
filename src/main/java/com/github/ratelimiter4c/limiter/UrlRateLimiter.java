package com.github.ratelimiter4c.limiter;

public interface UrlRateLimiter {
    boolean limit(String url);
    String getReturn(String url);
}
