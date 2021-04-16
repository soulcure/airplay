package com.coocaa.tvpi.module.pay.api;

public interface IPayResultCallback {
    void paySuccessed();
    void payFailed(String reason);
    void payCancel();
}
