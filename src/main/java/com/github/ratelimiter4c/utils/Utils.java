package com.github.ratelimiter4c.utils;

import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils {

    public static String getHostAddress() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        return addr.getHostAddress();
    }

    public static String generateUrlKey(String appId, String api, int limit) {
        return appId + ":" + api+":"+limit;
    }

    public static List<String> tokenizeUrlPath(String urlPath) {
        if (StringUtils.isBlank(urlPath)) {
            return Collections.emptyList();
        }

        if (!urlPath.startsWith("/")) {
            throw new IllegalArgumentException("UrlParser tokenize error, invalid urls: " + urlPath);
        }

        String[] dirs = urlPath.split("/");
        List<String> dirlist = new ArrayList<>();
        for (int i = 0; i < dirs.length; ++i) {
            if ((dirs[i].contains(".") || dirs[i].contains("?") || dirs[i].contains("*"))
                    && (!dirs[i].startsWith("{") || !dirs[i].endsWith("}"))) {
                return Collections.emptyList();
            }

            if (!StringUtils.isEmpty(dirs[i])) {
                dirlist.add(dirs[i]);
            }
        }
        return dirlist;
    }
}
