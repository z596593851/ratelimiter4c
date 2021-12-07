package com.github.ratelimiter4c.limiter.rule;

import com.github.ratelimiter4c.exception.AsmException;
import com.github.ratelimiter4c.limiter.UrlRateLimiter;
import com.github.ratelimiter4c.limiter.algorithm.LimitAlg;
import com.github.ratelimiter4c.limiter.rule.source.*;
import com.github.ratelimiter4c.monitor.MonitorManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class AbstractUrlRateLimiter implements UrlRateLimiter {

    private final AppLimitManager appLimitManager;
    private final Map<String, AppLimitSource> appLimitSourceFactory =new HashMap<>();
    private final ConcurrentHashMap<String, LimitAlg> cache = new ConcurrentHashMap<>(256);
    protected final AppLimitConfig config;
    private final MonitorManager monitorManager;
    private CuratorFramework client;



    public AbstractUrlRateLimiter() {
        InputStream in=this.getClass().getResourceAsStream("/ratelimiter.yaml");
        Yaml yaml = new Yaml();
        this.config = yaml.loadAs(in, AppLimitConfig.class);
        this.appLimitManager=new AppLimitManager();
        this.monitorManager=new MonitorManager(config);
        initZk();
        initAppLimitSourceFactory(appLimitManager,config);

        AppLimitSource source=appLimitSourceFactory.get(config.getConfigType());
        if(source==null){
            throw new AsmException("未知的配置文件类型");
        }
        appLimitManager.addLimits(config.getAppId(),source.load());
    }

    private void initZk(){
        if(!StringUtils.isBlank(this.config.getZookeeper())){
            try {
                RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
                this.client= CuratorFrameworkFactory.builder().connectString(config.getZookeeper()).sessionTimeoutMs(10000).retryPolicy(retryPolicy).build();
                this.client.start();
                boolean connected = client.blockUntilConnected(3000, TimeUnit.MILLISECONDS);
                if (!connected) {
                    throw new AsmException("connect zookeeper failed.");
                }
            } catch (Exception e) {
                this.client=null;
                throw new AsmException(e);
            }
        }
    }

    private void initAppLimitSourceFactory(AppLimitManager manager, AppLimitConfig config){
        appLimitSourceFactory.put(FileLimitSource.SOURCE_TYPE,new FileLimitSource(manager,config));
        if(this.client!=null){
            appLimitSourceFactory.put(ZkLimitSource.SOURCE_TYPE,new ZkLimitSource(manager,config,this.client));
        }

    }

    @Override
    public boolean limit(String url){
        boolean passed=true;
        LimitAlg limitAlg;
        try {
            AppLimitModel model = this.appLimitManager.getLimit(config.getAppId(), url);
            if(model!=null){
                limitAlg = getRateLimiterAlgorithm(config.getAppId(),url,model.getLimit());
                passed=limitAlg.tryAcquire();
            }
        } finally {
            //监控
            monitorManager.collect(url,passed);
        }
        return passed;
        //todo service返回值可配置化
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
