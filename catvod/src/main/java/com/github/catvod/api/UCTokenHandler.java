package com.github.catvod.api;

import cn.hutool.crypto.digest.DigestUtil;
import com.github.catvod.bean.uc.Cache;
import com.github.catvod.bean.uc.User;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.*;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UCTokenHandler {
    private static final String CLIENT_ID = "5acf882d27b74502b7040b0c65519aa7";
    private static final String SIGN_KEY = "l3srvtd7p42l0d0x1u8d7yc8ye9kki4d";
    private static final String API_URL = "https://open-api-drive.uc.cn";
    private static final String CODE_API_URL = "http://api.extscreen.com/ucdrive";

    private Map<String, Object> platformStates = new HashMap<>();
    private Map<String, String> addition = new HashMap<>();
    private Map<String, String> conf = new HashMap<>();
    private final Cache cache;

    public File getCache() {
        return Path.cache("uctoken");
    }

    public UCTokenHandler() {
        addition.put("DeviceID", "07b48aaba8a739356ab8107b5e230ad4");
        conf.put("api", API_URL);
        conf.put("clientID", CLIENT_ID);
        conf.put("signKey", SIGN_KEY);
        conf.put("appVer", "1.6.8");
        conf.put("channel", "UCTVOFFICIALWEB");
        conf.put("codeApi", CODE_API_URL);
        cache = Cache.objectFrom(Path.read(getCache()));
    }

    private String generateUUID() {
        return UUID.randomUUID().toString();
    }

    private String generateDeviceID(String timestamp) {
        return Util.md5(timestamp).substring(0, 16);
    }

    private String generateReqId(String deviceID, String timestamp) {
        return Util.md5(deviceID + timestamp).substring(0, 16);
    }

    private String generateXPanToken(String method, String pathname, String timestamp, String key) {
        return DigestUtil.sha256Hex(method + "&" + pathname + "&" + timestamp + "&" + key);
    }

    public String download(String token, String saveFileId) throws Exception {
        SpiderDebug.log("开始下载:" + saveFileId + ";token:" + token);
        String pathname = "/file";
        String timestamp = String.valueOf(new Date().getTime() / 1000 + 1) + "000";
        String deviceID = StringUtils.isAllBlank((String) addition.get("DeviceID")) ? (String) addition.get("DeviceID") : generateDeviceID(timestamp);
        String reqId = generateReqId(deviceID, timestamp);
        String xPanToken = generateXPanToken("GET", pathname, timestamp, (String) conf.get("signKey"));

        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "text/plain;charset=UTF-8");
        headers.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 13; zh-cn; M2004J7AC Build/UKQ1.231108.001) AppleWebKit/533.1 (KHTML, like Gecko) Mobile Safari/533.1");
        headers.put("x-pan-tm", timestamp);
        headers.put("x-pan-token", xPanToken);
        headers.put("x-pan-client-id", (String) conf.get("clientID"));

        Map<String, String> params = new HashMap<>();
        params.put("req_id", reqId);
        params.put("access_token", token);
        params.put("app_ver", (String) conf.get("appVer"));
        params.put("device_id", deviceID);
        params.put("device_brand", "Xiaomi");
        params.put("platform", "tv");
        params.put("device_name", "M2004J7AC");
        params.put("device_model", "M2004J7AC");
        params.put("build_device", "M2004J7AC");
        params.put("build_product", "M2004J7AC");
        params.put("device_gpu", "Adreno (TM) 550");
        params.put("activity_rect", URLEncoder.encode("{}", "UTF-8"));
        params.put("channel", (String) conf.get("channel"));
        params.put("method", "streaming");
        params.put("group_by", "source");
        params.put("fid", saveFileId);
        params.put("resolution", "low,normal,high,super,2k,4k");
        params.put("support", "dolby_vision");

        okhttp3.HttpUrl.Builder urlBuilder = okhttp3.HttpUrl.parse(API_URL + pathname).newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        okhttp3.Request.Builder reqBuilder = new okhttp3.Request.Builder().url(urlBuilder.build());
        reqBuilder.headers(okhttp3.Headers.of(headers));
        okhttp3.Response resp = OkHttp.client().newCall(reqBuilder.build()).execute();
        String result = resp.body().string();
        resp.close();

        JsonObject obj = Json.safeObject(Json.parse(result));
        String downloadUrl = obj.get("data").getAsJsonObject().get("video_info").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
        SpiderDebug.log("uc TV 下载文件内容：" + downloadUrl);
        return downloadUrl;
    }
}
