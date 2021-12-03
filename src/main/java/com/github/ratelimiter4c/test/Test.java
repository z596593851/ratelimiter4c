package com.github.ratelimiter4c.test;


import com.github.ratelimiter4c.limiter.UrlRateLimiter;
import com.github.ratelimiter4c.limiter.rule.MemoryUrlRateLimiter;

public class Test {

    public Test(String name){
        name="123";
    }

    public void test(){

    }

    public static void main(String[] args) {
        UrlRateLimiter limiter=new MemoryUrlRateLimiter();
        System.out.println(limiter.limit("/test1"));
    }
}
