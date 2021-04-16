package com.coocaa.tvpi.module.pay.http;

public interface IPayCallback {
    void payDataSuccessCallback(PayData data);
    void payDataErrorCallback(Exception e);
    void payDataParamsErrorCallback(String error);

}
