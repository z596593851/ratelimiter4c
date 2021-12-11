package com.github.ratelimiter4c.limiter.rule.source;

import com.github.ratelimiter4c.limiter.rule.AppLimitManager;

import java.util.List;

public class FileLimitSource implements AppLimitSource{
    public static String SOURCE_TYPE="file";
    private final AppLimitManager manager;
    private final AppLimitConfig config;
    public FileLimitSource(AppLimitManager appLimitManager,  AppLimitConfig config){
        this.manager=appLimitManager;
        this.config=config;
    }

    @Override
    public List<AppLimitModel> load() {
        return config.getLimits();
    }

}
