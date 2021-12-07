package com.github.ratelimiter4c;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AddrTest {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        System.out.println(addr.getHostAddress());
    }
}
