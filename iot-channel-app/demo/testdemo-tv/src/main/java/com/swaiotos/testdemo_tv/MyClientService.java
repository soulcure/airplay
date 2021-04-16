package com.swaiotos.testdemo_tv;

import android.util.Log;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;

public class MyClientService extends SSChannelClient.SSChannelClientService {

//    private int receiverTimes = 0;
//    private int replaySuccess = 0;
//    private int replayFailed = 0;

    public MyClientService() {
        super("MyClientService");
    }

    @Override
    protected boolean handleIMMessage(final IMMessage message, final SSChannel channel) {
        Log.d("c-test", "handleIMMessage  getId:" + message.getId());
        Log.d("c-test", "handleIMMessage  getSource:" + message.getSource());
        Log.d("c-test", "handleIMMessage  getTarget:" + message.getTarget());
//        Log.d("c-test", "handleIMMessage  getClientSource:" + message.getClientSource());
//        Log.d("c-test", "handleIMMessage  getClientTarget:" + message.getClientTarget());
//        Log.d("c-test", "handleIMMessage  type:" + message.getType());
//        Log.d("c-test", "handleIMMessage  content:" + message.getContent());


        repleyMsg(message,channel);


        return false;
    }

    private void repleyMsg(final IMMessage message, final SSChannel channel){
        boolean repley = false;
        String content = message.getContent();
        IMMessage reply = IMMessage.Builder.replyTextMessage(message, content + "~" + Math.random() * 100000);
        try {
            channel.getIMChannel().send(reply, new IMMessageCallback() {
                @Override
                public void onStart(IMMessage message) {
                    Log.d("c-test", "repleyMsg  onStart:" + message.getId());
                }

                @Override
                public void onProgress(IMMessage message, int progress) {
                    Log.d("c-test", "repleyMsg  onProgress:" + message.getId());
                }

                @Override
                public void onEnd(IMMessage message, int code, String info) {
                    Log.d("c-test", "repleyMsg  onEnd  code:" + code);
                }
            });
            repley = true;;
        }catch (Exception e){
            e.printStackTrace();
            repley = false;
        }
        Log.d("c-test", "repleyMsg  update  :" );
        DataManager.Instance.updateData(repley);
    }
}
