package com.coocaa.smartscreen.network.exception;

import androidx.annotation.NonNull;

/**
 * 服务器接口定义的错误信息
 * Created by songxing on 2020/3/21
 *
 * SYSTEM_DECRYPT_ERROR(900003, "获取云信token失败，请重试"),
 * ERROR_YX_ACCESSTOKEN(900004, "请求刷新结果失败"),
 * ERROR_UN_REGISTER(900005, "好友还未注册账号"),
 * ERROR_NO_FRIEND(900006, "非法 token"),
 * AUTH_TOKEN_EXPIRED_USER(20121, "非好友关系"),
 * AUTH_TOKEN_EXPIRED_SCREEN(1000, "Token失效,请重新登录")
 */
public class ApiException extends Exception {

    public static final int SYSTEM_DECRYPT_ERROR = 900003;
    public static final int ERROR_YX_ACCESSTOKEN = 900004;
    public static final int ERROR_UN_REGISTER = 900005;
    public static final int ERROR_NO_FRIEND = 900006;
    public static final int ERROR_MOBILE_NO_MOBILE = 900007;
    public static final int ERROR_NO_ITEM_DEAL = 900008;
    public static final int ERROR_NO_ADD_MYSELF = 900009;
    public static final int AUTH_TOKEN_EXPIRED_VIDEO_CALL = 20121;
    public static final int AUTH_TOKEN_EXPIRED_ACCOUNT = 20122;
    public static final int AUTH_TOKEN_EXPIRED_SCREEN = 1000;

    /**
     * 未知错误
     */
    public static final int UNKNOWN = 1000;

    /**
     * 解析错误
     */
    public static final int PARSE_ERROR = 1001;

    /**
     * 网络错误
     */
    public static final int NETWORK_ERROR = 1002;

    private int code;
    private String serverMessage;

    public ApiException(int code, String serverMessage) {
        super(serverMessage);
        this.code = code;
        this.serverMessage = serverMessage;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getServerMessage() {
        return serverMessage;
    }

    public void setServerMessage(String serverMessage) {
        this.serverMessage = serverMessage;
    }

    @NonNull
    @Override
    public String toString() {
        return "code" + code + "  serverMessage" + serverMessage;
    }
}
