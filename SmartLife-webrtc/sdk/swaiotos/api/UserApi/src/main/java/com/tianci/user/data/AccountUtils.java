/**
 * Copyright (C) 2012 The SkyTvOS Project
 * <p>
 * Version     Date           Author ───────────────────────────────────── 2016年3月28日         wen
 */

package com.tianci.user.data;

import android.text.TextUtils;

import com.tianci.user.api.SkyUserApi;
import com.tianci.user.data.UserCmdDefine.UserKeyDefine;

import java.util.List;
import java.util.Map;

import com.tianci.user.api.utils.JsonUtils;
import com.tianci.user.api.utils.ULog;

/**
 * 账户相关工具类，主要是账户信息处理
 *
 * @author wen 2016年3月28日
 */
public class AccountUtils {
    private static final String TAG = SkyUserApi.TAG + "-Utils";

    private static final String KEY_QQ_LOGIN = "login";
    // private static final String KEY_QQ_NAME = "nick";
    private static final String KEY_QQ_ICON = "face";

    /**
     * 检查账户信息中是否含有手机号
     *
     * @param accountInfo 账号信息
     * @return true：有手机号
     */
    public static boolean containsMobile(Map<String, ?> accountInfo) {
        String mobile = getAccountValue(accountInfo, UserKeyDefine.KEY_ACCOUNT_MOBILE);
        ULog.i(TAG, "containsMobile(), mobile = " + mobile);
        return !TextUtils.isEmpty(mobile);
    }

    /**
     * 从账户信息中获取头像的url地址，只显示酷开账号的头像。2017年3月1日<br/>
     *
     * @param accountInfo 账户信息
     * @return String 头像的url
     */
    public static String getAvatorFromInfo(Map<String, Object> accountInfo) {
        if (accountInfo == null || accountInfo.isEmpty()) {
            ULog.e(TAG, "info is nul or empty, return ");
            return "";
        }
        // 使用最大分辨率的头像图片
        String coocaaIcon = getIconUrl(accountInfo);
        ULog.i(TAG, "Avatar url = " + coocaaIcon);
        return coocaaIcon;
    }

    /**
     * 根据关键字，获取账户信息中对应字段的值。2016年3月28日<br/>
     *
     * @param key 关键字
     * @return String 字段值
     */
    public static String getAccountValue(Map<String, ?> info, String key) {
        String value = "";
        if (info != null && info.containsKey(key)) {
            Object obj = info.get(key);
            if (obj != null) {
                value = obj.toString();
            }
        }

        //        SkyLogger.i(TAG, "getAccountValue, key = " + key + ", value = " + value);
        return value;
    }

    /**
     * 获取账号中已绑定第3方账号的openid
     *
     * @param type 第3方账号的类型
     * @param info 账号信息
     * @return
     */
    public static String getBindOpenId(String type, Map<String, ?> info) {
        Map<String, String> bindInfo = getExtraInfo(info, type);
        if (bindInfo == null || bindInfo.isEmpty()) {
            return null;
        } else {
            //            String thirdOpenId = bindInfo.get(UserKeyDefine.KEY_OPEN_ID);
            String thirdOpenId = bindInfo.get(UserKeyDefine.KEY_EXTERNAL_ID);
            ULog.i(TAG, "getBindOpenId, thirdOpenId = " + thirdOpenId);
            return thirdOpenId;
        }
    }

    /**
     * 根据类型获取第三方账户信息<br/>
     *
     * @param map  账户总信息
     * @param type 第三方账户类型
     * @return Map<String, Object>
     */
    public static Map<String, String> getExtraInfo(Map<String, ?> map, String type) {
        if (TextUtils.isEmpty(type)) {
            ULog.i(TAG, "AccountUtils.getExtraInfo(), type is null or empty");
            return null;
        }

        List<Map<String, String>> list = getExtraInfo(map);
        if (list != null && !list.isEmpty()) {
            for (Map<String, String> submap : list) {
                // System.out.println("get external info, sub: " + submap);
                if (submap != null && type.equals(submap.get("external_flag"))) {
                    return submap;
                }
            }
        }
        return null;
    }

