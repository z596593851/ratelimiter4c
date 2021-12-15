package com.github.ratelimiter4c.limiter.rule.source;

import com.alibaba.fastjson.JSONObject;
import com.github.ratelimiter4c.constant.FileAndPathConstant;
import com.github.ratelimiter4c.exception.ZookeeperException;
import com.github.ratelimiter4c.limiter.rule.AppLimitManager;
import com.github.ratelimiter4c.utils.ZkUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;

import java.util.List;

public class ZkLimitSource implements AppLimitSource{
    public static String SOURCE_TYPE="zk";
    private final AppLimitManager manager;
    private volatile AppLimitConfig config;
    private final CuratorFramework client;

    public ZkLimitSource(AppLimitManager appLimitManager, AppLimitConfig config, CuratorFramework client){
        this.manager=appLimitManager;
        this.config=config;
        this.client=client;
        try {
            watch();
        } catch (Exception e) {
            throw new ZookeeperException(e);
        }
    }

    @Override
    public List<AppLimitModel> load() {
        try {
            //初始化
            ZkUtils.builder(client)
                    .create(FileAndPathConstant.ZK_ROOT_PATH)
                    .create(FileAndPathConstant.ZK_CONFIG_PATH)
                    .create(FileAndPathConstant.SPLIT+config.getAppId());
            String data = getZkData();
            if(!StringUtils.isBlank(data)){
                AppLimitConfig config= JSONObject.parseObject(data,AppLimitConfig.class);
                return config.getLimits();
            }
        } catch (Exception e) {
            throw new ZookeeperException(e);
        }
        return null;
    }

    private void watch() throws Exception {
        NodeCache nodeCache = new NodeCache(client,
                FileAndPathConstant.ZK_CONFIG_FULL_PATH+FileAndPathConstant.SPLIT+config.getAppId(), false);
        nodeCache.getListenable().addListener(() -> {
            String data=new String(nodeCache.getCurrentData().getData());
            System.out.println("监听的节点为" + nodeCache.getCurrentData().getPath() + "数据变为 : " + data);
            ConfigChangeEvent event= JSONObject.parseObject(data,ConfigChangeEvent.class);
            manager.rebuildRule(config.getAppId(),event);
            //加锁保证线程安全
            rebuildConfig(event);

        });
        nodeCache.start();
    }

    /**
     * 并发度不高，所以直接加锁
     */
    private synchronized void rebuildConfig(ConfigChangeEvent event){
        AppLimitConfig newConfig=new AppLimitConfig();
        newConfig.setAppId(config.getAppId());
        newConfig.setConfigType(config.getConfigType());
        newConfig.setDistributed(config.getDistributed());
        newConfig.setRedis(config.getRedis());
        newConfig.setZookeeper(config.getZookeeper());
        newConfig.setDbUrl(config.getDbUrl());
        newConfig.setDbUsername(config.getDbUsername());
        newConfig.setDbPassword(config.getDbPassword());
        newConfig.setReturnValue(event.getReturnValue());
        newConfig.setLimits(event.getLimits());
        this.config=newConfig;
    }

    private String getZkData() throws Exception {
        return new String(client.getData().forPath(FileAndPathConstant.ZK_CONFIG_FULL_PATH+FileAndPathConstant.SPLIT+config.getAppId()));
    }
}
