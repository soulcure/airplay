package com.coocaa.smartmall.data.api;


import com.alibaba.fastjson.JSONException;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Description:
 * Create by wzh on 2019-11-13
 */
public class HttpThrowable {
    private ERROR error;
    private int errCode;
    private String errMsg;

    public HttpThrowable(Throwable t) {
        if (t instanceof SocketTimeoutException) {
            error = ERROR.SocketTimeoutException;
        } else if (t instanceof SocketException) {
            if (t instanceof ConnectException) {
                error = ERROR.ConnectException;
            } else error = ERROR.SocketException;
        } else if (t instanceof RuntimeException) {
            if (t instanceof JSONException) {
                error = ERROR.ParseException;
            } else error = ERROR.RuntimeException;
        }else if(t instanceof UnknownHostException) {
            error = ERROR.UnknownHostException;
        } else {
            error = ERROR.UNKNOW;
        }
    }

    public HttpThrowable(ERROR error) {
        this.error = error;
    }

    public HttpThrowable(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public int getErrCode() {
        if (error == null) return errCode;
        return error.code;
    }

    public String getErrMsg() {
        if (error == null) return errMsg;
        return error.msg;
    }

    public enum ERROR {
        UNKNOW(-1, "UNKNOW"),
        SocketException(-2, "SocketException"),
        SocketTimeoutException(-3, "SocketTimeoutException"),
        RuntimeException(-4, "RuntimeException"),
        ParseException(-5, "ParseException"),
        ConnectException(-6, "ConnectException"),
        ServerRetunNullException(-7, "ServerRetunNullException"),
        NetworkNotConnected(-8, "NetworkNotConnected"),
        UnknownHostException(-9, "网络已断开");
        private int code;
        private String msg;

        ERROR(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }
}
