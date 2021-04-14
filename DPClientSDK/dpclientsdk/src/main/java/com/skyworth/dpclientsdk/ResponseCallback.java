package com.skyworth.dpclientsdk;

public interface ResponseCallback {
    void onCommand(String cmd);

    void onCommand(byte[] data);

    void onConnectState(ConnectState state);

    void ping(String cmd);

    void pong(String cmd);
}
