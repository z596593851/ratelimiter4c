package com.github.ratelimiter4c.asm;


import com.github.ratelimiter4c.limiter.UrlRateLimiter;
import com.github.ratelimiter4c.limiter.rule.MemoryUrlRateLimiter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AsmTemplate {
    MemoryUrlRateLimiter limiter=new MemoryUrlRateLimiter();
    protected void service(HttpServletRequest req, HttpServletResponse resp){
        String path=req.getRequestURI();
        System.out.println(path);
        boolean result=limiter.limit(path);
        if(!result){
            System.out.println("被限流");
            return;
        }
    }
}
