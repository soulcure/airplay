package com.swaiot.webrtc.service;


import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.swaiot.webrtc.StackAct;
import com.swaiot.webrtc.config.Constant;
import com.swaiot.webrtc.entity.Model;
import com.swaiot.webrtc.entity.SSEEvent;
import com.swaiot.webrtc.ui.AirPlayInfoActivity;
import com.swaiot.webrtc.ui.WebRTCActivity;
import com.swaiot.webrtc.util.AppUtils;
import com.swaiot.webrtc.util.Constants;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.utils.ThreadManager;

public class WebRtcClientService extends SSChannelClient.SSChannelClientService {
    private static final String TAG = "WebRtcClientService";

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public WebRtcClientService() {
        super("WebRtcClientService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate...");
    }

    @Override
    protected boolean handleIMMessage(IMMessage imMessage, SSChannel ssChannel) {
        Log.d(TAG, "WebRtcClientService handleIMMessage: message =" + imMessage.toString());
        String content = imMessage.getContent();
        Session sourceSession = imMessage.getSource();
        String sendSid = sourceSession.getId();

        Map<String, String> extras = imMessage.getExtra();
        String target_client = extras.get("target-client");
        boolean isWeb;
        if (!TextUtils.isEmpty(target_client)) {
            isWeb = false;
            Log.d(TAG, "target client is mobile");
        } else {
            isWeb = true;
            Log.d(TAG, "target client is web");
        }

        Log.d(TAG, "WebRtcClientService handleIMMessage: content =" + content);
        postData(sendSid, content, imMessage, ssChannel, isWeb);

        return true;
    }


    private void postData(String sid, String content, IMMessage imMessage, SSChannel ssChannel, boolean isWeb) {
        if (content.contains(Constant.OFFER)) {
            Map<String, String> extras = imMessage.getExtra();
            Log.i(TAG, "Received Offer sid=" + sid + " extras=" + extras.size());

            SSEEvent event = new SSEEvent();
            Model model = new Model(content, isWeb);
            event.setModel(model);
            event.setMsgType(Constant.OFFER);
            event.setTargetSid(sid);
            event.setSsChannel(ssChannel);
            event.setExtras(extras);

            if (StackAct.instance().finishActivity(sid)) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        WebRTCActivity.start(WebRtcClientService.this,isWeb);
                        //黏性事件 发送了该事件之后再订阅者依然能够接收到的事件
                        EventBus.getDefault().postSticky(event);
                    }
                }, 1000);
            } else {
                WebRTCActivity.start(WebRtcClientService.this,isWeb);
                //黏性事件 发送了该事件之后再订阅者依然能够接收到的事件
                EventBus.getDefault().postSticky(event);
            }

        } else if (content.contains(Constant.ANSWER)) {
            Log.i(TAG, "Received Answer");

            SSEEvent event = new SSEEvent();
            Model model = new Model(content, isWeb);
            event.setModel(model);
            event.setMsgType(Constant.ANSWER);
            //黏性事件 发送了该事件之后再订阅者依然能够接收到的事件
            EventBus.getDefault().postSticky(event);

        } else if (content.contains(Constant.CANDIDATE)) {
            Log.i(TAG, "Received candidate");
            SSEEvent event = new SSEEvent();
            Model model = new Model(content, isWeb);
            event.setModel(model);
            event.setMsgType(Constant.CANDIDATE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "EventBus post candidate");
                    EventBus.getDefault().post(event);
                }
            }, 1000);

        } else if (content.contains("startAirPlay")) {
            try {
                SSEEvent event = new SSEEvent();
                event.setImMessage(imMessage);
                event.setSsChannel(ssChannel);
                EventBus.getDefault().postSticky(event);

                JSONObject json = new JSONObject(content);
                String wifiAccount = json.optString("wifiAccount");
                String wifiPW = json.optString("wifiPW");
                String host = json.optString("host");

                Log.d(TAG, "Received startAirPlay UI" + " wifiAccount=" + wifiAccount
                        + " wifiPW=" + wifiPW + " host=" + host);
                AirPlayInfoActivity.start(this, wifiAccount, wifiPW, host);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("startAirPlay", true);
                jsonObject.put("code", 0);
                jsonObject.put("message", "屏幕镜像详情页面打开成功");
                String text = jsonObject.toString();
                sendChannelMessage(imMessage, ssChannel, text);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (content.contains("checkStatus")) {
            try {
                boolean isStart = AppUtils.isTopActivity(this, AirPlayInfoActivity.class.getName());
                Log.d(TAG, "do checkStatus startAirPlay=" + isStart);
                int code;
                String message;
                if (isStart) {
                    code = 0;
                    message = "屏幕镜像详情页面已经开启";
                } else {
                    code = -1;
                    message = "屏幕镜像详情页面未开启";
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("checkStatus", true);
                jsonObject.put("code", code);
                jsonObject.put("message", message);
                String text = jsonObject.toString();
                sendChannelMessage(imMessage, ssChannel, text);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (content.contains(Constant.SIGNALING_GET_NET_INFO)) {
            try {

                String ssId = ssChannel.getSessionManager().getMySession().getExtra("ssid");
                String password = ssChannel.getSessionManager().getMySession().getExtra("password");
                String net = ssChannel.getSessionManager().getMySession().getExtra("net");
                String localIp = ssChannel.getSessionManager().getMySession().getExtra("stream-local");
                String netIP = Constants.getNetIp();
                String gateWay = Constants.getGateWay();
                String ipMask = Constants.getIpAddrMaskForInterfaces("eth0");
                String dns = Constants.getDns(getApplicationContext());

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type","SIGNALING_GET_NETINFO");
                jsonObject.put("ssId", ssId);
                jsonObject.put("password", password);
                jsonObject.put("net", net);
                jsonObject.put("localIp", localIp);
                jsonObject.put("netIP", netIP);
                jsonObject.put("gateWay", gateWay);
                jsonObject.put("ipMask", ipMask);
                jsonObject.put("dns", dns);

                String text = jsonObject.toString();
                sendChannelMessage(imMessage, ssChannel, text);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * 回复对方消息
     */
    private void sendChannelMessage(IMMessage message, SSChannel ssChannel, String content) {
        IMMessage.Builder builder = new IMMessage.Builder();

        String msgId = message.getId();  //使用原消息体id
        builder.setId(msgId);

        Session source = message.getTarget();  //发送方设置为接收方
        Session target = message.getSource();  //接收方设置为发送方
        builder.setTarget(target);
        builder.setSource(source);

        String sourceClient = message.getClientTarget(); //发送方设置为接收方
        //String targetClient = message.getClientSource(); //接收方设置为发送方
        String targetClient = "ss-clientID-runtime-h5-channel"; //接收方

        builder.setClientSource(sourceClient);
        builder.setClientTarget(targetClient);

        builder.setType(IMMessage.TYPE.TEXT);
        builder.putExtra(SSChannel.FORCE_SSE, "true");//强制云端

        builder.setContent(content);
        IMMessage msg = builder.build();

        IMMessageCallback callback = new IMMessageCallback() {

            @Override
            public void onStart(IMMessage imMessage) {
                Log.d(TAG, "send onStart = " + content);
            }

            @Override
            public void onProgress(IMMessage imMessage, int i) {
                Log.d(TAG, "send onProgress = " + content);
            }

            @Override
            public void onEnd(IMMessage imMessage, int code, String info) {
                Log.d(TAG, "send onEnd = " + content);
            }
        };

        try {
            ssChannel.getIMChannel().send(msg, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy...");
    }


}