    public static List<Map<String, String>> getExtraInfo(Map<String, ?> map) {
        List<Map<String, String>> list = null;
        if (map != null) {
            // 获取所有绑定的第三方账号信息
            Object extraObj = map.get("external_info");
            ULog.i(TAG, "external info : " + extraObj);
            String extraStr = String.valueOf(extraObj);
            if (extraObj != null && !TextUtils.isEmpty(extraStr)) {
                List<Object> extraList = JsonUtils.toList(extraStr);
                for (Object obj : extraList) {
                    if (obj != null) {
                        list.add(JsonUtils.toMapV0(String.valueOf(obj)));
                    }
                }
//                list = SkyJSONUtil.getInstance()
//                        .parseObject(extraStr, new TypeReference<List<Map<String, String>>>() {
//                        });
            }
        }
        return list;
    }

//    /**
//     * 是否由QQ账户自动生成的账户，根据third_account标志位，1代表自动生成。如果账户已绑定手机号且设置密码，则认为是自动生成。2016年4月20日
//     *
//     * @param info
//     * @return boolean
//     */
//    public static boolean isAutoAccount(Map<String, ?> info) {
//        boolean autoFlag = false;
//        if (info != null && !info.isEmpty()) {
//            Object accountFlagObj = info.get(UserKeyDefine.KEY_ACCOUNT_THIRD);
//            autoFlag =
//                    UserKeyDefine.VALUE_THIRD_ACCOUNT_TRUE.equals(String.valueOf(accountFlagObj));
//            // autoFlag = accountFlagObj != null && String.valueOf(accountFlagObj).equals("1");
//        }
//        ULog.i(TAG, "isAutoAccount, result = " + autoFlag);
//        return autoFlag;
//    }

    /**
     * 从账号信息中获取头像URL，优先使用最大分辨率的图片（f_800），。2016年3月1日
     *
     * @param info
     * @return String
     */
    public static String getIconUrl(Map<String, ?> info) {
        String url = null;
        if (info != null) {
            Object obj = info.get("avatars");
            if (obj != null) {
                Map<String, String> map = JsonUtils.toMapV0(obj.toString());
                if (map != null && !map.isEmpty()) {
                    url = map.get("f_800");
                }
            }

            if (TextUtils.isEmpty(url)) {
                url = getAccountValue(info, UserKeyDefine.KEY_ACCOUNT_AVATAR);
            }
        }
        ULog.i(TAG, " --- getIconUrl = " + url);
        return url;
    }

//    public static UserInfo createUserInfo(Map<String, ?> infoMap, String session) {
//        UserInfo info = new UserInfo();
//        info.address = getAccountValue(infoMap, "address");
//        info.birthday = getAccountValue(infoMap, "birthday");
//        info.email = getAccountValue(infoMap, "email");
//        info.session = session;
//        info.sex = ByteUtil.pasreInt(getAccountValue(infoMap, "gender"));
//        info.signature = getAccountValue(infoMap, "slogan");
//        info.telephoneNo = getAccountValue(infoMap, "mobile");
//        info.userIcon = getAccountValue(infoMap, "avatar");
//        if (TextUtils.isEmpty(info.userIcon)) {
//            if (info.sex == 2) {
//                // 女性头像
//                info.userIcon = "USER_ICON_FEMALE";
//            } else {
//                info.userIcon = "USER_ICON_MALE";
//            }
//        }
//        // info.userId = map.get("sky_id");
//        info.userId = getAccountValue(infoMap, "open_id");
//        info.userNickName = getAccountValue(infoMap, "nick_name");
//        ULog.i(TAG, "createUserInfo:" + info.toPrintString());
//        return info;
//    }
}
