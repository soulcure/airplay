package com.swaiotos.testdemo_pad;

import android.util.Log;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.channel.im.IMMessage;

public class MyClientService extends SSChannelClient.SSChannelClientService {

    public MyClientService() {
        super("MyClientService");
    }

    @Override
    protected boolean handleIMMessage(IMMessage message, SSChannel channel) {
        Log.d("iot-channel", "handleIMMessage  getId:" + message.getId());
        Log.d("iot-channel", "handleIMMessage  getSource:" + message.getSource());
        Log.d("iot-channel", "handleIMMessage  getTarget:" + message.getTarget());
        Log.d("iot-channel", "handleIMMessage  getClientSource:" + message.getClientSource());
        Log.d("iot-channel", "handleIMMessage  getClientTarget:" + message.getClientTarget());
        Log.d("iot-channel", "handleIMMessage  type:" + message.getType());
        Log.d("iot-channel", "handleIMMessage  content:" + message.getContent());
        return false;
    }
}
