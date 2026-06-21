package com.github.catvod.utils;

public class Swings {

    public static final int resolution = 160;
    public static final int screenSize = 0;

    private static final int lenToEdge = dp2px(35);

    private static final int BottomBarHeight = dp2px(78);

    public static int dp2px(int dp) {
        return dp * resolution / 160;
    }

    public static Object screenRightDown(int winWidth, int winHeight) {
        throw new UnsupportedOperationException("Not supported on Android");
    }

    public static Object screenRightCenter(int winWidth, int winHeight) {
        throw new UnsupportedOperationException("Not supported on Android");
    }

    public static Object getCenter(int winWidth, int winHeight) {
        throw new UnsupportedOperationException("Not supported on Android");
    }
}

interface WINDOW_LOCATION {
    int CENTER = 0;
    int RIGHT_DOWN = 1;
    int RIGHT_CENTER = 2;
    int RIGHT_UP = 3;
}
