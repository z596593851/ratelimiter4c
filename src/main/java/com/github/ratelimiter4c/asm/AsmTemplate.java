package com.github.ratelimiter4c.asm;


import com.github.ratelimiter4c.limiter.UrlRateLimiter;
import com.github.ratelimiter4c.limiter.rule.MemoryUrlRateLimiter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class AsmTemplate {
    MemoryUrlRateLimiter limiter=new MemoryUrlRateLimiter();
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path=req.getRequestURI();
        System.out.println(path);
        boolean result=limiter.limit(path);
        if(!result){
            System.out.println("has been limited");
            resp.setCharacterEncoding("utf-8");
            resp.setContentType("application/json; charset=utf-8");
            PrintWriter writer = resp.getWriter();
            writer.write("has been limited");
            writer.flush();
            writer.close();
            return;
        }
    }
}
