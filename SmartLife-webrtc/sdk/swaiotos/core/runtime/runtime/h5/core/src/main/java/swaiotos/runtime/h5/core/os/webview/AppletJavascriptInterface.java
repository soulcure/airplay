package swaiotos.runtime.h5.core.os.webview;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coocaa.smartsdk.SmartApi;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.runtime.h5.common.bean.H5ContentBean;
import swaiotos.runtime.h5.common.event.OnFunctonCBData;
import swaiotos.runtime.h5.common.event.OnGameEngineInfo;
import swaiotos.runtime.h5.common.event.OnReportRC;
import swaiotos.runtime.h5.common.event.OnSetEnableIMReceive;
import swaiotos.runtime.h5.common.event.OnShakeRegisterCBData;
import swaiotos.runtime.h5.common.event.OnUISafeDistanceCBData;
import swaiotos.runtime.h5.common.event.OnVibrateEvent;
import swaiotos.runtime.h5.common.event.SetCastFromShow;
import swaiotos.runtime.h5.common.event.SetHeaderColorEvent;
import swaiotos.runtime.h5.common.event.SetLeftBtnEvent;
import swaiotos.runtime.h5.common.event.SetNativeUI;
import swaiotos.runtime.h5.common.util.LogUtil;
import swaiotos.runtime.h5.core.os.model.ISendMessageManager;


/**
 * @ClassName: AppletJavascriptInterface
 * @Author: AwenZeng
 * @CreateDate: 2020/10/22 16:10
 * @Description:
 */
public class AppletJavascriptInterface implements Serializable {
    private static final String TAG = "AppletJavascriptInterface";
    private String mLeftBtnType;
    private ISendMessageManager mSendMessageManager;
    private String id; //用来区分EventBus消息


//    public AppletJavascriptInterface(ISendMessageManager sendMessageManager) {
//        this.mSendMessageManager = sendMessageManager;
//    }

    public AppletJavascriptInterface(ISendMessageManager sendMessageManager, String id) {
        this.mSendMessageManager = sendMessageManager;
        this.id = id;
    }

    /**
     * H5 给Dongle/TV发送消息
     *
     * @param senderUrl,发送消息的url
     * @param targetUrl,目标url
     * @param content,内容
     */
    @JavascriptInterface
    public void broadcastToScreenClient(String senderUrl, String targetUrl, String content) {
        mSendMessageManager.sendTvDongleMessage(new H5ContentBean(senderUrl, targetUrl, content), null, null, null);
    }

    /**
     * H5 给所有的设备都发送消息
     *
     * @param senderUrl,发送消息的url
     * @param targetUrl,目标url
     * @param content,内容
     */
    @JavascriptInterface
    public void broadcastToRoomClients(String senderUrl, String targetUrl, String content) {
        mSendMessageManager.sendDeviceMessage(new H5ContentBean(senderUrl, targetUrl, content), null, null, null);
    }

    @JavascriptInterface
    public void gotoApplet(String appletUri) {
        Uri uri = Uri.parse(appletUri);
        if (uri == null) {
            Log.e("AppletJSInterface", "gotoApplet appletUri error: " + uri);
        }
        mSendMessageManager.gotoApplet(uri);
    }

