package com.coocaa.statemanager.view;

/**
 * @ Created on: 2020/10/22
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public class Util {

    public static String hideMiddleNum(String num) {
        if (num == null || num.length() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(num);
        if (builder.length() >= 7) {
            builder.replace(3, 7, "****");
        }
        return builder.toString();
    }
}
