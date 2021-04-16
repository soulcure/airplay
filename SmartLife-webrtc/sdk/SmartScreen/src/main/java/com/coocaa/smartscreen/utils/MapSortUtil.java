package com.coocaa.smartscreen.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MapSortUtil {
    /**
     *对集合内的数据按key的字母顺序做排序
     */
    public static <T> List<Map.Entry<String, T>> sortMap(final Map<String, T> map) {
        final List<Map.Entry<String, T>> infos = new ArrayList<>(map.entrySet());

        // 重写集合的排序方法：按字母顺序
        Collections.sort(infos, new Comparator<Map.Entry<String, T>>() {
            @Override
            public int compare(final Map.Entry<String, T> o1, final Map.Entry<String, T> o2) {
                return (o1.getKey().compareTo(o2.getKey()));
            }
        });

        return infos;
    }
}
