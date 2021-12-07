package com.github.ratelimiter4c.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Utils {

    public static String getHostAddress() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        return addr.getHostAddress();
    }
}
