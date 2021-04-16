package com.coocaa.tvpi.util;

public class NumParseUtil {

    public static float parseFloat(String numStr){
        float num;
        try {
            num = Float.parseFloat(numStr);
        } catch (Exception e) {
            e.printStackTrace();
            num = 0f;
        }
        return num;
    }
}
