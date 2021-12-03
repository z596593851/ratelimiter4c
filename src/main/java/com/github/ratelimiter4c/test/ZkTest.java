package com.github.ratelimiter4c.test;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.nio.charset.StandardCharsets;

public class ZkTest {
    public static void main(String[] args) throws Exception {

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client= CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181").sessionTimeoutMs(10000).retryPolicy(retryPolicy).build();
        client.start();
//        client.create().forPath("/ratelimiter");
//        client.setData().forPath("/ratelimiter",data.getBytes(StandardCharsets.UTF_8));
        String resut=new String(client.getData().forPath("/ratelimiter/app1"));
        System.out.println("初始数据:"+resut);
        watch(client, "/ratelimiter/app1");
        while (true){

        }

    }

    public static void watch(CuratorFramework curator, String path) throws Exception{
        final NodeCache nodeCache = new NodeCache(curator, path, false);
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                System.out.println("监听的节点为" + nodeCache.getCurrentData().getPath() + "数据变为 : "+new String(nodeCache.getCurrentData().getData()));

            }
        });
        nodeCache.start();
    }

}
