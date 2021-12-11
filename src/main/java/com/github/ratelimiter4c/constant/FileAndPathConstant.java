package com.github.ratelimiter4c.constant;

public interface FileAndPathConstant {
    String SPLIT="/";
    String RATELIMITER_CONFIG_PATH="/ratelimiter.yaml";
    String ZK_ROOT_PATH="/ratelimiter";
    String ZK_NODE_PATH="/node";
    String ZK_NODE_FULL_PATH=ZK_ROOT_PATH+ZK_NODE_PATH;
    String ZK_CONFIG_PATH="/config";
    String ZK_CONFIG_FULL_PATH=ZK_ROOT_PATH+ZK_CONFIG_PATH;
}
