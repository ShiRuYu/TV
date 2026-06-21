package com.github.catvod.spider;

import android.util.Base64;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.bean.Vod.VodPlayBuilder.PlayUrl;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Image;
import com.github.catvod.utils.Json;
import com.github.catvod.utils.Util;
import com.google.common.net.HttpHeaders;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DUB extends Spider {

    private final String host = "https://tv.gboku.com/";
    private final String cateFormat = "/vodtype/%s.html";
    private final String cateFormat2 = "/vodtype/%s-%s.html";
    private final String signUrl = "static/player/vidjs25.php";
    private final String searchUrl = "/vodsearch/---------------.html?wd=%s&submit=";
    private String referer = "https://www.duboku.tv/";

    @Override
    public String homeContent(boolean filter) throws Exception {
        String result = OkHttp.string(host + "vodtype/2.html", Util.webHeaders("duboku.tv"));
        Document document = Jsoup.parse(result);
        Elements select = document.select("ul.nav-list > li");
        List<Class> classes = new ArrayList<>();
        for (Element element : select) {
            if ("首页".equals(element.text())) continue;
            String href = element.select("a").attr("href");
            String[] parts = href.split("/");
            String lastPart = parts[parts.length - 1];
            String typeId = lastPart.split("\\.")[0];
            classes.add(new Class(typeId, element.text()));
        }
        return Result.string(classes, Collections.emptyList());
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        String url;
        if ("1".equals(pg)) {
            url = String.format(cateFormat, tid);
        } else {
            url = String.format(cateFormat2, tid, pg);
        }
        url = host + url;
        String string = OkHttp.string(url, Util.webHeaders(referer));
        referer = url;
        Document document = Jsoup.parse(string);
        Elements boxList = document.select(".myui-vodlist__box");
        List<Vod> vodList = new ArrayList<>();
        for (Element element : boxList) {
            Vod vod = new Vod();
            Element fa = element.select("a").first();
            if (fa != null) {
                vod.setVodRemarks(fa.text());
                vod.setVodId(fa.attr("href"));
                vod.setVodPic(new Image.UrlHeaderBuilder(fa.attr("data-original")).referer(referer).build());
                vod.setVodName(fa.attr("title"));
            }
            vodList.add(vod);
        }
        return Result.string(vodList);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        String u = host + ids.get(0);
        String string = OkHttp.string(u, Util.webHeaders(referer));
        referer = u;
        Document document = Jsoup.parse(string);
        Elements detail = document.select(".myui-content__detail");
        Vod vod = new Vod();
        vod.setVodName(detail.select(".title").text());
        Elements thumbLink = document.select("div.myui-content__thumb > a.myui-vodlist__thumb");
        vod.setVodPic(host + thumbLink.select("img").attr("src"));
        Elements list = detail.select("div#rating + p.data");
        String text = list.text();
        List<String> textList = Arrays.asList(text.split(" "));
        vod.setVodActor(findPData(detail, "主演"));
        vod.setVodDirector(findPData(detail, "导演"));
        vod.setVodArea(findSome(textList, "地区"));
        vod.setVodYear(findSome(textList, "年份"));
        Vod.VodPlayBuilder vodPlayBuilder = new Vod.VodPlayBuilder();
        Elements vodList = document.select("ul.myui-content__list a");
        List<PlayUrl> urlList = new ArrayList<>();
        for (Element element : vodList) {
            PlayUrl playUrl = new PlayUrl();
            playUrl.flag = "B";
            playUrl.url = element.attr("href");
            playUrl.name = element.text();
            urlList.add(playUrl);
        }
        Vod.VodPlayBuilder.BuildResult buildResult = vodPlayBuilder.append("B", urlList).build();
        vod.setVodPlayFrom(buildResult.vodPlayFrom);
        vod.setVodPlayUrl(buildResult.vodPlayUrl);
        return Result.string(vod);
    }

    private String findPData(Elements detail, String d) {
        return detail.select("p.data:contains(" + d + ")").text().replace(d + "：", "");
    }

    private String findSome(List<String> list, String d) {
        for (String element : list) {
            if (element.startsWith(d)) {
                return element.replace(d + "：", "");
            }
        }
        return "";
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        String string = OkHttp.string(host + String.format(searchUrl, URLEncoder.encode(key, "UTF-8")), Util.webHeaders(referer));
        Document document = Jsoup.parse(string);
        Elements select = document.select("ul#searchList > li");
        List<Vod> vodList = new ArrayList<>();
        for (Element element : select) {
            Vod vod = new Vod();
            Elements textEl = element.select("a.searchKey");
            vod.setVodName(textEl.text());
            vod.setVodId(textEl.attr("href"));
            Elements thumb = element.select(".thumb > a");
            vod.setVodPic(new Image.UrlHeaderBuilder(thumb.attr("data-original")).referer(referer).build());
            vod.setVodRemarks(thumb.select(".tag").text());
            vodList.add(vod);
        }
        return Result.string(vodList);
    }

    @Override
    public String searchContent(String key, boolean quick, String pg) throws Exception {
        return super.searchContent(key, quick, pg);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        String string = OkHttp.string(host + id);
        Pattern pattern = Pattern.compile("var\\s*player_[a-z]{0,4}\\s*=\\s*([^<]+)");
        Matcher matcher = pattern.matcher(string);
        String url = "";
        if (matcher.find()) {
            String jsonStr = matcher.group(1);
            JsonElement parse = Json.parse(jsonStr);
            JsonObject rst = parse.getAsJsonObject();
            int encrypt = rst.get("encrypt").getAsInt();
            String urlStr = rst.get("url").getAsString();
            if (encrypt == 2) {
                url = URLDecoder.decode(new String(Base64.decode(urlStr, Base64.DEFAULT)), "UTF-8");
            } else {
                url = URLDecoder.decode(urlStr, "UTF-8");
            }
        } else {
            SpiderDebug.log("DUB 获取播放链接失败 " + string);
        }
        String signDocument = OkHttp.string(host + signUrl, Util.webHeaders(referer));
        Pattern signRegx = Pattern.compile("encodeURIComponent\\s*\\(\\s*[\"']([^\"']+)[\"']\\s*\\)");
        Matcher signMatcher = signRegx.matcher(signDocument);
        if (signMatcher.find()) {
            String sign = signMatcher.group(1);
            Map<String, String> headers = Util.webHeaders(host);
            headers.put(HttpHeaders.HOST, new URL(url).getHost());
            String m3u = OkHttp.string(url + "?sign=" + URLEncoder.encode(sign, "UTF-8"), headers);
            Pattern m3uRegx = Pattern.compile("/\\d{8}/[A-Za-z0-9]+/hls/index\\.m3u8\\?sign=[A-Za-z0-9+=%]+");
            Matcher m3uMatcher = m3uRegx.matcher(m3u);
            if (m3uMatcher.find()) {
                URL toHttpUrl = new URL(url);
                return Result.get().url(toHttpUrl.getProtocol() + "://" + toHttpUrl.getHost() + "/" + m3uMatcher.group()).string();
            } else {
                SpiderDebug.log("DUB 解析m3u地址失败");
            }
            return Result.get().url(url + "?sign=" + URLEncoder.encode(sign, "UTF-8")).string();
        }
        SpiderDebug.log("DUB 获取签名失败");
        return Result.error("获取播放链接失败");
    }
}
