package swaiotos.runtime.h5.core.os.exts.channel;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.SmartApiListener;
import com.coocaa.smartsdk.SmartApiListenerImpl;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.runtime.base.utils.ToastUtils;
import swaiotos.runtime.h5.H5ChannelInstance;
import swaiotos.runtime.h5.H5CoreExt;
import swaiotos.runtime.h5.core.os.exts.utils.ExtLog;
import swaiotos.runtime.h5.remotectrl.State;

/**
 * @ClassName: AccountExt
 * @Author: lu
 * @CreateDate: 11/18/20 2:33 PM
 * @Description:
 */
public class ChannelExt extends H5CoreExt {
    public static final String NAME = "channel";

    private static final String TAG = "ChannelExt";
    private final Set<String> listenerIds = new TreeSet<>();
    private final Set<String> statusListenerSet = new TreeSet<>();
    private Context appContext;
    private String networkForceKey;

    public static synchronized H5CoreExt get(Context context) {
        return new ChannelExt(context);
    }

    public void setNetworkForceKey(String key) {
        this.networkForceKey = key;
    }

    private ChannelExt(Context context) {
        H5ChannelInstance.getSingleton().open(context);
        H5ChannelInstance.getSingleton().setOnReceiveMsg(receiveMsgListener);
        EventBus.getDefault().register(this);
        appContext = context.getApplicationContext();
        ToastUtils.getInstance().init(appContext);

//        IntentFilter intentFilter = new IntentFilter(DongleStateReceiver.STATE_ACTION);
//        context.registerReceiver(new DongleStateReceiver(), intentFilter);
//        context.registerReceiver(new StateChangedReceiver(), new IntentFilter(STATE_CHANGED_ACTION));

        ExtLog.w(TAG, "init over...");
    }

    @Override
    public void attach(Context context) {
        super.attach(context);
        ExtLog.w(TAG, "attach ChannelExt");
        SmartApi.addListener(smartApiListener);
    }

