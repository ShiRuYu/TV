package com.github.catvod.spider;

import android.text.TextUtils;
import android.util.Base64;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Image;
import com.github.catvod.utils.Json;
import com.github.catvod.utils.Util;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BD extends Spider {

    private static String session = "";

    private static String host = Util.decodeStr("aHR0cHM6Ly93d3cueWp5czA1LmNvbS8=");

    private static final List<Class> classList = Class.parseFromFormatStr(Util.decodeStr("5Yqo5L2cPS9zL2Rvbmd6dW8m54ix5oOFPS9zL2FpcWluZybllpzliac9L3MveGlqdSbnp5Hlubs9L3Mva2VodWFuJuaBkOaAlj0vcy9rb25nYnUm5oiY5LqJPS9zL3poYW56aGVuZybmrabkvqA9L3Mvd3V4aWEm6a2U5bm7PS9zL21vaHVhbibliafmg4U9L3MvanVxaW5nJuWKqOeUuz0vcy9kb25naHVhJuaDiuaCmj0vcy9qaW5nc29uZyYzRD0vcy8zRCbngb7pmr49L3MvemFpbmFuJuaCrOeWkT0vcy94dWFueWkm6K2m5YyqPS9zL2ppbmdmZWkm5paH6Im6PS9zL3dlbnlpJumdkuaYpT0vcy9xaW5nY2h1biblhpLpmak9L3MvbWFveGlhbibniq/nvao9L3MvZmFuenVpJue6quW9lT0vcy9qaWx1JuWPpOijhT0vcy9ndXpodWFuZyblpYflubs9L3MvcWlodWFuJuWbveivrT0vcy9ndW95dSbnu7zoibo9L3Mvem9uZ3lpJuWOhuWPsj0vcy9saXNoaSbov5Dliqg9L3MveXVuZG9uZybljp/liJvljovliLY9L3MveXVhbmNodWFuZybnvo7liac9L3MvbWVpanUm6Z+p5YmnPS9zL2hhbmp1JuWbveS6p+eUteinhuWJpz0vcy9ndW9qdSbml6Xliac9L3MvcmlqdSboi7Hliac9L3MveWluZ2p1JuW+t+WJpz0vcy9kZWp1JuS/hOWJpz0vcy9lanUm5be05YmnPS9zL2JhanUm5Yqg5YmnPS9zL2ppYWp1Juilv+WJpz0vcy9zcGFuaXNoJuaEj+Wkp+WIqeWJpz0vcy95aWRhbGlqdSbms7Dliac9L3MvdGFpanUm5riv5Y+w5YmnPS9zL2dhbmd0YWlqdSbms5Xliac9L3MvZmFqdSbmvrPliac9L3MvYW9qdQ=="));

    private static final String ad = Util.decodeStr("enp6eno=");

    private static String reference = host;

    private static final String pattern = Util.decodeStr("dmFyICVzID0gKFxkKyk=");

    private static final String bdFourHost = Util.decodeStr("d3d3LmJkZTQuY2M=");

    private static final String js = Util.decodeStr("djQvanMvc2VhcmNoLWpjYXJvdXNlbC5yZXNwb25zaXZlLmpzOw==");

    private static Map<String, String> webHeaders(String url) {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", Util.CHROME);
        headers.put("Referer", url);
        return headers;
    }

    private static Map<String, String> webHeaders(String url, String cookie) {
        Map<String, String> headers = webHeaders(url);
        if (!TextUtils.isEmpty(cookie)) headers.put("Cookie", cookie);
        return headers;
    }

    private static String getString(String url, Map<String, String> params, Map<String, String> headers) {
        if (!url.startsWith("http")) return "";
        HttpUrl parsed = HttpUrl.parse(url);
        if (parsed == null) return "";
        HttpUrl.Builder urlBuilder = parsed.newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        try (Response res = OkHttp.newCall(urlBuilder.build().toString(), headers).execute()) {
            return res.body().string();
        } catch (Exception e) {
            SpiderDebug.log("BD getString error: " + e.getMessage());
            return "";
        }
    }

    private static String postString(String url, String jsonBody, Map<String, String> headers) {
        try {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonBody, JSON);
            try (Response res = OkHttp.newCall(url, headers, body).execute()) {
                return res.body().string();
            }
        } catch (Exception e) {
            SpiderDebug.log("BD postString error: " + e.getMessage());
            return "";
        }
    }

    public void init(String extend) throws Exception {
        if (!TextUtils.isEmpty(extend)) host = Util.decodeStr(extend);
        SpiderDebug.log("域名：" + host);
        init();
    }

    public void init() throws Exception {
        try (Response res = OkHttp.newCall(host + ad, webHeaders(host)).execute()) {
            if (res.isSuccessful()) {
                String c = res.header("Set-Cookie");
                if (c != null) {
                    String[] split = c.split(";");
                    session = split.length > 1 ? split[0] : c;
                }
            } else {
                SpiderDebug.log("Db初始化失败：" + res);
            }
        }
        SpiderDebug.log("BD init session:" + session);
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        String string = OkHttp.string(host, webHeaders(host, session));
        Document body = Jsoup.parse(string);
        List<Vod> vodList = new ArrayList<>();
        Elements cons = body.select("div[class*=page-header] ~ div > div[class*=row-cards]");
        for (Element con : cons) {
            getVodList(con, vodList);
        }
        return Result.string(classList, vodList);
    }

    private void getVodList(Element con, List<Vod> vodList) {
        Elements cards = con.select("div[class*=card-link]");
        for (Element card : cards) {
            Vod vod = new Vod();
            vod.setVodRemarks(card.select("div > div[class*=ribbon]").text());
            Element cover = card.select("a[class*=cover]").first();
            if (cover != null) {
                vod.setVodId(cover.attr("href"));
                Element img = cover.select("img").first();
                String pic = img != null ? img.attr("data-src") : "";
                if (pic.isEmpty() && img != null) pic = img.attr("src");
                vod.setVodPic(new Image.UrlHeaderBuilder(pic).referer(host).userAgent(Util.CHROME).build());
            }
            vod.setVodName(card.select("h3[class*=card-title]").text());
            vodList.add(vod);
        }
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        String url = host + tid + "/" + pg;
        String string = OkHttp.string(url, webHeaders(host, session));
        Document parse = Jsoup.parse(string);
        Elements cons = parse.select("div[class=card-body]");
        List<Vod> list = new ArrayList<>();
        for (Element con : cons) {
            getVodList(con, list);
        }
        return Result.string(classList, list);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        String id = ids.get(0);
        reference = host + id;
        String string = OkHttp.string(reference, webHeaders(host, session));
        Document document = Jsoup.parse(string);
        Element card = document.select("div.card-body:nth-child(1)").first();
        if (card == null) return "";
        Element head = card.select("div.col").first();
        if (head == null) return "";
        Vod vod = new Vod();
        vod.setVodName(head.select("h1").text());
        Elements spans = head.select("div > span");
        vod.setVodRemarks(spans.size() > 0 ? spans.last().text() : "");
        String img = card.select("div[class*=cover] > img").attr("src");
        vod.setVodPic(img);
        Element detail = card.select("div.card-body > div[class*=row] > div[class*=col]").last();
        if (detail == null) return "";
        String removeDetail = detail.html().replaceAll("</?strong>", "");
        detail = Jsoup.parse(removeDetail);
        Elements pList = detail.select("p");
        String alia = pList.get(0).text();
        vod.setVodTag("");
        vod.setVodId(id);
        if (pList.size() > 1) vod.setVodDirector(pList.get(1).text());
        if (pList.size() > 3) {
            Elements actorLinks = pList.get(3).select("a");
            List<String> actorNames = new ArrayList<>();
            for (Element a : actorLinks) actorNames.add(a.text());
            vod.setVodActor(TextUtils.join(" ", actorNames));
        }
        if (pList.size() > 4) {
            Elements tagLinks = pList.get(4).select("a");
            List<String> tagNames = new ArrayList<>();
            for (Element a : tagLinks) tagNames.add(a.text());
            vod.setVodTag(TextUtils.join(" ", tagNames));
        }
        if (pList.size() > 5) vod.setVodArea(pList.get(5).text());
        if (pList.size() > 7) vod.setVodYear(pList.get(7).text());

        Elements cardBodies = document.select("div.card-body");
        String content = cardBodies.size() > 1 ? cardBodies.get(1).text() : "";
        vod.setVodContent(content + "\n" + alia);

        Elements links = document.select("div#play-list a");
        List<Vod.VodPlayBuilder.PlayUrl> urlList = new ArrayList<>();
        for (Element link : links) {
            Vod.VodPlayBuilder.PlayUrl playUrl = new Vod.VodPlayBuilder.PlayUrl();
            playUrl.url = link.attr("href");
            playUrl.name = link.text();
            urlList.add(playUrl);
        }
        Vod.VodPlayBuilder.BuildResult buildResult = new Vod.VodPlayBuilder().append("线路", urlList).build();
        vod.setVodPlayFrom(buildResult.vodPlayFrom);
        vod.setVodPlayUrl(buildResult.vodPlayUrl);

        return Result.string(vod);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        String s = host + id;
        String string = OkHttp.string(s, webHeaders(reference));
        Document doc = Jsoup.parse(string);
        Element sc = doc.select("script:containsData(pid)").first();
        String pid = "";
        if (sc != null) {
            String html = sc.html();
            String idRegex = String.format(pattern, "pid");
            Matcher matcher = Pattern.compile(idRegex).matcher(html);
            if (matcher.find()) pid = matcher.group(1);
        }
        String time = String.valueOf(System.currentTimeMillis());
        Map<String, String> map = new HashMap<>();
        map.put("t", time);
        map.put("pid", pid);
        map.put("sg", getSg(pid, time));
        String resp = getString(host + "lines", map, webHeaders(reference));
        Resp res = new Gson().fromJson(resp, Resp.class);
        SpiderDebug.log("BD lines res:" + resp);
        if (res.code != 0) {
            SpiderDebug.log("Bd 播放失败");
            return "";
        }
        List<String> urlList = new ArrayList<>();
        if (res.data != null && !TextUtils.isEmpty(res.data.url3)) {
            String[] parts = res.data.url3.split(",");
            for (String part : parts) urlList.add(part);
        }
        if (res.data != null && !TextUtils.isEmpty(res.data.tos)) {
            urlList.add(host + "god/" + pid + "?type=1");
        }
        if (res.data != null && !TextUtils.isEmpty(res.data.m3u8)) {
            urlList.add(res.data.m3u8);
        }
        if (res.data != null && !TextUtils.isEmpty(res.data.ptoken)) {
            urlList.add(host + "god/" + pid);
        }
        Result.UrlBuilder urlBuilder = new Result.UrlBuilder();
        HttpUrl httpUrl = HttpUrl.parse(host);
        String hostOnly = httpUrl != null ? httpUrl.host() : "";
        for (int i = 0; i < urlList.size(); i++) {
            String url = urlList.get(i).replace(bdFourHost, hostOnly);
            String[] split = url.split("#");
            if (split.length > 1) {
                urlBuilder.add(split[1], buildUrl(split[0], pid, session, s));
            } else {
                urlBuilder.add(String.valueOf(i), buildUrl(url, pid, session, s));
            }
        }
        Map<String, String> header = new HashMap<>();
        header.put("Referer", s);
        header.put("Cookie", session);
        return Result.get().url(urlBuilder.build()).header(header).string();
    }

    private String buildUrl(String url, String id, String session, String ref) {
        return com.github.catvod.spider.Proxy.getProxyUrl() + "?do=bd&url=" + Util.base64(url.getBytes()) + "&id=" + Util.base64(id.getBytes()) + "&session=" + Util.base64(session.getBytes()) + "&ref=" + Util.base64(ref.getBytes());
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        String url = host + "search/" + URLEncoder.encode(key, "UTF-8");
        AtomicReference<String> string = new AtomicReference<>(OkHttp.string(url, webHeaders(host, session)));
        if (string.get().contains("验证码")) {
            String jsUrl = host + js + (session.contains(";") ? session.split(";")[0] : session);
            OkHttp.string(jsUrl, webHeaders(url));
            byte[] picData = getVerifyCodePic(url);
            if (picData.length > 0) {
                SpiderDebug.log("BD 验证码图片已获取，使用预设值继续");
                string.set(OkHttp.string(url + "?code=888", webHeaders(url, session)));
            } else {
                SpiderDebug.log("BD 获取验证码失败");
                return "";
            }
        }
        SpiderDebug.log("req end:" + string.get());
        Document parse = Jsoup.parse(string.get());
        List<Vod> vodList = new ArrayList<>();
        Elements items = parse.select("div[class*=row-cards] div[class*=row]");
        for (Element item : items) {
            String pic = item.select("div[class*=row] > a > img").attr("src");
            Element head = item.select("a[class*=search]").first();
            String vid = "";
            String name = "";
            if (head != null) {
                vid = head.attr("href");
                name = head.text();
            }
            Vod vod = new Vod(vid, name, new Image.UrlHeaderBuilder(pic).referer(url).build());
            vodList.add(vod);
        }
        return Result.string(vodList);
    }

    private byte[] getVerifyCodePic(String url) {
        String codeUrl = host + "search/verifyCode?t=" + System.currentTimeMillis();
        Response resp = null;
        try {
            resp = OkHttp.newCall(codeUrl, webHeaders(url, session)).execute();
            return resp.body() != null ? resp.body().bytes() : new byte[0];
        } catch (Exception e) {
            SpiderDebug.log("BD 请求验证码出错:" + e.getMessage());
            return new byte[0];
        } finally {
            if (resp != null) resp.close();
        }
    }

    public Object[] proxyLocal(Map<String, String> params) {
        try {
            String url = Util.decodeStr(params.get("url"));
            String id = !TextUtils.isEmpty(params.get("id")) ? Util.decodeStr(params.get("id")) : "";
            String cookie = !TextUtils.isEmpty(params.get("session")) ? Util.decodeStr(params.get("session")) : "";
            if (url.contains("god")) {
                String t = String.valueOf(System.currentTimeMillis());
                Map<String, String> p = new HashMap<>();
                p.put("t", t);
                p.put("sg", getSg(id, t));
                p.put("verifyCode", "888");
                String body = postString(host + "god", Json.get().toJson(p), webHeaders(host, cookie));
                SpiderDebug.log("DB god req:" + body);
                url = Json.get().fromJson(body, com.google.gson.JsonObject.class).get("url").getAsString();
            }
            return new Object[]{302, "text/plain", url};
        } catch (Exception e) {
            SpiderDebug.log("bd proxy error: " + e.getMessage());
            e.printStackTrace();
        }
        return new Object[0];
    }

    public static String getSg(String id, String time) throws Exception {
        String s = id + "-" + time;
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        String md5Hex = sb.toString();
        String keyStr = md5Hex.substring(0, 16);
        byte[] keyBytes = keyStr.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(s.getBytes(StandardCharsets.UTF_8));
        String encoded = Base64.encodeToString(encrypted, Base64.DEFAULT | Base64.NO_WRAP);
        return base64ToHex(encoded);
    }

    private static String base64ToHex(String s) {
        byte[] decodedBytes = Base64.decode(s, Base64.DEFAULT | Base64.NO_WRAP);
        StringBuilder hexString = new StringBuilder();
        for (byte b : decodedBytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase(Locale.ROOT);
    }

    public static class Resp {
        @SerializedName("code")
        public int code;
        @SerializedName("data")
        public Data data;
        @SerializedName("msg")
        public String msg;
    }

    public static class Data {
        @SerializedName("m3u8")
        public String m3u8;
        @SerializedName("m3u8_2")
        public String m3u8_2;
        @SerializedName("ptoken")
        public String ptoken;
        @SerializedName("tos")
        public String tos;
        @SerializedName("url3")
        public String url3;
    }
}
