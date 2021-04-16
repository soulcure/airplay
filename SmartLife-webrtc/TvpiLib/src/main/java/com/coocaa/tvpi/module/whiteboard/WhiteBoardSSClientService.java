package com.coocaa.tvpi.module.whiteboard;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.coocaa.whiteboard.client.WhiteBoardClientSSEvent;
import com.coocaa.whiteboard.server.WhiteBoardServerCmdInfo;
import com.coocaa.whiteboard.server.WhiteBoardServerSSCmd;

import org.greenrobot.eventbus.EventBus;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.sensor.client.SensorClientChannelService;

public class WhiteBoardSSClientService extends SensorClientChannelService {

    final String TAG = "WBClient";

    public WhiteBoardSSClientService() {
        super("ss-whiteboard-client-service");
    }

    @Override
    protected boolean handleIMMessage(IMMessage message, SSChannel channel) {
        Log.i(TAG, "client handleIMMessage  type:" + message.getType());
        Log.i(TAG, "client handleIMMessage  id: " + message.getId());
        Log.i(TAG, "client handleIMMessage  source:" + message.getSource());
        Log.i(TAG, "client handleIMMessage  content:" + message.getContent());
        try {
            WhiteBoardServerCmdInfo info = JSON.parseObject(message.getContent(), WhiteBoardServerCmdInfo.class);
            if(WhiteBoardServerSSCmd.CMD_SERVER_REPLY_START.equals(info.cmd)
                    || WhiteBoardServerSSCmd.CMD_SERVER_REPLY_OWNER.equals(info.cmd)) {
                Log.d(TAG, "receive server reply.");
                WhiteBoardClientSSEvent event = new WhiteBoardClientSSEvent();
                event.info = info;
                EventBus.getDefault().post(event);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.handleIMMessage(message, channel);
    }
}
