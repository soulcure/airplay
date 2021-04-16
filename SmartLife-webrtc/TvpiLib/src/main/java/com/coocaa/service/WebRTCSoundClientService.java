package com.coocaa.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.swaiot.webrtcc.Constant;
import com.swaiot.webrtcc.entity.Model;
import com.swaiot.webrtcc.entity.SSEEvent;

import org.greenrobot.eventbus.EventBus;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.channel.im.IMMessage;

public class WebRTCSoundClientService extends SSChannelClient.SSChannelClientService {
    private static final String TAG = "sound";

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public WebRTCSoundClientService() {
        super("WebRTCSoundClientService");
        Log.d(TAG, "WebRTCSoundClientService() called");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "WebRTCSoundClientService onCreate called");
    }


    @Override
    protected boolean handleIMMessage(IMMessage message, SSChannel channel) {
        String content = message.getContent();
        postData(content);
        return true;
    }


    private void postData(String content) {
        if (content.contains(Constant.ANSWER)) {
            Log.i(TAG, "Received Answer");

            SSEEvent event = new SSEEvent();
            Model model = new Model(content);
            event.setModel(model);
            event.setMsgType(Constant.ANSWER);
            //黏性事件 发送了该事件之后再订阅者依然能够接收到的事件
            EventBus.getDefault().postSticky(event);

        } else if (content.contains(Constant.CANDIDATE)) {
            Log.i(TAG, "Received candidate");
            SSEEvent event = new SSEEvent();
            Model model = new Model(content);
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

}
