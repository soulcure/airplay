/**
 * Copyright (C) 2012 The SkyTvOS Project
 * <p>
 * Version     Date           Author
 * ─────────────────────────────────────
 * 2013-11-28         guiqingwen
 */

package com.tianci.user.data;

import com.tianci.user.api.SkyUserApi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import com.tianci.user.api.utils.ULog;

public class ByteUtil {
//    public static Map<String, Object> getExtraInfo(Map<String, ?> map, AccountType type) {
//        String typeString = type == null ? AccountType.qq.toString() : type.toString();
//        List<Map<String, Object>> list = getExtraInfo(map);
//        if (list != null && !list.isEmpty()) {
//            for (Map<String, Object> submap : list) {
//                System.out.println("get external info, sub: " + submap);
//                if (submap != null && typeString.equals(submap.get("external_flag"))) {
//                    return submap;
//                }
//            }
//        }
//        return null;
//    }
//
//    public static List<Map<String, Object>> getExtraInfo(Map<String, ?> map) {
//        List<Map<String, Object>> list = null;
//        if (map != null) {
//            // 获取所有绑定的第三方账号信息
//            Object extraObj = map.get("external_info");
//            System.out.println("externall info : " + extraObj);
//            String extraStr = String.valueOf(extraObj);
//            if (extraObj != null && !TextUtils.isEmpty(extraStr)) {
//                list = SkyJSONUtil.getInstance()
//                        .parseObject(extraStr, new TypeReference<List<Map<String, Object>>>() {
//                        });
//            }
//        }
//        return list;
//    }

    //    public static String cutRsPathTail(String rsPath)
    //    {
    //        if (rsPath != null && !rsPath.startsWith("http") && getSlashCount(rsPath) == 2)
    //        {
    //            return rsPath.substring(0, rsPath.lastIndexOf("/"));
    //        }
    //        return rsPath;
    //    }
    //
    //    public static int getSlashCount(String rsPath)
    //    {
    //        int count = 0;
    //        // if (rsPath != null)
    //        if (!TextUtils.isEmpty(rsPath))
    //        {
    //            String tmpRsPath = rsPath.replaceAll("/", "");
    //            count = rsPath.length() - tmpRsPath.length();
    //        }
    //        return count;
    //    }
    //
    //    public static boolean isAllType(String type)
    //    {
    //        if (TextUtils.isEmpty(type) || type.equalsIgnoreCase("all"))
    //        {
    //            return true;
    //        }
    //        return false;
    //    }

    public static long parseLong(String str) {
        long time = 0l;
        try {
            time = Long.parseLong(str);
        } catch (Exception e) {
            ULog.e(SkyUserApi.TAG, "parseLong exception = " + e.getMessage());
        }
        return time;
    }

//    public static byte[] getBytes(Object o) {
//        return SkyObjectByteSerialzie.toBytes(o);
//    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> parseList(Class<T> t, byte[] byteData) {
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(byteData);
        ObjectInputStream objInputStream = null;
        try {
            objInputStream = new ObjectInputStream(byteInputStream);
            return (List<T>) objInputStream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getByteFromBool(boolean result) {
        return String.valueOf(result).getBytes();
        //        Boolean b = result;
        //        return b.toString().getBytes();
    }

    public static boolean getBoolFromByte(byte[] data) {
        String sResult = getStringFromBytes(data);
        return parseBoolean(sResult);
    }

    public static int getIntFromByte(byte[] data) {
        String sResult = getStringFromBytes(data);
        ULog.i("ByteUtil", "resutl = " + sResult);
        return pasreInt(sResult);
    }

    public static String getStringFromBytes(byte[] body) {
        if (isEmptyBody(body)) {
            ULog.e("ByteUtil", "body is null");
            return null;
        }
        return new String(body);
    }

    public static boolean isEmptyBody(byte[] body) {
        return body == null || body.length == 0;
        // if (body == null || body.length == 0)
        // {
        // return true;
        // }
        // return false;
    }

    /**
     * 概述：将字符串解析成enum<br/>
     *
     * @param t
     * @param str
     * @return T
     * @date 2013-11-26
     */
    public static <T extends Enum<T>> T parseEnum(Class<T> t, String str) {
        T result = null;
        if (t != null && str != null && !str.equals("")) {
            try {
                result = Enum.valueOf(t, str);
            } catch (Exception e) {
                // ULog.e("ByteUtil", "gqw, exception = " + e.getMessage());
            }
        }
        return result;
    }

    public static boolean parseBoolean(String status) {
        return "true".equals(status);
    }

    public static int pasreInt(String numStr) {
        int number = 0;
        try {
            number = Integer.parseInt(numStr);
        } catch (Exception e) {
            ULog.e("ByteUtil", "gqw-u, Exception = " + e.getMessage());
        }
        return number;
    }

}