    @Override
    public void detach(Context context) {
        ExtLog.w(TAG, "detach ChannelExt");
        super.detach(context);
        SmartApi.removeListener(smartApiListener);
        dispatchClientIdSet.clear();
        dispatchClientIdMap.clear();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnReceiveImMessage(IMMessage message) {
        ExtLog.i("OnChannelMessage msg: " + message);
        JSONObject params = new JSONObject();
        params.put("sourceClientId", message.getClientSource());
        params.put("target", message.getTarget());
        params.put("content", message.getContent());
        String json = params.toString();
        // ExtLog.i(TAG, "OnReceiveImMessage(), params = " + params);
        ExtLog.i(TAG, "OnReceiveImMessage(), listener ids = " + listenerIds);
        for (String id : listenerIds) {
            native2js(id, ON_RECEIVE, json);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnDongleState(State state) {
        String stateJson = state.encode();
        ExtLog.i(TAG, "OnDongleState state: " + stateJson);
        ExtLog.i(TAG, "OnDongleState listener: " + statusListenerSet);
        for (String id : statusListenerSet) {
            native2js(id, ON_RECEIVE, stateJson);
        }
    }

    @JavascriptInterface
    public void isLAN(String id) {
        ExtLog.d(TAG, "isLAN(), id = " + id);
        boolean isLan = SmartApi.isSameWifi();
        ExtLog.d(TAG, "isLAN(), result = " + isLan);

        JSONObject params = new JSONObject();
        params.put("isLAN", isLan);
        native2js(id, RET_SUCCESS, params.toString());
    }

    @JavascriptInterface
    public void getConnectDeviceInfo(String id) {
        ExtLog.d(TAG, "getConnectDeviceInfo(), id = " + id);

        ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        String json = JSON.toJSONString(deviceInfo);
        ExtLog.d(TAG, "getConnectDeviceInfo(), deviceInfo = " + json);
        native2js(id, RET_SUCCESS, json);
    }

    @JavascriptInterface
    public void getBindCode(String id) {
        ExtLog.d(TAG, "getBindCode(), id = " + id);
        SmartApi.addListener(smartApiListener);
        SmartApi.requestBindCode(id);
    }

    @JavascriptInterface
    public void getSpaceId(String id) {
        ExtLog.d(TAG, "getSpaceId(), id = " + id);
        String spaceId = H5ChannelInstance.getSingleton().getSpaceId();
        JSONObject params = new JSONObject();
        params.put("spaceId", spaceId);
        native2js(id, RET_SUCCESS, params.toString());
    }

    @JavascriptInterface
    public void startConnectDevice(String id) {
        ExtLog.d(TAG, "startConnectDevice(), id = " + id);
        // String sceneInfo = H5ChannelInstance.getSingleton().();
        SmartApi.startConnectDevice();
        JSONObject params = new JSONObject();
        // params.put("sceneInfo", sceneInfo);
        native2js(id, RET_SUCCESS, params.toString());
    }

    /**
     * 发消息，手机给Dongle发
     *
     * @param id
     * @param target
     * @param content
     */
    @JavascriptInterface
    public void sendMsg(String id, String target, String content) {
        ExtLog.d(TAG, "sendMsg(), id = " + id + ", target = " + target + ", content = " + content);
        if (!canSend()) {
            native2js(id, RET_FAIL, new JSONObject().toString());
            return;
        }
        H5ChannelInstance.getSingleton().sendMsg(target, content, new MessageCallback(id));
    }

    /**
     * 带一个类型发消息，手机发给Dongle
     *
     * @param id
     * @param target
     * @param content
     * @param type
     */
    @JavascriptInterface
    public void sendCommonMsg(String id, String target, String content, String type) {
        sendCommonMsg(id, target, content, type, null);
    }

    /**
     * 带一个类型发消息，手机发给Dongle
     *
     * @param id
     * @param target
     * @param content
     * @param type
     */
    @JavascriptInterface
    public void sendCommonMsg(String id, String target, String content, String type, String extras) {
        ExtLog.d(TAG, "sendMsg(), id = " + id + ", target = " + target + ", content = " + content
                + ", type = " + type + ", extra=" + extras);
        if (!canSend()) {
            native2js(id, RET_FAIL, new JSONObject().toString());
            return;
        }

        H5ChannelInstance.getSingleton().sendCommonMsg(target, content, type, extras, new MessageCallback(id));
    }

    @JavascriptInterface
    public void sendBrowserMsg(String id, String target, String content) {
        ExtLog.d(TAG, "sendBrowserMsg(), id = " + id + ", target = " + target + ", content = " + content);
        if (!canSend()) {
            native2js(id, RET_FAIL, new JSONObject().toString());
            return;
        }
        H5ChannelInstance.getSingleton().sendMsg(target, content, new MessageCallback(id));
        native2js(id, RET_SUCCESS, new JSONObject().toString());
    }

    @JavascriptInterface
    public void sendOnlineVideoMsg(String id, String target, String content) {
        ExtLog.d(TAG, "sendOnlineVideoMsg(), id = " + id + ", target = " + target + ", content = " + content);
        if (!canSend()) {
            native2js(id, RET_FAIL, new JSONObject().toString());
            return;
        }
        H5ChannelInstance.getSingleton().sendMsg(target, content, new MessageCallback(id), false);
        native2js(id, RET_SUCCESS, new JSONObject().toString());
    }

    @JavascriptInterface
    public void sendAudioMsg(String id, String target, String content) {
        ExtLog.d(TAG, "sendAudioMsg(), id = " + id + ", target = " + target + ", content = " + content);
        if (!canSend()) {
            native2js(id, RET_FAIL, new JSONObject().toString());
            return;
        }
        H5ChannelInstance.getSingleton().sendAudio(target, content, new MessageCallback(id));
        native2js(id, RET_SUCCESS, new JSONObject().toString());
    }

    @JavascriptInterface
    public void sendVideoMsg(String id, String target, String content) {
        ExtLog.d(TAG, "sendVideoMsg(), id = " + id + ", target = " + target + ", content = " + content);
        if (!canSend()) {
            native2js(id, RET_FAIL, new JSONObject().toString());
            return;
        }
        H5ChannelInstance.getSingleton().sendVideo(target, content, new MessageCallback(id));
        native2js(id, RET_SUCCESS, new JSONObject().toString());
    }

    @JavascriptInterface
    public void sendImageMsg(String id, String target, String content) {
        ExtLog.d(TAG, "sendImageMsg(), id = " + id + ", target = " + target + ", content = " + content);
        if (!canSend()) {
            native2js(id, RET_FAIL, new JSONObject().toString());
            return;
        }
        H5ChannelInstance.getSingleton().sendImage(target, content, new MessageCallback(id));
        native2js(id, RET_SUCCESS, new JSONObject().toString());
    }

    @JavascriptInterface
    public void sendLiveMsg(String id, String target, String content1, String content2) {
        ExtLog.d(TAG, "sendLiveMsg(), id = " + id + ", target = " + target + ", content1 = " + content1 + ", content2 = " + content2);
        if (!canSend()) {
            native2js(id, RET_FAIL, new JSONObject().toString());
            return;
        }
        MessageCallback callback = new MessageCallback(id);
        H5ChannelInstance.getSingleton().sendMsg(target, content1, callback);
        native2js(id, RET_SUCCESS, new JSONObject().toString());
    }

    /**
     * dongle直接发给手机，不经过状态机
     *
     * @param id
     * @param target
     * @param content
     */
    @JavascriptInterface
    public void sendBroadcast(String id, String target, String content) {
//        target = "client-runtime-h5"; // XXX
//        target = H5ChannelInstance.sourceClient; // XXX
        if (!canSend()) {
            native2js(id, RET_FAIL, new JSONObject().toString());
            return;
        }
        ExtLog.d(TAG, "sendBroadcast(), id = " + id + ", target = " + target + ", content = " + content);
        H5ChannelInstance.getSingleton().sendBroadcast(target, content, new MessageCallback(id));
        native2js(id, RET_SUCCESS, new JSONObject().toString());
    }

    /**
     * Dongle发给手机状态同步，经过状态机
     *
     * @param id
     * @param target
     * @param content
     */
    @JavascriptInterface
    public void sendStatusBroadcast(String id, String target, String content) {
        ExtLog.d(TAG, "sendStatusBroadcast(), id = " + id + ", target = " + target + ", content = " + content);
        H5ChannelInstance.getSingleton().sendStatusBroadcast(target, content);
        native2js(id, RET_SUCCESS, new JSONObject().toString());
    }

    @JavascriptInterface
    public void addMsgReceiveListener(String id) {
        synchronized (listenerIds) {
            ExtLog.d(TAG, "addMsgReceiveListener(), id: " + id);
            listenerIds.add(id);
        }
        native2js(id, RET_SUCCESS, new JSONObject().toString());
    }

    @JavascriptInterface
    public void removeMsgReceiveListener(String id) {
        synchronized (listenerIds) {
            ExtLog.d(TAG, "removeMsgReceiveListener(), id: " + id);
            listenerIds.remove(id);
        }
        native2js(id, RET_SUCCESS, new JSONObject().toString());
    }

    @JavascriptInterface
    public void addStatusReceiveListener(String id) {
        synchronized (statusListenerSet) {
            ExtLog.d(TAG, "addStatusReceiveListener(), id: " + id);
            statusListenerSet.add(id);
        }
        native2js(id, RET_SUCCESS, new JSONObject().toString());
    }

    @JavascriptInterface
    public void removeStatusReceiveListener(String id) {
        synchronized (statusListenerSet) {
            ExtLog.d(TAG, "removeStatusReceiveListener(), id: " + id);
            statusListenerSet.remove(id);
        }
        native2js(id, RET_SUCCESS, new JSONObject().toString());
    }

    @JavascriptInterface
    public void startConnectWiFi(String id) {
        ExtLog.d(TAG, "startConnectWiFi(), id = " + id);
        SmartApi.startConnectSameWifi("h5: " + id);
        native2js(id, RET_SUCCESS, new JSONObject().toString());
    }


    @JavascriptInterface
    public void sendSSEMsg(String id, String targetSid, String content) {
        String target = "com.coocaa.webrtc.airplay";

        ExtLog.d(TAG, "sendSSEMsg(), id = " + id + ", targetSid = " + targetSid + ", content = " + content);
        if (!canSend()) {
            native2js(id, RET_FAIL, new JSONObject().toString());
            return;
        }

        IMMessageCallback callback = new IMMessageCallback() {
            @Override
            public void onStart(IMMessage message) {

            }

            @Override
            public void onProgress(IMMessage message, int progress) {

            }

            @Override
            public void onEnd(IMMessage message, int code, String info) {
                JSONObject params = new JSONObject();
                params.put("code", code);
                params.put("info", info);
                native2js(id, RET_SUCCESS, params.toString());
            }
        };
        H5ChannelInstance.getSingleton().sendSSEMsg(targetSid, target, content, callback);
    }

    @JavascriptInterface
    public void enableTransferMessage(String id, String json) {
        ExtLog.d(TAG, "enableTransferMessage(), id = " + id + ", json = " + json);
        try {
            JSONObject jsonObject = JSON.parseObject(json);
            String clientId = jsonObject.getString("clientId");
            String enableStr = jsonObject.getString("enable");
            boolean enable = Boolean.parseBoolean(enableStr);
            if(!TextUtils.isEmpty(clientId)) {
                if(enable) {
                    dispatchClientIdSet.add(clientId);
                    dispatchClientIdMap.put(clientId, id);
                } else {
                    dispatchClientIdSet.remove(clientId);
                    dispatchClientIdMap.remove(clientId);
                }
                SmartApi.setMsgDispatchEnable(clientId, enable);
                native2js(id, RET_SUCCESS, new JSONObject().toString());
            } else {
                native2js(id, RET_FAIL, new JSONObject().toString());
            }
        } catch (Exception e) {
            native2js(id, RET_FAIL, new JSONObject().toString());
        }
    }


    @JavascriptInterface
    public void sendKeyMsg(String id, String json) {
        Log.d(TAG, "sendKeyMsg, id=" + id + ", json=" + json);
        try {
            JSONObject jsonObject = JSON.parseObject(json);
            String key = jsonObject.getString("key");
            int keyCode = Integer.parseInt(key);
            H5ChannelInstance.getSingleton().sendKey(keyCode);
            native2js(id, RET_SUCCESS, new JSONObject().toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private H5ChannelInstance.OnReceiveMsg receiveMsgListener = new H5ChannelInstance.OnReceiveMsg() {
        @Override
        public void onReceive(String target, String content) {
            ExtLog.d(TAG, "onReceive(), target = " + target + ", content = " + content);
            JSONObject params = new JSONObject();
            params.put("target", target);
            params.put("content", content);
            String json = params.toString();

            for (String id : listenerIds) {
                native2js(id, ON_RECEIVE, json);
            }

        }
    };

    private class MessageCallback implements IMMessageCallback {
        private String id;

        public MessageCallback(String callBackId) {
            this.id = callBackId;
        }

        @Override
        public void onStart(IMMessage message) {
            ExtLog.d(TAG, "MessageCallback - onStart - msg " + message);
        }

        @Override
        public void onProgress(IMMessage message, int progress) {
            ExtLog.d(TAG, "MessageCallback - onProgress - msg " + message + ", progress = " + progress);
        }

        @Override
        public void onEnd(IMMessage message, int code, String info) {
            ExtLog.d(TAG, "MessageCallback - onEnd - code " + code + ", info = " + info);
            JSONObject params = new JSONObject();
            params.put("code", code);
            params.put("info", info);
            native2js(id, ON_RECEIVE, params.toString());
        }

    }


    private boolean canSend() {
        if (!SmartApi.isMobileRuntime()) {
            //非手机端，不做拦截处理
            return true;
        }
        if (SmartApi.getUserInfo() == null) {
            ToastUtils.getInstance().showGlobalLong("请先登录账号");
            SmartApi.showLoginUser();
            return false;
        }
        if (SmartApi.getConnectDeviceInfo() == null) {
            SmartApi.startConnectDevice();
            return false;
        }
//        if ("FORCE_LAN".equals(networkForceKey) && !SmartApi.isSameWifi()) {
//            ToastUtils.getInstance().showGlobalLong(appContext.getResources().getString(R.string.err_no_same_wifi).toString());
//            SmartApi.startConnectSameWifi(networkForceKey);
////            return false;
//        }
        return true;
    }

    private Set<String> dispatchClientIdSet = new HashSet<>();
    private Map<String, String> dispatchClientIdMap = new HashMap<>();

    private SmartApiListener smartApiListener = new SmartApiListenerImpl() {
        @Override
        public void onDispatchMessage(String clientId, String msgJson) {
            ExtLog.d(TAG, "onDispatchMessage clientId=" + clientId + ", json=" + msgJson);
            if(dispatchClientIdSet.contains(clientId)) {
                for(String id : listenerIds) {
                    native2js(id, ON_RECEIVE, msgJson);
                }
            }
        }

        @Override
        public void onBindCodeResult(String requestId, String bindCode) {
            ExtLog.d(TAG, "onBindCodeResult requestId=" + requestId + ", bindCode=" + bindCode);
            JSONObject params = new JSONObject();
            params.put("bindCode", bindCode);
            native2js(requestId, TextUtils.isEmpty(bindCode) ? RET_FAIL : RET_SUCCESS, params.toJSONString());
        }
    };


//    private final static String STATE_CHANGED_ACTION = "com.coocaa.app_status.changed";
//
//    private class StateChangedReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG, "onReceive : " + intent.getAction());
//            if (STATE_CHANGED_ACTION.equals(intent.getAction())) {
//                ExtLog.d(TAG, "onReceive, type : " + intent.getStringExtra("type"));
//                ExtLog.d(TAG, "onReceive, state : " + intent.getStringExtra("state"));
//                ExtLog.d(TAG, "onReceive, version : " + intent.getIntExtra("version", -1));
//            }
//        }
//    }
}
