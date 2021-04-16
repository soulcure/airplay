package com.coocaa.whiteboard.ui.common.notemark;

import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.coocaa.whiteboard.client.WhieBoardClientSSCmd;
import com.coocaa.whiteboard.config.WhiteBoardConfig;

import java.util.Map;

import swaiotos.sensor.channel.ChannelMsgSender;
import swaiotos.sensor.data.AccountInfo;
import swaiotos.sensor.data.ClientCmdInfo;

public class NoteClientIOTChannelHelper {

    private static ChannelMsgSender msgSender;
    private final static String TAG = "NMClient";

    public static void init(Context context) {
        if(msgSender == null) {
            msgSender = new ChannelMsgSender(context.getApplicationContext(), WhiteBoardConfig.NOTE_CLIENT_SS_ID);
        }
    }

    public static void sendStartNoteMsg(AccountInfo accountInfo) {
        ClientCmdInfo clientCmdInfo = new ClientCmdInfo();
        clientCmdInfo.cmd = WhieBoardClientSSCmd.CMD_CLIENT_REQUEST_START_SERVER;
        clientCmdInfo.accountInfo = accountInfo;
        clientCmdInfo.cid = clientCmdInfo.accountInfo.mobile;

        Map<String, String> params = new ArrayMap<>();
        clientCmdInfo.content = JSON.toJSONString(params);

        String content = JSON.toJSONString(clientCmdInfo);
        sendChannelMsg(content);
    }

    public static void sendChannelMsg(String content) {
        Log.d(TAG, "sendChannelMsg to whiteboard : " + content);
        try {
            msgSender.sendMsgSticky(content, WhiteBoardConfig.NOTE_SERVER_SS_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
