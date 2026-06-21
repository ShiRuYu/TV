package com.github.catvod.spider;

import com.github.catvod.crawler.SpiderDebug;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Init {

    private final ExecutorService executor;

    private static class Loader {
        static volatile Init INSTANCE = new Init();
    }

    public static Init get() {
        return Loader.INSTANCE;
    }

    public Init() {
        this.executor = Executors.newFixedThreadPool(5);
    }

    public static void init() {
        SpiderDebug.log("自定义爬虫代码加载成功！");
    }

    public static void execute(Runnable runnable) {
        get().executor.execute(runnable);
    }
}
