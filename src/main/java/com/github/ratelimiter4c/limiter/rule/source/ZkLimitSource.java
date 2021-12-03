package com.github.ratelimiter4c.limiter.rule.source;

import com.github.ratelimiter4c.exception.AsmException;
import com.github.ratelimiter4c.limiter.rule.AppLimitManager;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ZkLimitSource implements AppLimitSource{
    private static final int BASE_SLEEP_TIME_MS = (int) TimeUnit.SECONDS.toMillis(1);
    private static final int MAX_RETRIES = 3;
    private static final int TIMEOUT = (int) TimeUnit.SECONDS.toMillis(3);
    public static String SOURCE_TYPE="zk";
    private final AppLimitManager manager;
    private final AppLimitConfig config;
    private CuratorFramework client;
    public ZkLimitSource(AppLimitManager appLimitManager,  AppLimitConfig config){
        this.manager=appLimitManager;
        this.config=config;
        if(config.getConfigType().equals(SOURCE_TYPE)){
            try {
                RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES);
                this.client= CuratorFrameworkFactory.builder().connectString(config.getZookeeper()).sessionTimeoutMs(10000).retryPolicy(retryPolicy).build();
                client.start();
                boolean connected = client.blockUntilConnected(TIMEOUT, TimeUnit.MILLISECONDS);
                if (!connected) {
                    throw new AsmException("connect zookeeper failed.");
                }
            } catch (Exception e) {
                throw new AsmException(e);
            }
        }
    }
    @Override
    public List<AppLimitModel> load() {
        return null;
    }

    @Override
    public void rebuild(AppLimitConfig config) {

    }
}
