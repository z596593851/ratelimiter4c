package com.github.ratelimiter4c.limiter.rule.source;

import java.util.List;

public class AppLimitConfig {
    private String appId;
    private String configType;
    private Boolean distributed;
    private String redis;
    private String zookeeper;
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private List<AppLimitModel> limits;

    public AppLimitConfig() {}

    public AppLimitConfig(String appId, List<AppLimitModel> limits) {
        this.appId = appId;
        this.limits = limits;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public List<AppLimitModel> getLimits() {
        return limits;
    }

    public void setLimits(List<AppLimitModel> limits) {
        this.limits = limits;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getRedis() {
        return redis;
    }

    public void setRedis(String redis) {
        this.redis = redis;
    }

    public String getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(String zookeeper) {
        this.zookeeper = zookeeper;
    }

    public Boolean getDistributed() {
        return distributed;
    }

    public void setDistributed(Boolean distributed) {
        this.distributed = distributed;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }
}
