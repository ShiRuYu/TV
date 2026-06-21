package com.github.catvod.utils;

import com.github.catvod.spider.Proxy;

import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

public class MultiThread {

    public static String url(String url, int thread) {
        return String.format(Proxy.getHostPort() + "?do=multi&url=%s&thread=%d", URLEncoder.encode(url), thread);
    }

    public static Object[] proxy(Map<String, String> params) throws Exception {
        throw new UnsupportedOperationException("Multi-threaded download not supported");
    }

    private static Map<String, String> removeHeaders(Map<String, String> headers) {
        throw new UnsupportedOperationException("Multi-threaded download not supported");
    }
}
