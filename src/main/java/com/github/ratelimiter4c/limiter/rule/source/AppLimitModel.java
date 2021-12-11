package com.github.ratelimiter4c.limiter.rule.source;

/**
 * 限流配置类实体
 */
public class AppLimitModel {

  private static final int DEFAULT_TIME_UNIT = 1; // 1 second
  private String api;
  private int limit;
  private String returnValue;
  private int unit = DEFAULT_TIME_UNIT;

  public AppLimitModel() {}

  public AppLimitModel(String api, int limit, String returnValue) {
    this(api, limit, returnValue, DEFAULT_TIME_UNIT);
  }

  public AppLimitModel(String api, int limit, String returnValue, int unit) {
    this.api = api;
    this.limit = limit;
    this.returnValue=returnValue;
    this.unit = unit;
  }

  public String getApi() {
    return api;
  }

  public void setApi(String url) {
    this.api = url;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public  int getUnit() {
    return unit;
  }

  public void setUnit(int unit) {
    this.unit = unit;
  }

  public String getReturnValue() {
    return returnValue;
  }

  public void setReturnValue(String returnValue) {
    this.returnValue = returnValue;
  }

  @Override
  public String toString() {
    return "AppLimitModel{" +
            "api='" + api + '\'' +
            ", limit=" + limit +
            ", returnValue='" + returnValue + '\'' +
            '}';
  }
}
