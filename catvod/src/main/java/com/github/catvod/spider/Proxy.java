package com.github.catvod.spider;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Json;
import com.github.catvod.utils.ProxyVideo;
import com.github.catvod.utils.Util;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Proxy extends Spider {

    private static int port = -1;

    public static Object[] proxy(Map<String, String> params) throws Exception {
        switch (params.get("do")) {
            case "ck":
                return new Object[]{200, "text/plain; charset=utf-8", new ByteArrayInputStream("ok".getBytes("UTF-8"))};
            //case "multi":
            //    try { return MultiThread.proxy(params); } catch (Exception e) { return null; }
            case "ali":
                return Ali.proxy(params);
            case "quark":
                return Quark.proxy(params);
            case "uc":
                return UC.proxy(params);
            case "bili":
                return Bili.proxy(params);
            case "proxy":
                return commonProxy(params);
            case "advanceProxy":
                return advanceProxy(params);
            default:
                return null;
        }
    }

    private static final List<String> keys = Arrays.asList("url", "header", "do", "User-Agent", "Content-Type", "Host");

    private static Object[] commonProxy(Map<String, String> params) throws Exception {
        String url = Util.decodeStr(params.get("url"));
        Map<String, String> header = Json.toMap(Util.decodeStr(params.get("header")));
        header = getHeader(params, header);
        return new Object[]{ProxyVideo.proxyResponse(url, header)};
    }

    private static Object[] advanceProxy(Map<String, String> params) throws Exception {
        String url = Util.decodeStr(params.get("url"));
        Map<String, String> header = Json.toMap(Util.decodeStr(params.get("header")));
        Map<String, String> respHeader = Json.toMap(Util.decodeStr(params.get("respHeader")));
        header = getHeader(params, header);
        respHeader = getHeader(params, respHeader);
        return ProxyVideo.proxy(url, header, respHeader);
    }

    private static Map<String, String> getHeader(Map<String, String> params, Map<String, String> header) {
        if (header == null) header = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!keys.contains(entry.getKey())) header.put(entry.getKey(), entry.getValue());
        }
        return header;
    }

    static void adjustPort() {
        if (Proxy.port > 0) return;
        int pt = 9978;
        while (pt < 10000) {
            try {
                String resp = OkHttp.string("http://127.0.0.1:" + pt + "/proxy?do=ck", null);
                if (resp.equals("ok")) {
                    SpiderDebug.log("Found local server port " + pt);
                    Proxy.port = pt;
                    break;
                }
                pt++;
            } catch (Exception e) {
                SpiderDebug.log("check port exception:" + e.getMessage());
            }
        }
    }

    public static String getHostPort() {
        adjustPort();
        return "http://127.0.0.1:" + port;
    }

    public static String getProxyUrl() {
        return getHostPort() + "/proxy";
    }
}
