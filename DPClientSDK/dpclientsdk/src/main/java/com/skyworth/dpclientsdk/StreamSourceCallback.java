package com.skyworth.dpclientsdk;

public interface StreamSourceCallback {
    void onConnectState(ConnectState state);

    //local socket string data
    void onData(String data);


    //local socket bytes data
    void onData(byte[] data);


    void ping(String msg);

    void pong(String msg);
}
