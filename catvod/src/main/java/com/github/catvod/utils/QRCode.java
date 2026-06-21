package com.github.catvod.utils;

import cn.hutool.core.codec.Base64;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayInputStream;
import java.util.EnumMap;
import java.util.Map;

public class QRCode {

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    public static int[] createQRCode(String contents, int size, int margin) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, margin);
            BitMatrix matrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, Swings.dp2px(size), Swings.dp2px(size), hints);
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = matrix.get(x, y) ? BLACK : WHITE;
                }
            }
            return pixels;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object createBufferedImage(BitMatrix matrix) {
        throw new UnsupportedOperationException("Not supported on Android");
    }

    public static Object getBitmap(String contents, int size, int margin) {
        throw new UnsupportedOperationException("Not supported on Android");
    }

    public static Object base64StringToImage(String strBase64) {
        throw new UnsupportedOperationException("Not supported on Android");
    }
}
