package com.coocaa.smartscreen.utils;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static String getRandomNumAndChacters(int length) {
        Random random = new Random();
        StringBuffer result = new StringBuffer();
        do {
            for (int i = 0; i < length; i++) {
                boolean b = random.nextBoolean();
                if (b) {
                    int choice = random.nextBoolean() ? 65 : 97; //随机到65：大写字母  97：小写字母
                    result.append((char) (choice + random.nextInt(26)));
                } else {
                    result.append(random.nextInt(10));
                }
            }
        } while (validate(result.toString()));//验证是否为字母和数字的组合
        return result.toString();
    }


    /**
     * 验证产生的随机字母数字组合是否是纯数字或者存字母
     *
     * @param str
     * @return true:纯字母或者纯数字组成；false：不是纯字母或者纯数字组成
     */
    private static boolean validate(String str) {
        Pattern pattern = Pattern.compile("^([0-9]+)|([A-Za-z]+)$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}
