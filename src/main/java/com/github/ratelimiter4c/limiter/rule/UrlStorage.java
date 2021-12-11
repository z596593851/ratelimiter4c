package com.github.ratelimiter4c.limiter.rule;


import com.github.ratelimiter4c.limiter.rule.source.AppLimitModel;
import com.github.ratelimiter4c.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Trie tree，存储app的 {@link AppLimitModel}
 */
public class UrlStorage {

  private final Node root;

  public UrlStorage() {
    root = new Node("/");
  }

  public void addLimitInfo(AppLimitModel appLimitModel){
    String urlPath = appLimitModel.getApi();
    if (!urlPath.startsWith("/")) {
      throw new IllegalArgumentException("the api is invalid: " + urlPath);
    }

    if (appLimitModel.getApi().equals("/")) {
      root.setApiLimit(appLimitModel);
      return;
    }

    List<String> pathDirs = Utils.tokenizeUrlPath(appLimitModel.getApi());
    if (pathDirs.isEmpty()) {
      return;
    }

    Node p = root;
    for (String pathDir : pathDirs) {
      ConcurrentHashMap<String, Node> children = p.getEdges();

      String pathDirPattern = pathDir;
      boolean isPattern = false;
      //处理通配符
      if (isUrlTemplateVariable(pathDir)) {
        pathDirPattern = getPathDirPatten(pathDir);
        isPattern = true;
      }
      Node newNode = new Node(pathDirPattern, isPattern);
      Node existedNode = children.putIfAbsent(pathDirPattern, newNode);
      if (existedNode != null) {
        p = existedNode;
      } else {
        p = newNode;
      }
    }
    p.setApiLimit(appLimitModel);
  }

  public void addLimitInfos(Collection<AppLimitModel> appLimitModelList) throws Exception {
    for (AppLimitModel appLimitModel : appLimitModelList) {
      addLimitInfo(appLimitModel);
    }
  }

  /**
   * 深层有对应的限流策略，则优先用深层的；深层没有则用最近的浅层的限流策略。
   */
  public AppLimitModel getLimitInfo(String urlPath) {
    if (StringUtils.isBlank(urlPath)) {
      return null;
    }

    if (urlPath.equals("/")) {
      return root.getApiLimit();
    }

    List<String> pathDirs = Utils.tokenizeUrlPath(urlPath);
    if (pathDirs.isEmpty()) {
      return null;
    }

    Node p = root;
    AppLimitModel currentLimit = null;
    if (p.getApiLimit() != null) {
      currentLimit = p.getApiLimit();
    }
    for (String pathDir : pathDirs) {
      ConcurrentHashMap<String, Node> children = p.getEdges();
      Node matchedNode = children.get(pathDir);
      if (matchedNode == null) {
        for (Map.Entry<String, Node> entry : children.entrySet()) {
          Node n = entry.getValue();
          if (n.getIsPattern()) {
            boolean matched = Pattern.matches(n.getPathDir(), pathDir);
            if (matched) {
              matchedNode = n;
            }
          }
        }
      }

      if (matchedNode != null) {
        p = matchedNode;
        if (matchedNode.getApiLimit() != null) {
          currentLimit = matchedNode.getApiLimit();
        }
      } else {
        break;
      }
    }

    return currentLimit;
  }

  public static final class Node {

    private String pathDir;

    private boolean isPattern;

    private final ConcurrentHashMap<String, Node> edges = new ConcurrentHashMap<>();

    private AppLimitModel appLimitModel;

    public Node() {}

    public Node(String pathDir) {
      this(pathDir, false);
    }

    public Node(String pathDir, boolean isPattern) {
      this.pathDir = pathDir;
      this.isPattern = isPattern;
    }

    public void setIsPattern(boolean isPattern) {
      this.isPattern = isPattern;
    }

    public boolean getIsPattern() {
      return isPattern;
    }

    public String getPathDir() {
      return pathDir;
    }

    public void setPathDir(String pathDir) {
      this.pathDir = pathDir;
    }

    public ConcurrentHashMap<String, Node> getEdges() {
      return edges;
    }

    public AppLimitModel getApiLimit() {
      return appLimitModel;
    }

    public void setApiLimit(AppLimitModel appLimitModel) {
      this.appLimitModel = appLimitModel;
    }
  }

  private boolean isUrlTemplateVariable(String pathDir) {
    return pathDir.startsWith("{") && pathDir.endsWith("}");
  }

  private String getPathDirPatten(String pathDir) {
    StringBuilder patternBuilder = new StringBuilder();
    int colonIdx = pathDir.indexOf(':');
    if (colonIdx == -1) {
      patternBuilder.append("(^[0-9]*$)"); // default url template variable pattern: ID
    } else {
      String variablePattern = pathDir.substring(colonIdx + 1, pathDir.length() - 1);
      patternBuilder.append('(');
      patternBuilder.append(variablePattern);
      patternBuilder.append(')');
    }
    return patternBuilder.toString();
  }

}
