package com.swaiot.webrtc.service;


import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.swaiot.webrtc.config.Constant;
import com.swaiot.webrtc.entity.Model;
import com.swaiot.webrtc.entity.SSEEvent;

import org.greenrobot.eventbus.EventBus;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.channel.im.IMMessage;

public class WebRtcClientVoiceService extends SSChannelClient.SSChannelClientService {
    private static final String TAG = "voice";

    public WebRtcClientVoiceService() {
        super("WebRtcClientService");
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate...");
    }

    @Override
    protected boolean handleIMMessage(IMMessage imMessage, SSChannel ssChannel) {
        String content = imMessage.getContent();
        Log.d(TAG, "voice handleIMMessage content=" + content);
        postData(content, imMessage, ssChannel);

        return true;
    }


    private void postData(String content, IMMessage imMessage, SSChannel ssChannel) {
        if (content.contains(Constant.OFFER)) {

            SSEEvent event = new SSEEvent();
            Model model = new Model(content, false);
            event.setModel(model);
            event.setMsgType(Constant.OFFER);
            event.setSsChannel(ssChannel);
            event.setImMessage(imMessage);

            //WebRTCActivity1.start(WebRtcClientVoiceService.this);

            startService(new Intent(this, WebRtcVoiceService.class));

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //黏性事件 发送了该事件之后再订阅者依然能够接收到的事件
                    EventBus.getDefault().postSticky(event);
                }
            }, 1000);

        } else if (content.contains(Constant.ANSWER)) {
            Log.i(TAG, "Received Answer");

            SSEEvent event = new SSEEvent();
            Model model = new Model(content, false);
            event.setModel(model);
            event.setMsgType(Constant.ANSWER);
            //黏性事件 发送了该事件之后再订阅者依然能够接收到的事件
            EventBus.getDefault().postSticky(event);

        } else if (content.contains(Constant.CANDIDATE)) {
            Log.i(TAG, "Received candidate");
            SSEEvent event = new SSEEvent();
            Model model = new Model(content, false);
            event.setModel(model);
            event.setMsgType(Constant.CANDIDATE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "EventBus post candidate");
                    EventBus.getDefault().post(event);
                }
            }, 1000);
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy...");
    }

}