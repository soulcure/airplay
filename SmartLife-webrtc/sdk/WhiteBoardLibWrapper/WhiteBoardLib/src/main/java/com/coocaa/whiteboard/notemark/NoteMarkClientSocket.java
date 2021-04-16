package com.coocaa.whiteboard.notemark;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.whiteboard.config.WhiteBoardConfig;
import com.coocaa.whiteboard.server.WhiteBoardServerCmdInfo;

import swaiotos.sensor.client.SensorClient;
import swaiotos.sensor.client.data.ClientBusinessInfo;
import swaiotos.sensor.connect.IConnectCallback;
import swaiotos.sensor.data.AccountInfo;

public enum NoteMarkClientSocket {

    INSTANCE;

    private SensorClient client;
    private ClientBusinessInfo clientBusinessInfo;

    private WhiteBoardServerCmdInfo initSyncData;

    private static String serverAddress;

    final static String TAG = "NMClient";

    NoteMarkClientSocket(){
    }

    public void init(Context context, AccountInfo accountInfo) {
        init(context, accountInfo, WhiteBoardConfig.NOTE_CLIENT_SS_ID);
    }

    public void init(Context context, AccountInfo accountInfo, String clientSSID) {
        Log.d(TAG, "init notemark client socket...");
        initBusinessInfo(context, clientSSID);
        if(client == null) {
            client = new SensorClient(context.getApplicationContext(), clientBusinessInfo, accountInfo);
            client.setShowTips(false);
        }
    }

    public ClientBusinessInfo getBusinessInfo(Context context) {
        initBusinessInfo(context, WhiteBoardConfig.NOTE_CLIENT_SS_ID);
        return clientBusinessInfo;
    }

    private void initBusinessInfo(Context context, String clientSSID) {
        if(clientBusinessInfo == null) {
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
            int offsetX = 0;
            int offsetY = 0;
            clientBusinessInfo = new ClientBusinessInfo(TextUtils.isEmpty(clientSSID) ? WhiteBoardConfig.NOTE_CLIENT_SS_ID : clientSSID,
                    WhiteBoardConfig.NOTE_SERVER_SS_ID, "批注客户端",
                    screenWidth - offsetX, screenHeight - offsetY, offsetX, offsetY);
            clientBusinessInfo.protoVersion = 0;//增加版本拉平
        }
    }

    public SensorClient getClient() {
        return client;
    }

    public void connect() {
        client.connect(serverAddress);
    }

    public void connect(String address) {
        serverAddress = address;
        client.connect(address);
    }

    public void send(String content) {
        Log.d(TAG, "notemark send : " + content);
        client.send(content);
    }

    public void setCallback(IConnectCallback callback) {
        client.setCallback(callback);
    }

    public void start() {
        client.start();
    }

    public void stop() {
        client.stop();
    }

    public WhiteBoardServerCmdInfo getInitSyncData() {
        return initSyncData;
    }

    public void setInitSyncData(WhiteBoardServerCmdInfo data) {
        this.initSyncData = data;
    }

    public void setTestServerAddress(String address) {
        serverAddress = address;
    }
}
