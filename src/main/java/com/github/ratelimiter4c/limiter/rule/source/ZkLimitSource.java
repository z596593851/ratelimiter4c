package com.github.ratelimiter4c.limiter.rule.source;

import com.alibaba.fastjson.JSONObject;
import com.github.ratelimiter4c.exception.AsmException;
import com.github.ratelimiter4c.limiter.rule.AppLimitManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
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

    public ZkLimitSource(AppLimitManager appLimitManager,  AppLimitConfig config, CuratorFramework client){
        this.manager=appLimitManager;
        this.config=config;
        this.client=client;
        try {
            watch();
        } catch (Exception e) {
            throw new AsmException(e);
        }
    }

    @Override
    public List<AppLimitModel> load() {
        try {
            //初始化
            if(client.checkExists().forPath("/ratelimiter")==null){
                client.create().forPath("/ratelimiter");
            }
            if(client.checkExists().forPath("/ratelimiter"+"/"+config.getAppId())==null){
                client.create().forPath("/ratelimiter"+"/"+config.getAppId());
            }
            String data = getZkData();
            if(!StringUtils.isBlank(data)){
                AppLimitConfig config= JSONObject.parseObject(data,AppLimitConfig.class);
                return config.getLimits();
            }
        } catch (Exception e) {
            throw new AsmException(e);
        }
        return null;
    }

    private void watch() throws Exception {
        final NodeCache nodeCache = new NodeCache(client, "/ratelimiter"+"/"+config.getAppId(), false);
        nodeCache.getListenable().addListener(() -> {
            String data=new String(nodeCache.getCurrentData().getData());
            System.out.println("监听的节点为" + nodeCache.getCurrentData().getPath() + "数据变为 : " + data);
            AppLimitConfig config= JSONObject.parseObject(data,AppLimitConfig.class);
            manager.rebuildRule(config);

        });
        nodeCache.start();
    }

    @Override
    public void rebuild(AppLimitConfig config) {
        manager.rebuildRule(config);
    }



    private String getZkData() throws Exception {
        return new String(client.getData().forPath("/ratelimiter/"+config.getAppId()));
    }
}
