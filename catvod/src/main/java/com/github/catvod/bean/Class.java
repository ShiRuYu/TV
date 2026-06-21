package com.github.catvod.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Class {

    @SerializedName("type_id")
    private String typeId;
    @SerializedName("type_name")
    private String typeName;
    @SerializedName("type_flag")
    private String typeFlag;

    public static List<Class> arrayFrom(String str) {
        Type listType = new TypeToken<List<Class>>() {
        }.getType();
        return new Gson().fromJson(str, listType);
    }

    public Class(String typeId) {
        this(typeId, typeId);
    }

    public Class(String typeId, String typeName) {
        this(typeId, typeName, null);
    }

    public Class(String typeId, String typeName, String typeFlag) {
        this.typeId = typeId;
        this.typeName = typeName;
        this.typeFlag = typeFlag;
    }

    public String getTypeId() {
        return typeId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Class)) return false;
        Class it = (Class) obj;
        return getTypeId().equals(it.getTypeId());
    }

    public static List<Class> parseFromFormatStr(String formatStr) {
        ArrayList<Class> list = new ArrayList<>();
        String[] split = formatStr.split("&");
        for (String s : split) {
            String[] strings = s.split("=");
            if (strings.length == 2) {
                list.add(new Class(strings[1], strings[0]));
            }
        }
        return list;
    }

    public static String listToFormatStr(List<Class> list) {
        List<String> li = new ArrayList<>();
        for (Class aClass : list) {
            li.add(aClass.typeName + "=" + aClass.typeId);
        }
        return String.join("&", li);
    }
}
