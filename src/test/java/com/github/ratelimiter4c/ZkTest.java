package com.github.ratelimiter4c;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;


public class ZkTest {
    CuratorFramework client=null;

    @Before
    public void zkInit(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client= CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181").sessionTimeoutMs(10000).retryPolicy(retryPolicy).build();
        client.start();
    }

    @Test
    public void creatNode() throws Exception {
        if(client.checkExists().forPath("/ratelimiter")==null){
            client.create().forPath("/ratelimiter");
        }
        if(client.checkExists().forPath("/ratelimiter/app2")==null){
            client.create().forPath("/ratelimiter/app2");
        }
    }

    @Test
    public void getData() throws Exception {
        String result=new String(client.getData().forPath("/ratelimiter/app2"));
        System.out.println(result==null);
    }

    @Test
    public void setData() throws Exception {
//        String data="{\"appId\":\"app1\",\"limits\":[{\"api\":\"/test\",\"limit\":3}]}";
        String data="{\"appId\":\"app1\",\"limits\":[{\"api\":\"/hello/hi\",\"limit\":1}]}";
        client.setData().forPath("/ratelimiter/app1",data.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void watchTest() throws Exception {
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
