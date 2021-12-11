package com.github.ratelimiter4c;

import com.alibaba.fastjson.JSONObject;
import com.github.ratelimiter4c.limiter.UrlRateLimiter;
import com.github.ratelimiter4c.limiter.rule.DistributedUrlRateLimiter;
import com.github.ratelimiter4c.limiter.rule.MemoryUrlRateLimiter;
import com.github.ratelimiter4c.limiter.rule.source.AppLimitConfig;
import com.github.ratelimiter4c.limiter.rule.source.AppLimitModel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LimitTest {

    @Test
    public void configTest(){
        AppLimitConfig config=new AppLimitConfig();
        config.setAppId("app1");
        List<AppLimitModel> limits=new ArrayList<>();
        AppLimitModel model=new AppLimitModel();
        model.setApi("/test");
        model.setLimit(3);
        limits.add(model);
        config.setLimits(limits);
        System.out.println(JSONObject.toJSONString(config));
    }

    @Test
    public void test(){
        String data="{\"appId\":\"app1\",\"limits\":[{\"api\":\"/test\",\"limit\":3}]}";
        System.out.println(data);
        AppLimitConfig config= JSONObject.parseObject(data,AppLimitConfig.class);
    }

    @Test
    public void limitTest(){

    }

    public static void main(String[] args) {
        UrlRateLimiter limiter=new DistributedUrlRateLimiter();
        limiter.limit("/123");
//        while (true){
//
//        }
    }
}
