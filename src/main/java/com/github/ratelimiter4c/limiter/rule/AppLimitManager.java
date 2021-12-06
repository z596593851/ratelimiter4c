package com.github.ratelimiter4c.limiter.rule;


import com.github.ratelimiter4c.limiter.rule.source.AppLimitConfig;
import com.github.ratelimiter4c.limiter.rule.source.AppLimitModel;
import org.apache.commons.lang3.StringUtils;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 给app提供限流规则的CRUD操作，内部使用trie tree存储每个app的限流设置
 */
public class AppLimitManager {

  /**
   * store <appId, limit rules> pairs.
   */
  private volatile ConcurrentHashMap<String, UrlStorage> limitRules =
      new ConcurrentHashMap<>();

  public void addRule(List<AppLimitConfig> appLimitConfigs) {
    for (AppLimitConfig appLimitConfig : appLimitConfigs) {
      String appId = appLimitConfig.getAppId();
      addLimits(appId, appLimitConfig.getLimits());
    }
  }

  public AppLimitModel getLimit(String appId, String urlPath) {
    UrlStorage urlStorage = limitRules.get(appId);
    if (urlStorage == null) {
      return null;
    }
    return urlStorage.getLimitInfo(urlPath);
  }

  public void addLimit(String appId, AppLimitModel appLimitModel) {
    if (StringUtils.isEmpty(appId) || appLimitModel == null) {
      return;
    }

    UrlStorage newTrie = new UrlStorage();
    UrlStorage trie = limitRules.putIfAbsent(appId, newTrie);
    if (trie == null) {
      newTrie.addLimitInfo(appLimitModel);
    } else {
      trie.addLimitInfo(appLimitModel);
    }
  }


  public void addLimits(String appId, List<AppLimitModel> limits){
    if(limits==null || limits.isEmpty()){
      return;
    }
    UrlStorage newTrie = new UrlStorage();
    UrlStorage trie = limitRules.putIfAbsent(appId, newTrie);
    if (trie == null) {
      trie = newTrie;
    }
    for (AppLimitModel appLimitModel : limits) {
      trie.addLimitInfo(appLimitModel);
    }
  }

  public void rebuildRule(AppLimitConfig config) {
    ConcurrentHashMap<String, UrlStorage> newLimitRules = new ConcurrentHashMap<>();
    String appId = config.getAppId();
    UrlStorage urlStorage = new UrlStorage();
    newLimitRules.put(appId, urlStorage);
    for (AppLimitModel appLimitModel : config.getLimits()) {
      urlStorage.addLimitInfo(appLimitModel);
    }
    limitRules = newLimitRules;
  }

}
