package com.github.ratelimiter4c.limiter.rule.source;

import java.util.List;

public class ConfigChangeEvent {

    private String returnValue;
    private List<AppLimitModel> limits;

    public ConfigChangeEvent(){}

    public ConfigChangeEvent(String returnValue, List<AppLimitModel> limits) {
        this.returnValue = returnValue;
        this.limits = limits;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    public List<AppLimitModel> getLimits() {
        return limits;
    }

    public void setLimits(List<AppLimitModel> limits) {
        this.limits = limits;
    }
}
