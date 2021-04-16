package com.coocaa.tvpi.module.pay.http;

import java.io.Serializable;

public class PayBaseData implements Serializable {


    private String message;
    private PayData data;
    private String errorCode;
    private boolean success;
    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }

    public void setData(PayData data) {
        this.data = data;
    }
    public PayData getData() {
        return data;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    public String getErrorCode() {
        return errorCode;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    public boolean getSuccess() {
        return success;
    }

}