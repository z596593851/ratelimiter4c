package com.github.ratelimiter4c.limiter.rule;


import com.github.ratelimiter4c.limiter.UrlRateLimiter;
import com.github.ratelimiter4c.limiter.algorithm.LimitAlg;
import com.github.ratelimiter4c.limiter.rule.source.*;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractUrlRateLimiter implements UrlRateLimiter {

    private final AppLimitManager appLimitManager;
    private final Map<String, AppLimitSource> appLimitSourceFactory =new HashMap<>();
    private final ConcurrentHashMap<String, LimitAlg> cache = new ConcurrentHashMap<>(256);
    protected final AppLimitConfig config;



    public AbstractUrlRateLimiter() {
        InputStream in=this.getClass().getResourceAsStream("/ratelimiter.yaml");
        Yaml yaml = new Yaml();
        this.config = yaml.loadAs(in, AppLimitConfig.class);
        this.appLimitManager=new AppLimitManager();
        initAppLimitSourceFactory(appLimitManager,config);
        AppLimitSource source=appLimitSourceFactory.get(config.getConfigType());
        appLimitManager.addLimits(config.getAppId(),source.load());
    }

    private void initAppLimitSourceFactory(AppLimitManager manager, AppLimitConfig config){
        appLimitSourceFactory.put(FileLimitSource.SOURCE_TYPE,new FileLimitSource(manager,config));
        appLimitSourceFactory.put(ZkLimitSource.SOURCE_TYPE,new ZkLimitSource(manager,config));
    }

    @Override
    public boolean limit(String url){
        AppLimitModel model = this.appLimitManager.getLimit(config.getAppId(), url);
        LimitAlg limitAlg=getRateLimiterAlgorithm(config.getAppId(),url,model.getLimit());
        return limitAlg.tryAcquire();
    }

    private LimitAlg getRateLimiterAlgorithm(String appId, String api, int limit){
        String limitKey = generateUrlKey(appId, api);
        LimitAlg limitAlg = cache.get(limitKey);
        if (limitAlg == null) {
            LimitAlg newlimitAlg = createRateLimitAlgorithm(limitKey,limit);
            limitAlg = cache.putIfAbsent(limitKey, newlimitAlg);
            if (limitAlg == null) {
                limitAlg = newlimitAlg;
            }
        }
        return limitAlg;
    }

    private String generateUrlKey(String appId, String api) {
        return appId + ":" + api;
    }

    protected abstract LimitAlg createRateLimitAlgorithm(String limitKey, int limit);
}