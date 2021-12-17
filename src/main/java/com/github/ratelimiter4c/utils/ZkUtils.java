package com.github.ratelimiter4c.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;
import java.util.List;

public class ZkUtils {

    public enum NodeType{
        /**
         * 永久节点
         */
        NORMAL,
        /**
         * CONTAINER节点，当其子节点都消失后，自己也会消失
         */
        CONTAINER,
        /**
         * 临时节点
         */
        TEMP,
        ;

    }

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
            nodeList.add(new ZkNode(path,NodeType.NORMAL));
            return this;
        }
        /**
         * 创建CONTAINER节点
         */
        public ZkPathBuild createContainer(String path) throws Exception {
            nodeList.add(new ZkNode(path,NodeType.CONTAINER));
            return this;
        }
        /**
         * 创建临时节点
         */
        public ZkPathBuild createTemp(String path) throws Exception {
            nodeList.add(new ZkNode(path,NodeType.TEMP));
            return this;
        }

        public void build() throws Exception {
            String path="";
            for(ZkNode node:nodeList){
                path=path+node.getPath();
                if(client.checkExists().forPath(path)==null){
                    switch (node.getType()){
                        case NORMAL: client.create().forPath(path);break;
                        case CONTAINER: client.create().withMode(CreateMode.CONTAINER).forPath(path);break;
                        case TEMP: client.create().withMode(CreateMode.EPHEMERAL).forPath(path);break;
                        default:break;
                    }
                }
            }
        }
    }

    public static class ZkNode{
        private final String path;
        private final NodeType type;
        public ZkNode(String path, NodeType type) {
            this.path = path;
            this.type = type;
        }

        public String getPath() {
            return path;
        }

        public NodeType getType() {
            return type;
        }
    }
}
