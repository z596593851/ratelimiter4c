package com.github.ratelimiter4c.limiter.test;


import com.github.ratelimiter4c.limiter.UrlRateLimiter;
import com.github.ratelimiter4c.limiter.rule.MemoryUrlRateLimiter;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        MemoryUrlRateLimiter urlRateLimiter=new MemoryUrlRateLimiter();
        System.out.println(urlRateLimiter.limit("/test1"));
        System.out.println(urlRateLimiter.limit("/test1"));
        System.out.println(urlRateLimiter.limit("/test1"));
        System.out.println(urlRateLimiter.limit("/test1"));
        System.out.println(urlRateLimiter.limit("/test1"));
        Thread.sleep(2000);
        System.out.println(urlRateLimiter.limit("/test1"));
        System.out.println(urlRateLimiter.limit("/test1"));
        System.out.println(urlRateLimiter.limit("/test1"));
        System.out.println(urlRateLimiter.limit("/test1"));
        System.out.println(urlRateLimiter.limit("/test1"));
    }
}
