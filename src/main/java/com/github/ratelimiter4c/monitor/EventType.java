package com.github.ratelimiter4c.monitor;

public enum EventType {
    /**
     * 所有请求数
     */
    TOTAL,
    /**
     * 限流通过的请求数
     */
    PASSED,
    /**
     * 被限流的请求数
     */
    LIMITED
}
