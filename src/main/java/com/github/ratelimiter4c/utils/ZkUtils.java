package com.github.ratelimiter4c.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;
import java.util.List;

public class ZkUtils {

    public static ZkPathBuild builder(CuratorFramework client){
        return new ZkPathBuild(client);
    }

    public static void create(String path){

    }


    /**
     * 递进式创建zk节点
     * 如 create("/aaa").create("/bbb") 相当于创建了"/aaa/bbb"
     */
    public static class ZkPathBuild{
        private final CuratorFramework client;
        private final List<ZkNode> nodeList=new ArrayList<>();
        public ZkPathBuild(CuratorFramework client){
            this.client=client;
        }

        /**
         * 创建永久节点
         */
        public ZkPathBuild create(String path) throws Exception {
            nodeList.add(new ZkNode(path,false));
            return this;
        }

        /**
         * 创建临时节点
         */
        public ZkPathBuild createTemp(String path) throws Exception {
            nodeList.add(new ZkNode(path,true));
            return this;
        }

        public void build() throws Exception {
            String path="";
            for(ZkNode node:nodeList){
                path=path+node.getPath();
                if(client.checkExists().forPath(path)==null){
                    if(node.isTemp()){
                        client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
                    }else{
                        client.create().forPath(path);
                    }
                }
            }
        }
    }

    public static class ZkNode{
        private final String path;
        private final boolean isTemp;
        public ZkNode(String path, boolean isTemp) {
            this.path = path;
            this.isTemp = isTemp;
        }

        public String getPath() {
            return path;
        }

        public boolean isTemp() {
            return isTemp;
        }
    }
}
