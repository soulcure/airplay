package com.skyworth.dpclientsdk;

public interface RequestCallback {
    void onRead(int socketId, String cmd);

    void onRead(int socketId, byte[] data);

    void onConnectState(int socketId, ConnectState state);

    void ping(int socketId, String cmd);

    void pong(int socketId, String cmd);
}
