package com.github.catvod.spider;

import android.content.Context;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Json;
import com.github.catvod.utils.ProxyVideo;
import com.github.catvod.utils.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Glod extends Spider {

    private final String host = "https://www.cfkj86.com/";
    private final String detailUrl = host + "api/mw-movie/anonymous/video/detail?id=%s";
    private final String epUrl = "/api/mw-movie/anonymous/v1/video/episode/url?clientType=1&id=%s&nid=%s";
    private final String deviceId = UUID.randomUUID().toString();
    private final List<Class> classList = Class.parseFromFormatStr("\u7535\u5f71=1&\u7535\u89c6\u5267=2&\u7efc\u827a=3&\u52a8\u6f2b=4");
    private final String key = "cb808529bae6b6be45ecfab29a4889bc";
    private String cookie = "";

    @Override
    public void init(Context context) throws Exception {
        cookie = generateWafCookie();
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        String string = OkHttp.string(host, Util.webHeaders("https://www.bing.com"));
        List<Vod> vodList = parseFromJson(string, "home");
        return Result.string(classList, vodList);
    }

    private String generateWafCookie() {
        String uuidPart1 = UUID.randomUUID().toString();
        String uuidPart2 = UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        return uuidPart1 + uuidPart2;
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        String times = String.valueOf(System.currentTimeMillis());
        String string = OkHttp.string(String.format(detailUrl, ids.get(0)), genHeaders("id=" + ids.get(0) + "&key=" + key + "&t=" + times, times));
        JsonObject detail = Json.parse(string).getAsJsonObject();
        JsonObject data = detail.get("data").getAsJsonObject();
        Vod vod = new Vod(ids.get(0), data.get("vodName").getAsString(), data.get("vodPic").getAsString(), data.get("vodClass").getAsString());
        vod.setVodActor(data.get("vodActor").getAsString());
        vod.setVodDirector(data.get("vodDirector").getAsString());
        vod.setVodContent(data.get("vodContent").getAsString());

        JsonArray linkList = data.get("episodeList").getAsJsonArray();
        List<Vod.VodPlayBuilder.PlayUrl> playUrlList = new ArrayList<>();
        for (com.google.gson.JsonElement element : linkList) {
            JsonObject u = element.getAsJsonObject();
            String nid = u.get("nid").getAsString();
            String name = u.get("name").getAsString();
            Vod.VodPlayBuilder.PlayUrl playUrl = new Vod.VodPlayBuilder.PlayUrl();
            playUrl.name = name;
            playUrl.url = ids.get(0) + "/" + nid;
            playUrlList.add(playUrl);
        }

        Vod.VodPlayBuilder builder = new Vod.VodPlayBuilder();
        Vod.VodPlayBuilder.BuildResult buildResult = builder.append("glod", playUrlList).build();
        vod.setVodYear(data.get("vodYear").getAsString());
        vod.setVodPlayFrom(buildResult.vodPlayFrom);
        vod.setVodPlayUrl(buildResult.vodPlayUrl);
        return Result.string(vod);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        String[] parts = id.split("/");
        String i = parts[0];
        String nid = parts[1];
        String time = String.valueOf(System.currentTimeMillis());
        HashMap<String, String> webHeaders = genHeaders("clientType=1&id=" + i + "&nid=" + nid + "&key=" + key + "&t=" + time, time);
        String string = OkHttp.string(host + String.format(epUrl, i, nid), webHeaders);
        JsonObject parse = Json.parse(string).getAsJsonObject();
        if (parse.get("code").getAsInt() != 200) {
            SpiderDebug.log("glod \u83b7\u53d6\u64ad\u653e\u94fe\u63a5\u5931\u8d25:" + string);
            return Result.error("\u83b7\u53d6\u64ad\u653e\u94fe\u63a5\u5931\u8d25");
        }
        String url = parse.get("data").getAsJsonObject().get("playUrl").getAsString();
        String content = OkHttp.string(url, webHeaders);
        return Result.get().url(ProxyVideo.buildCommonProxyUrl(url, webHeaders)).string();
    }

    private HashMap<String, String> genHeaders(String signKey, String time) {
        HashMap<String, String> webHeaders = Util.webHeaders(host);
        try {
            String sign = genSign(signKey);
            webHeaders.put("t", time);
            webHeaders.put("deviceId", deviceId);
            webHeaders.put("Sign", sign);
        } catch (Exception ignored) {
        }
        return webHeaders;
    }

    private String genSign(String key) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] md5Bytes = md5.digest(key.getBytes(StandardCharsets.UTF_8));
        String md5Hex = bytesToHex(md5Bytes);
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Bytes = sha1.digest(md5Hex.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(sha1Bytes);
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
        String url = host + "type/" + tid;
        String string = OkHttp.string(url, Util.webHeaders(host));
        List<Vod> vodList = parseFromJson(string, "cate");
        return Result.string(classList, vodList);
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        String string = OkHttp.string(host + "vod/search/" + key, Util.webHeaders(host));
        List<Vod> vodList = parseFromJson(string, "search");
        return Result.string(vodList);
    }

    private List<Vod> parseFromJson(String string, String type) {
        List<Vod> vodList = new ArrayList<>();
        org.jsoup.nodes.Document parse = Jsoup.parse(string);
        org.jsoup.select.Elements select = parse.select("script");
        Element data = null;
        for (Element script : select) {
            if (script.html().contains("\u64cd\u4f5c\u6210\u529f")) {
                data = script;
                break;
            }
        }
        if (data == null) {
            SpiderDebug.log("glod \u627e\u4e0d\u5230json");
            return vodList;
        }
        String json = data.html().replace("self.__next_f.push(", "").replace(")", "");
        String gson = Json.parse(json).getAsJsonArray().get(1).getAsString().replace("6:", "");
        JsonObject resp = Json.parse(gson).getAsJsonArray().get(3).getAsJsonObject();
        if (type.equals("home")) {
            JsonObject element = resp.get("children").getAsJsonArray().get(3).getAsJsonObject().get("data").getAsJsonObject().get("data").getAsJsonObject();
            JsonArray vList = element.get("homeNewMoviePageData").getAsJsonObject().get("list").getAsJsonArray();
            getVodList(vList, vodList);
            vList = element.get("homeBroadcastPageData").getAsJsonObject().get("list").getAsJsonArray();
            getVodList(vList, vodList);
            vList = element.get("homeManagerPageData").getAsJsonObject().get("list").getAsJsonArray();
            getVodList(vList, vodList);
            vList = element.get("newestTvPageData").getAsJsonObject().get("list").getAsJsonArray();
            getVodList(vList, vodList);
            vList = element.get("newestCartoonPageData").getAsJsonObject().get("list").getAsJsonArray();
            getVodList(vList, vodList);
        } else if (type.equals("cate")) {
            for (com.google.gson.JsonElement jsonElement : resp.get("children").getAsJsonArray().get(3).getAsJsonObject().get("data").getAsJsonArray()) {
                JsonArray objList = jsonElement.getAsJsonObject().get("vodList").getAsJsonObject().get("list").getAsJsonArray();
                getVodList(objList, vodList);
            }
        } else if (type.equals("search")) {
            JsonArray asJsonArray = resp.get("data").getAsJsonObject().get("data").getAsJsonObject().get("result").getAsJsonObject().get("list").getAsJsonArray();
            getVodList(asJsonArray, vodList);
        }
        return vodList;
    }

    private void getVodList(JsonArray objList, List<Vod> vodList) {
        for (com.google.gson.JsonElement oj : objList) {
            JsonObject obj = oj.getAsJsonObject();
            Vod v = new Vod();
            v.setVodId(obj.get("vodId").getAsString());
            v.setVodName(obj.get("vodName").getAsString());
            v.setVodRemarks(obj.get("vodScore").getAsString());
            v.setVodPic(obj.get("vodPic").getAsString());
            vodList.add(v);
        }
    }

    private List<Vod> parseVodList(String string) {
        org.jsoup.nodes.Document parse = Jsoup.parse(string);
        org.jsoup.select.Elements list = parse.select("div.content-card");
        List<Vod> vodList = new ArrayList<>();
        for (Element element : list) {
            String id = element.select("a").attr("href");
            String title = element.select("div.info-title-box > div.title").text();
            String score = element.select("div.bottom div[class^=score]").text();
            String img = element.select("img").attr("srcset");
            vodList.add(new Vod(id, title, host + img, score));
        }
        return vodList;
    }
}
