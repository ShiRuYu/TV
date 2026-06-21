package com.github.catvod.bean.webdav;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Sorter implements Comparator<Object> {

    private final String type;
    private final String order;

    public static void sort(String type, String order, List<Object> items) {
        // TODO: re-enable with DavResource when sardine-android API is confirmed
    }

    public Sorter(String type, String order) {
        this.type = type;
        this.order = order;
    }

    @Override
    public int compare(Object o1, Object o2) {
        return -1;
    }
}
