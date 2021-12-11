package com.github.ratelimiter4c.limiter.rule;

import com.github.ratelimiter4c.constant.FileAndPathConstant;
import com.github.ratelimiter4c.constant.ValueConstant;
import com.github.ratelimiter4c.exception.AsmException;
import com.github.ratelimiter4c.exception.ZookeeperException;
import com.github.ratelimiter4c.limiter.UrlRateLimiter;
import com.github.ratelimiter4c.limiter.algorithm.LimitAlg;
import com.github.ratelimiter4c.limiter.rule.source.*;
import com.github.ratelimiter4c.monitor.MonitorManager;
import com.github.ratelimiter4c.utils.Utils;
import com.github.ratelimiter4c.utils.ZkUtils;
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

public abstract class AbstractUrlRateLimiter implements UrlRateLimiter{

    private final AppLimitManager appLimitManager;
    private final Map<String, AppLimitSource> appLimitSourceFactory =new HashMap<>();
    private final ConcurrentHashMap<String, LimitAlg> cache = new ConcurrentHashMap<>(256);
    protected volatile AppLimitConfig config;
    private final MonitorManager monitorManager;
    private CuratorFramework client;

    public AbstractUrlRateLimiter() {
        InputStream in=this.getClass().getResourceAsStream(FileAndPathConstant.RATELIMITER_CONFIG_PATH);
        Yaml yaml = new Yaml();
        this.config = yaml.loadAs(in, AppLimitConfig.class);
        this.appLimitManager=new AppLimitManager();
        this.monitorManager=new MonitorManager(config);
        initZk();
        initAppLimitSourceFactory(appLimitManager,config);
        AppLimitSource source = appLimitSourceFactory.get(config.getConfigType());
        if(source == null){
            throw new IllegalArgumentException("未知的配置文件类型");
        }
        appLimitManager.addLimits(config.getAppId(), source.load());
    }

    private void initZk() {
        if(!StringUtils.isBlank(this.config.getZookeeper())){
            try {
                RetryPolicy retryPolicy = new ExponentialBackoffRetry(ValueConstant.ZK_RETRY_SLEEP_TIME_MS, ValueConstant.ZK_RETRY_MAX_TIMES);
                this.client= CuratorFrameworkFactory.builder().connectString(config.getZookeeper()).sessionTimeoutMs(ValueConstant.ZK_SESSION_TIMEOUT_MS).retryPolicy(retryPolicy).build();
                this.client.start();
                //todo 上报节点信息
                boolean connected = client.blockUntilConnected(3000, TimeUnit.MILLISECONDS);
                if (!connected){
                    throw new ZookeeperException("connect zookeeper failed.");
                }
                ZkUtils.builder(client)
                        .create(FileAndPathConstant.RATELIMITER_CONFIG_PATH)
                        .create(FileAndPathConstant.ZK_NODE_PATH)
                        .createTemp(FileAndPathConstant.SPLIT+config.getAppId())
                        .createTemp(FileAndPathConstant.SPLIT+Utils.getHostAddress())
                        .build();
            } catch (Exception e) {
                throw new ZookeeperException(e);
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
    }

    @Override
    public String getReturn(String url) {
        AppLimitModel model = this.appLimitManager.getLimit(config.getAppId(), url);
        if(model!=null){
            return model.getReturnValue();
        }
        return config.getReturnValue();
    }

    public LimitAlg getRateLimiterAlgorithm(String appId, String api, int limit) {
        //todo 这个缓存在使用远程配置，且limit发生变动后，旧的缓存还会一直存在，考虑加入失效策略
        String limitKey = Utils.generateUrlKey(appId, api,limit);
        LimitAlg limitAlg = cache.get(limitKey);
        if (limitAlg == null) {
            System.out.println("新的限流策略:api-"+api+",limit-"+limit);
            LimitAlg newlimitAlg = createRateLimitAlgorithm(limitKey,limit);
            //putIfAbsent是原子性的
            limitAlg = cache.putIfAbsent(limitKey, newlimitAlg);
            if (limitAlg == null) {
                limitAlg = newlimitAlg;
            }
        }
        return limitAlg;
    }


    public abstract LimitAlg createRateLimitAlgorithm(String limitKey, int limit);
}
