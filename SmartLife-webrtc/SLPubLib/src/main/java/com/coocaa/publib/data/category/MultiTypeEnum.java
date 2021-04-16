package com.coocaa.publib.data.category;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IceStorm on 2017/12/29.
 */

public enum MultiTypeEnum {
    // 101:Banner模块,102:双列模块,103:三列模块,104:传统追剧预定模块, 105 搜索横向滑动类型，201:灰色分割线模块
    BANNER   (101),
    COLUMS_1 (104),
    COLUMS_2 (102),
    COLUMS_3 (103),
    COLUMS_4 (202),
    RECYCLER (105),
    DIVIDER  (201);


    // 定义私有变量
    private int value ;

    // 构造函数，枚举类型只能为私有
    private MultiTypeEnum(int value) {
        this.value = value;
    }

    //从int到enum的转换
    private static final Map<Integer, MultiTypeEnum> intToEnum = new HashMap<Integer, MultiTypeEnum>();
    static {
        for (MultiTypeEnum status : values()) {
            intToEnum.put(status.value(), status);
        }
    }

    public static MultiTypeEnum valueOf(int value) {
        return intToEnum.get(value);
    }

    public int value() {
        return this.value;
    }

    @Override
    public String toString() {
        return String.valueOf (this.value);
    }
}
