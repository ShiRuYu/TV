package com.github.catvod.utils;

import cn.hutool.core.map.MapUtil;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import com.github.catvod.spider.Proxy;
import okhttp3.Response;

import java.util.HashMap;
import java.util.Map;

public class ProxyVideo {

    public static String buildCommonProxyUrl(String url, Map<String, String> headers) {
        return Proxy.getProxyUrl() + "?do=proxy&url=" + Util.base64Encode(url) + "&header=" + Util.base64Encode(Json.toJson(headers));
    }

    public static String buildAdvanceCommonProxyUrl(String url, Map<String, String> headers, Map<String, String> respHeaders) {
        return Proxy.getProxyUrl() + "?do=advanceProxy&url=" + Util.base64Encode(url) + "&header=" + Util.base64Encode(Json.toJson(headers)) + "&respHeader=" + Util.base64Encode(Json.toJson(respHeaders));
    }

    public static Object[] proxy(String url, Map<String, String> headers) throws Exception {
        return proxy(url, headers, null);
    }

    public static Object[] proxy(String url, Map<String, String> headers, Map<String, String> overrideRespHeaders) throws Exception {
        SpiderDebug.log(" ++start proxy:");
        SpiderDebug.log(" ++proxy url:" + url);
        SpiderDebug.log(" ++proxy header:" + Json.toJson(headers));

        Response response = OkHttp.execute(url, headers);
        SpiderDebug.log(" ++end proxy:");
        SpiderDebug.log(" ++proxy res code:" + response.code());
        SpiderDebug.log(" ++proxy res header:" + Json.toJson(response.headers()));
        String contentType = response.headers().get("Content-Type");
        String contentDisposition = response.headers().get("Content-Disposition");
        if (contentDisposition != null) contentType = getMimeType(contentDisposition);
        Map<String, String> respHeaders = new HashMap<>();
        for (String key : response.headers().names()) {
            respHeaders.put(key, response.headers().get(key));
        }
        if(MapUtil.isNotEmpty(overrideRespHeaders)) respHeaders.putAll(overrideRespHeaders);
        SpiderDebug.log("++proxy res contentType:" + contentType);
        SpiderDebug.log("++proxy res respHeaders:" + Json.toJson(respHeaders));
        return new Object[]{response.code(), contentType, response.body().byteStream(), respHeaders};
    }

    public static Response proxyResponse(String url, Map<String, String> headers) throws Exception {
        SpiderDebug.log("proxy url：" + url + " headers" + Json.toJson(headers));
        return OkHttp.execute(url, headers);
    }

    public static class ProxyRespBuilder {
        public static Object[] redirect(String url) {
            return new Object[]{302, "text/plain", url};
        }

        public static Object[] response(Response response) {
            return new Object[]{response};
        }
    }

    private static String getMimeType(String contentDisposition) {
        if (contentDisposition.endsWith(".mp4")) {
            return "video/mp4";
        } else if (contentDisposition.endsWith(".webm")) {
            return "video/webm";
        } else if (contentDisposition.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (contentDisposition.endsWith(".wmv")) {
            return "video/x-ms-wmv";
        } else if (contentDisposition.endsWith(".flv")) {
            return "video/x-flv";
        } else if (contentDisposition.endsWith(".mov")) {
            return "video/quicktime";
        } else if (contentDisposition.endsWith(".mkv")) {
            return "video/x-matroska";
        } else if (contentDisposition.endsWith(".mpeg")) {
            return "video/mpeg";
        } else if (contentDisposition.endsWith(".3gp")) {
            return "video/3gpp";
        } else if (contentDisposition.endsWith(".ts")) {
            return "video/MP2T";
        } else if (contentDisposition.endsWith(".mp3")) {
            return "audio/mp3";
        } else if (contentDisposition.endsWith(".wav")) {
            return "audio/wav";
        } else if (contentDisposition.endsWith(".aac")) {
            return "audio/aac";
        } else {
            return null;
        }
    }
}
