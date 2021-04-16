package com.coocaa.tvpi.module.local.document;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 2020/11/20
 */
public enum ResultEnum {

    //相关结果码
    SUCCESS(0, "Success"),
    ERROR_UNKNOW(-1, "Unknow"),
    ERROR_EMPTY_URL(-2, "Empty_Url"),
    ERROR_NOT_SUPPORT(-3, "Not_Support"),
    ERROR_FILE_NOT_FOUND(-4, "File_Not_Found"),
    ERROR_TIMEOUT(-5, "SocketTimeout"),
    ERROR_SOCKET(-6, "SocketException"),
    ERROR_CONNECT(-7, "ConnectException"),
    ERROR_UNKNOWNHOST(-8, "UnknownHostException"),
    ERROR_PARSE_JSON(-9, "JSONException"),
    ERROR_RUNTIME(-10, "RuntimeException"),
    ERROR_CUSTOM(-11, "custom_error");

    private int code;
    private String msg;

    ResultEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
