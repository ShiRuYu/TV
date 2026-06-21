package com.github.catvod.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Base64;

import com.github.catvod.Init;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttp;

public class Util {

    public static final String OKHTTP = "okhttp/" + OkHttp.VERSION;
    public static final String CHROME = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36";
    public static final String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7";
    public static final Pattern patternQuark = Pattern.compile("https://pan\\.quark\\.cn/s/[a-zA-Z0-9]+");
    public static final Pattern patternUC = Pattern.compile("https://drive\\.uc\\.cn/s/[a-zA-Z0-9]+");
    public static final int URL_SAFE = Base64.DEFAULT | Base64.URL_SAFE | Base64.NO_WRAP;

    public static String base64(String s) {
        return base64(s.getBytes());
    }

    public static String base64(byte[] bytes) {
        return base64(bytes, Base64.DEFAULT | Base64.NO_WRAP);
    }

    public static String base64(String s, int flags) {
        return base64(s.getBytes(), flags);
    }

    public static String base64(byte[] bytes, int flags) {
        return Base64.encodeToString(bytes, flags);
    }

    public static byte[] decode(String s) {
        return decode(s, Base64.DEFAULT | Base64.NO_WRAP);
    }

    public static byte[] decode(String s, int flags) {
        return Base64.decode(s, flags);
    }

    public static String decodeStr(String s) {
        return new String(decode(s));
    }

    public static byte[] hex2byte(String s) {
        byte[] bytes = new byte[s.length() / 2];
        for (int i = 0; i < bytes.length; i++) bytes[i] = Integer.valueOf(s.substring(i * 2, i * 2 + 2), 16).byteValue();
        return bytes;
    }

    public static boolean equals(String name, String md5) {
        return md5(Path.jar(name)).equalsIgnoreCase(md5);
    }

    public static String md5(String src) {
        try {
            if (TextUtils.isEmpty(src)) return "";
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(src.getBytes(StandardCharsets.UTF_8));
            BigInteger no = new BigInteger(1, bytes);
            StringBuilder sb = new StringBuilder(no.toString(16));
            while (sb.length() < 32) sb.insert(0, "0");
            return sb.toString().toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    public static String md5(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[16384];
            int count;
            while ((count = fis.read(bytes)) != -1) digest.update(bytes, 0, count);
            fis.close();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest.digest()) sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean containOrMatch(String text, String regex) {
        try {
            return text.contains(regex) || text.matches(regex);
        } catch (Exception e) {
            return false;
        }
    }

    public static String substring(String text) {
        return substring(text, 1);
    }

    public static String substring(String text, int num) {
        if (text != null && text.length() > num) return text.substring(0, text.length() - num);
        return text;
    }

    public static String base64Encode(String s) {
        return base64(s);
    }

    public static String base64Encode(byte[] bytes) {
        return base64(bytes);
    }

    public static String base64Decode(String s) {
        return decodeStr(s);
    }

    public static HashMap<String, String> webHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", CHROME);
        return headers;
    }

    public static HashMap<String, String> webHeaders(String referer) {
        HashMap<String, String> headers = webHeaders();
        headers.put("Referer", referer);
        return headers;
    }

    public static HashMap<String, String> webHeaders(String referer, String cookie) {
        HashMap<String, String> headers = webHeaders(referer);
        if (cookie != null && !cookie.isEmpty()) headers.put("Cookie", cookie);
        return headers;
    }

    public static HashMap<String, String> webHeaders(String referer, String cookie, String host) {
        HashMap<String, String> headers = webHeaders(referer, cookie);
        headers.put("Host", host);
        return headers;
    }

    public static boolean isVideoFormat(String url) {
        return url.contains(".mp4") || url.contains(".m3u8") || url.contains(".flv") || url.contains(".mkv");
    }

    public static String getExt(String name) {
        if (name == null) return "";
        int index = name.lastIndexOf(".");
        if (index == -1) return "";
        return name.substring(index + 1);
    }

    public static String fixUrl(String base, String url) {
        if (url.startsWith("http")) return url;
        if (base.endsWith("/")) return base + url;
        return base + "/" + url;
    }

    public static String removeExt(String url) {
        int index = url.lastIndexOf(".");
        if (index == -1) return url;
        return url.substring(0, index);
    }

    public static String getDigit(String text) {
        return text.replaceAll("[^0-9]", "");
    }

    public static String findByRegex(String regex, String content, int group) {
        Matcher m = Pattern.compile(regex).matcher(content);
        return m.find() ? m.group(group) : "";
    }

    public static String stringJoin(String delimiter, Collection<String> items) {
        return String.join(delimiter, items);
    }

    public static String getVar(String html, String key) {
        return findByRegex(key + "\\s*=\\s*['\"]([^'\"]+)['\"]", html, 1);
    }

    public static String getSize(double size) {
        if (size <= 0) return "";
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double s = size;
        while (s >= 1024 && unitIndex < units.length - 1) {
            s /= 1024;
            unitIndex++;
        }
        return String.format("%.2f%s", s, units[unitIndex]);
    }

    public static String MD5(String str) {
        return md5(str);
    }

    public static void notify(String msg) {
    }

    public static String getIp() {
        try {
            String ip = getHostAddress("wlan");
            if (!ip.isEmpty()) return ip;
            ip = getHostAddress("eth");
            if (!ip.isEmpty()) return ip;
            ip = getWifiAddress();
            if (!ip.isEmpty()) return ip;
            return getHostAddress("");
        } catch (Exception e) {
            return "";
        }
    }

    private static String getWifiAddress() {
        WifiManager manager = (WifiManager) Init.context().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int ip = manager.getConnectionInfo().getIpAddress();
        return ip == 0 ? "" : String.format(Locale.getDefault(), "%d.%d.%d.%d", ip & 0xFF, (ip >> 8) & 0xFF, (ip >> 16) & 0xFF, (ip >> 24) & 0xFF);
    }

    private static String getHostAddress(String keyword) throws SocketException {
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface nif = en.nextElement();
            if (!keyword.isEmpty() && !nif.getName().startsWith(keyword)) continue;
            for (Enumeration<InetAddress> addresses = nif.getInetAddresses(); addresses.hasMoreElements(); ) {
                InetAddress addr = addresses.nextElement();
                if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                    return addr.getHostAddress();
                }
            }
        }
        return "";
    }
}
