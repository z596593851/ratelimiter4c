package com.github.ratelimiter4c.utils;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UrlUtils {

  public static List<String> tokenizeUrlPath(String urlPath) {
    if (StringUtils.isBlank(urlPath)) {
      return Collections.emptyList();
    }

    if (!urlPath.startsWith("/")) {
      throw new RuntimeException("UrlParser tokenize error, invalid urls: " + urlPath);
    }

    String[] dirs = urlPath.split("/");
    List<String> dirlist = new ArrayList<>();
    for (int i = 0; i < dirs.length; ++i) {
      if ((dirs[i].contains(".") || dirs[i].contains("?") || dirs[i].contains("*"))
          && (!dirs[i].startsWith("{") || !dirs[i].endsWith("}"))) {
        return Collections.emptyList();
//        throw new RuntimeException("UrlParser tokenize error, invalid urls: " + urlPath);
      }

      if (!StringUtils.isEmpty(dirs[i])) {
        dirlist.add(dirs[i]);
      }
    }
    return dirlist;
  }

  public static String getUrlPath(String url) throws Exception {
    if (StringUtils.isBlank(url)) {
      return null;
    }

    URI urlObj = null;
    try {
      urlObj = new URI(url);
    } catch (URISyntaxException e) {
      throw new Exception("Get url path error: " + url, e);
    }

    String path = urlObj.getPath();
    if (path.isEmpty()) {
      return "/";
    }
    return path;
  }

  public static boolean validUrl(String url) {
    if (StringUtils.isBlank(url)) {
      return false;
    }
    try {
      new URL(url);
      return true;
    } catch (MalformedURLException e) {
      return false;
    }
  }

}
