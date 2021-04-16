package com.coocaa.tvpi.module.connection.wifi;


public interface WifiConnectCallback {
    void onConnectSuccess();

    void onConnectFail(WifiConnectErrorCode errorCode);
}