    @JavascriptInterface
    public void onJSMessage(String msg) {
        LogUtil.d(" onJSMessage type: " + msg);
        try {
            JSONObject obj = JSON.parseObject(msg);
            String msgType = (String) obj.get("event");
            Object callbackId = obj.get("callbackId");
            Object checkSupport = obj.get("checkSupport");
            Object localAppVersion = obj.get("localAppVersion");
            Object remoteAppVersion = obj.get("remoteAppVersion");
            IMMessageCallback imCallback = new IMMessageCallback() {
                @Override
                public void onStart(IMMessage message) {

                }

                @Override
                public void onProgress(IMMessage message, int progress) {

                }

                @Override
                public void onEnd(IMMessage message, int code, String info) {
                    if (callbackId != null) {
                        obj.put("callbackCode", code < 0 ? -3 : 1); //-3 表示IM发送失败
                        obj.put("resultCode", code);
                        obj.put("resultMsg", info);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                }
            };
            if ("leftBtnType".equals(msgType)) {
                String leftBtnType = (String) obj.get("leftBtnType");
                mLeftBtnType = leftBtnType;
                SetLeftBtnEvent event = new SetLeftBtnEvent(leftBtnType).setId(id);
                EventBus.getDefault().post(event);
            } else if ("setHeaderBgColor".equals(msgType)) {
                String color = (String) obj.get("color");
                SetHeaderColorEvent event = new SetHeaderColorEvent(color).setId(id);
                EventBus.getDefault().post(event);
            } else if ("setCastFromShow".equals(msgType)) {
                Integer show = (Integer) obj.get("show");
                SetCastFromShow event = new SetCastFromShow(show).setId(id);
                EventBus.getDefault().post(event);
            } else if ("setNativeUI".equals(msgType)) {
                SetNativeUI event = new SetNativeUI(obj).setId(id);
                Object leftType = obj.get("leftBtnType");
                if (leftType != null) {
                    mLeftBtnType = (String) leftType;
                }
                EventBus.getDefault().post(event);
            } else if ("requireRemoteAppVersion".equals(msgType)) {
                String appId = (String) obj.get("appId");
                mSendMessageManager.getRemoteAppVersion(appId);
            } else if ("requireUserInfo".equals(msgType)) {
                if (checkSupport != null && (boolean) checkSupport == true) {
                    if (callbackId != null) {
                        obj.put("callbackCode", 1);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                } else {
                    mSendMessageManager.getUserInfo(callbackId);
                }

            } else if ("remoteCtrl".equals(msgType)) {
                String play = (String) obj.get("playState"); // "play", 播放 , " pause" , 暂停
                String title = (String) obj.get("title"); // 标题
                if (checkSupport != null && (boolean) checkSupport == true) {
                    if (callbackId != null) {
                        obj.put("callbackCode", 1);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                } else {
                    boolean isOk = mSendMessageManager.setRemoteCtrlState(play, title);
                    if (callbackId != null) {
                        obj.put("callbackCode", isOk ? 1 : 0);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                }
            } else if ("registerShake".equals(msgType)) {
                if (checkSupport != null && (boolean) checkSupport == true) {
                    if (callbackId != null) {
                        obj.put("callbackCode", 1);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                } else {
                    EventBus.getDefault().post(new OnShakeRegisterCBData("onShakeRegister", obj));
                }
            } else if ("shake".equals(msgType)) {
                if (checkSupport != null && (boolean) checkSupport == true) {
                    if (callbackId != null) {
                        obj.put("callbackCode", 1);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                } else {
                    Integer time = obj.getInteger("time");
                    EventBus.getDefault().post(new OnVibrateEvent(time));
                    if (callbackId != null) {
                        obj.put("callbackCode", 1);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                }
            } else if("shake".equals(msgType)){
                if (checkSupport != null && (boolean)checkSupport == true) {
                    if (callbackId != null) {
                        obj.put("callbackCode", 1);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                } else{
                    Integer time = obj.getInteger("time");
                    EventBus.getDefault().post(new OnVibrateEvent(time));
                    if (callbackId != null) {
                        obj.put("callbackCode", 1);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                }
            } else if("postMessage".equals(msgType)){
                if (checkSupport != null && (boolean)checkSupport == true) {

                    if (callbackId != null) {
                        obj.put("callbackCode", 1);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                } else {
                    String senderUrl = obj.getString("senderUrl");
                    String targetUrl = obj.getString("targetUrl");
                    String content = obj.getString("content");
                    String logType = obj.getString("logType");
                    Boolean isBroadcast = obj.getBoolean("isBroadcast");
                    Boolean isShowCastFrom = obj.getBoolean("isShowCastFrom");
                    boolean isOk;
                    if (isBroadcast != null && isBroadcast) {
                        isOk = mSendMessageManager.sendDeviceMessage(new H5ContentBean(senderUrl, targetUrl, content, logType), remoteAppVersion, imCallback, isShowCastFrom);
                    } else {
                        isOk = mSendMessageManager.sendTvDongleMessage(new H5ContentBean(senderUrl, targetUrl, content, logType), remoteAppVersion, imCallback, isShowCastFrom);
                    }
                    if (callbackId != null && !isOk) {
                        obj.put("callbackCode", 0);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                }
            } else if ("setEnableIMReceive".equals(msgType)) {
                if (checkSupport != null && (boolean) checkSupport == true) {
                    if (callbackId != null) {
                        obj.put("callbackCode", 1);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                } else {
                    Object data = obj.get("enable");
                    boolean isOk = false;
                    if (data != null && data.getClass().equals(Boolean.class)) {
                        isOk = true;
                        EventBus.getDefault().post(new OnSetEnableIMReceive((Boolean) data).setId(id));
                    }
                    if (callbackId != null) {
                        obj.put("callbackCode", isOk ? 1 : 0);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                }
            } else if ("submitLog".equals(msgType)) {
                if (checkSupport != null && (boolean) checkSupport == true) {
                    if (callbackId != null) {
                        obj.put("callbackCode", 1);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                } else {
                    String eventName = obj.getString("eventName");
                    JSONObject logData = obj.getJSONObject("logData");
                    String tag = obj.getString("tag");
                    Map<String, String> map = null;
                    boolean isOk = false;
                    if (logData != null) {
                        map = logData.toJavaObject(Map.class);
                    } else {
                        map = new HashMap<>();
                    }
                    if(TextUtils.isEmpty(tag)) {
                        SmartApi.submitLog(eventName, map);
                    } else {
                        SmartApi.submitLogWithTag(tag, eventName, map);
                    }
                    if (callbackId != null) {
                        obj.put("callbackCode", isOk ? 1 : 0);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                }
            } else if ("gameEngine".equals(msgType)) {

                if (checkSupport != null && (boolean) checkSupport == true) {
                    if (callbackId != null) {
                        obj.put("callbackCode", 1);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                } else {
                    String gameEvent = (String) obj.get("gameEvent");
                    String mobileUrl = (String) obj.get("mobileUrl");
                    String tvUrl = (String) obj.get("tvUrl");

                    OnGameEngineInfo engineInfo = new OnGameEngineInfo(gameEvent, mobileUrl, tvUrl);
                    if ("input".equals(gameEvent)) {
                        engineInfo.keyCodeID = (int) obj.get("keycodeID");
                        engineInfo.keyCode = (int) obj.get("keycode");
                        engineInfo.keyAction = (int) obj.get("keyAction");
                    } else if ("custom_data".equals(gameEvent)) {
                        engineInfo.extra = (String) obj.get("extra");
                    }
                    boolean isOk = mSendMessageManager.sendGameEvent(engineInfo, remoteAppVersion, imCallback);

                    if (callbackId != null && !isOk) {
                        obj.put("callbackCode", 0);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                }
            } else if ("reportRC".equals(msgType)) {
                if (checkSupport != null && (boolean) checkSupport == true) {
                    if (callbackId != null) {
                        obj.put("callbackCode", 1);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                } else {
                    // 气氛由于h5没有拿用户信息的逻辑，所以已经在runtime当中做完了遥控器的逻辑
                    // 游戏要注意h5-runtime-tag=dice,BusinessState的id字段直接传dice
                    // 另外，userList字段TV端需要记录用户列表数据，塞到BusinessState当中
                    Object state = obj.get("state");
                    LogUtil.d("reportRC received state = " + state);
                    int callbackCode = 0;
                    if (state != null) {
                        callbackCode = 1;
                        EventBus.getDefault().post(new OnReportRC(id, (String) state));
                    }
                    if (callbackId != null) {
                        obj.put("callbackCode", callbackCode);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                }
            } else if ("requireSafeDistance".equals((msgType))) {
                LogUtil.d("requireSafeDistance received ");
                if (checkSupport != null && (boolean) checkSupport == true) {
                    if (callbackId != null) {
                        LogUtil.d("requireSafeDistance received checkSupport");
                        obj.put("callbackCode", 1);
                        EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                    }
                } else {
                    LogUtil.d("requireSafeDistance received onUISafeDistance");
                    EventBus.getDefault().post(new OnUISafeDistanceCBData("onUISafeDistance"));
                }
            } else {
                LogUtil.d(" postMessage not support message type: " + msgType);
                if (callbackId != null) {
                    obj.put("callbackCode", -1);// 表示方法不支持
                    EventBus.getDefault().post(new OnFunctonCBData(obj).setId(id));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getLeftBtnType() {
        return mLeftBtnType;
    }
}
