package com.github.catvod.spider;

import com.github.catvod.bean.Result;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Json;
import com.github.catvod.utils.Util;

import org.jsoup.Jsoup;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Aiga extends Spider {

    private final String host = "https://aigua.tv/";
    private final String home = "/video/index";
    public String a = "https://tvapi211.magicetech.com/";
    public String b = "hr_1_1_0/apptvapi/web/index.php";

    @Override
    public String homeContent(boolean filter) throws Exception {
        String get = Jsoup.connect(host).get().html();
        String string = OkHttp.string(host, Util.webHeaders(host));
        return Result.string(new ArrayList<>());
    }

    private Map<String, String> commonParam() {
        HashMap<String, String> map = Json.parseSafe("{\n" +
                "    \"debug\":\"1\",\n" +
                "\"appId\":\"1\",\n" +
                "\"osType\":\"3\",\n" +
                "\"product\":\"4\",\n" +
                "\"sysVer\":\"30\",\n" +
                "\"udid\":\"0A2233445566\",\n" +
                "\"ver\":\"1.1.0\",\n" +
                "\"packageName\":\"com.gzsptv.gztvvideo\",\n" +
                "\"marketChannel\":\"tv\"" +
                "\n" +
                "}", HashMap.class);
        map.put("time", String.valueOf(System.currentTimeMillis() / 1000));
        try {
            getSign(map);
        } catch (Exception ignored) {
        }
        return map;
    }

    private String getSign(Map<String, String> map) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            list.add(entry.getKey() + "=" + entry.getValue());
        }
        Collections.sort(list);
        StringBuilder sb = new StringBuilder();
        sb.append("jI7POOBbmiUZ0lmi");
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) sb.append(list.get(i));
            else sb.append("&").append(list.get(i));
        }
        sb.append("D9ShYdN51ksWptpkTu11yenAJu7Zu3cR");
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            String digestHex = bytesToHex(digest);
            map.put("sign", digestHex);
            return digestHex;
        } catch (Exception e) {
            return "";
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        return super.categoryContent(tid, pg, filter, extend);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        return super.detailContent(ids);
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        return super.searchContent(key, quick);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        return super.playerContent(flag, id, vipFlags);
    }
}
