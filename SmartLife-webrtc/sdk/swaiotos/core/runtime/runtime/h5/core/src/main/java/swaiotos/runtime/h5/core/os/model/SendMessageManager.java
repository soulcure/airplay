package swaiotos.runtime.h5.core.os.model;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coocaa.businessstate.BusinessStateTvReport;
import com.coocaa.businessstate.object.BusinessState;
import com.coocaa.businessstate.object.User;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.UserChangeListener;
import com.coocaa.smartsdk.object.IUserInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import swaiotos.channel.iot.ccenter.CCenterManger;
import swaiotos.channel.iot.ccenter.CCenterMangerImpl;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.utils.ThreadManager;
import swaiotos.runtime.AppletRuntimeManager;
import swaiotos.runtime.h5.H5ChannelInstance;
import swaiotos.runtime.h5.H5SSClientService;
import swaiotos.runtime.h5.common.bean.H5ContentBean;
import swaiotos.runtime.h5.common.bean.SsePushBean;
import swaiotos.runtime.h5.common.event.OnGameEngineInfo;
import swaiotos.runtime.h5.common.event.OnQrCodeCBData;
import swaiotos.runtime.h5.common.event.OnUserInfo;
import swaiotos.runtime.h5.common.util.LogUtil;
import swaiotos.runtime.h5.common.util.URLSplitUtil;
import swaiotos.runtime.h5.core.os.H5CoreOS;
import swaiotos.runtime.h5.core.os.H5RunType;
import swaiotos.runtime.h5.remotectrl.H5MediaStateData;

import static swaiotos.runtime.h5.core.os.H5CoreOS.TAG;


/**
 * @ClassName: SendMessageManager
 * @Author: AwenZeng
 * @CreateDate: 2020/10/26 20:11
 * @Description:
 */
public class SendMessageManager implements ISendMessageManager {
    private Context mContext;
    private boolean isShowCastFrom = true;
    private String networkType = H5RunType.RUNTIME_NETWORK_NORMAL;
    public final static int H5_RUNTIME_PROP_VERSION = 4;

    private CustomUserChangedListener mUserListener;

    public SendMessageManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void setNetworkForceType(String forceType){
        if(H5RunType.RUNTIME_NETWORK_FORCE_LAN.equalsIgnoreCase(forceType) || H5RunType.RUNTIME_NETWORK_FORCE_WAN.equalsIgnoreCase(forceType)){
            this.networkType = forceType;
            LogUtil.d("SendMessageManager setNetworkForceType = "+forceType);
        }
    }

    @Override
    public boolean sendTvDongleMessage(H5ContentBean data, Object remoteVersion, IMMessageCallback callback, Boolean isShowCastFrom) {
        LogUtil.androidLog(H5CoreOS.TAG, "sendTvDongleMessage: " + data);
        if(data!=null && data.getH5ReceivedUrl() != null && data.getH5ReceivedUrl().startsWith("tvnp://")){
            String clientTargetWithPrefix = data.getH5ReceivedUrl();
            String clientTarget = clientTargetWithPrefix.substring(7);
            String content = data.getH5Content();
            String sourceUrl = data.getH5SenderUrl();
            return sendToScreenNativeApp(clientTarget,sourceUrl,content, remoteVersion, callback, isShowCastFrom);
        }else{
//            return broadCastMessage(SsePushBean.EVENT_TYPE.DONGLEANDTV,data,remoteVersion, callback);
            return sendCustomBackgroundMsg(SsePushBean.EVENT_TYPE.DONGLEANDTV,data,remoteVersion, callback, isShowCastFrom);
        }
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public void getRemoteAppVersion(String clientTarget) {
        Log.d("SendMessageManager", "getRemoteAppVersion() called with: clientTarget = [" + clientTarget + "]");
        SSChannel ssChannel = H5ChannelInstance.getSingleton().getSSChannel(mContext);
        try {
            Session mySession = ssChannel.getSessionManager().getMySession();
            Session targetSession = ssChannel.getSessionManager().getConnectedSession();
            IMMessage message = IMMessage.Builder.reqClientProto(mySession, targetSession, "client-runtime-h5", clientTarget);
            ssChannel.getIMChannel().send(message, new IMMessageCallback() {
                @Override
                public void onStart(IMMessage message) {
                    LogUtil.androidLog(H5CoreOS.TAG, "getRemoteAppVersion onStart: " + message.getContent());
                }

                @Override
                public void onProgress(IMMessage message, int progress) {
                    LogUtil.androidLog(H5CoreOS.TAG, "getRemoteAppVersion onProgress: " + message.getContent() + "--progress:" + progress);
                }

                @Override
                public void onEnd(IMMessage message, int code, String info) {
                    LogUtil.androidLog(H5CoreOS.TAG, "getRemoteAppVersion onEnd: " + message.getContent() + "--code:" + code);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean sendDeviceMessage(H5ContentBean data, Object remoteVersion, IMMessageCallback callback, Boolean isShowCastFrom) {
        LogUtil.androidLog(H5CoreOS.TAG, "sendDeviceMessage: " + data);
        return broadCastMessage(SsePushBean.EVENT_TYPE.ALL_DEVICES,data,remoteVersion, callback, isShowCastFrom);
    }

    @Override
    public void gotoApplet(Uri applet){
        LogUtil.androidLog(H5CoreOS.TAG, "gotoApplet: " + applet.toString());
        try{
            if(null == SmartApi.getUserInfo()){
                SmartApi.showLoginUser();
            }else{
                AppletRuntimeManager.get(mContext).startApplet(applet);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void setCastFromShow(boolean isShow) {
        isShowCastFrom = isShow;
    }

    public class CustomUserChangedListener implements UserChangeListener{

        Object mCallBackID;
        public CustomUserChangedListener(Object callbackID){
            mCallBackID = callbackID;
        }

        public void updateCallBackID(Object callbackID){
            this.mCallBackID = callbackID;
        }

        @Override
        public void onUserChanged(boolean login, IUserInfo userInfo) {
            if(login){
                if(userInfo!=null){
                    EventBus.getDefault().post(new OnUserInfo("onUserInfo",1, userInfo.nickName,userInfo.mobile,userInfo.avatar,userInfo.open_id,userInfo.accessToken, mCallBackID));
                }
            }else{
                EventBus.getDefault().post(new OnUserInfo("onUserInfo",0, "","","","","", mCallBackID));
            }
        }
    }

    @Override
    public boolean getUserInfo(Object callbackId) {

        if(mUserListener == null){
            mUserListener = new CustomUserChangedListener(callbackId);
        }
        mUserListener.updateCallBackID(callbackId);

        IUserInfo info = SmartApi.getUserInfo(mUserListener,true);
        if(info!=null){
            EventBus.getDefault().post(new OnUserInfo("onUserInfo",1, info.nickName,info.mobile,info.avatar,info.open_id,info.accessToken, callbackId));
        }else{
            EventBus.getDefault().post(new OnUserInfo("onUserInfo",0, "","","","","", callbackId));
        }
        return true;
    }

    private static final String ACTION = "coocaa.intent.action.StateManagerService";

    public void reportH5Media(Context context,boolean isPause,String title) {

        if(H5SSClientService.isTVOrDongle()){
            H5MediaStateData h5MediaStateData = new H5MediaStateData();
            h5MediaStateData.playCmd = isPause?"pause":"play";
            h5MediaStateData.mediaTitle = title;

            String stateJson = H5MediaStateData.toJsonString(h5MediaStateData);

            // TV上报背景定制状态
            if(H5SSClientService.owner !=null){
                LogUtil.d("reportH5Media" + stateJson);
                User mUser = User.decode(H5SSClientService.owner);
                String id = mContext.getPackageName()+"$"+"H5_ATMOSPHERE";
                BusinessStateTvReport.getDefault().updateBusinessState(BusinessState.builder()
                        .id(id)
                        .owner(User.builder()
                                .token(mUser.token)
                                .avatar(mUser.avatar)
                                .nickName(mUser.nickName)
                                .userID(mUser.userID)
                                .mobile(mUser.mobile)
                                .build())
                        .values(stateJson == null ? "{}" : stateJson)
                        .build());
            }
        }else{
            LogUtil.d("reportH5Media only avaiable on TV or Dongle.");
        }

    }

    @Override
    public boolean setRemoteCtrlState(String pause, String title) {
        if(H5SSClientService.isTVOrDongle()){
            if("play".equalsIgnoreCase(pause)){
                reportH5Media(mContext,false,title);
            }else if("pause".equalsIgnoreCase(pause)){
                reportH5Media(mContext,true,title);
            } else {
                return false;
            }
        }else{
            LogUtil.e("setRemoteCtrlState called! But current device is mobile,don't send "+ title + " "+ pause +" state.");
            return false;
        }
        return true;
    }

    private class QrCodeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //结果回调
            if (msg.what == 1) {//what默认返回1
                Bundle data = msg.getData();
                String code = data.getString("bind_code");
                String qr = data.getString("screenQR");

                EventBus.getDefault().post(new OnQrCodeCBData("onGetQRCode",qr,code));
            }
        }
    }

    @Override
    public boolean sendGameEvent(OnGameEngineInfo gameInfo, Object remoteVersion, IMMessageCallback callback) {

        if(H5SSClientService.isTVOrDongle()){
            if("custom_data".equals(gameInfo.engineEvent) || "tv_destroyed".equals(gameInfo.engineEvent)){
                LogUtil.e("tv replay custom_data to mobile!");
                if(gameInfo!=null) {
                    SSChannel ssChannel = H5ChannelInstance.getSingleton().getSSChannel(mContext);
                    if(null!=ssChannel) {
                        ThreadManager.getInstance().ioThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Session mySession = ssChannel.getSessionManager().getMySession();
                                    boolean isCreate = false;
                                    if("create".equals(gameInfo.engineEvent)){
                                        isCreate = true;
                                    }
                                    IMMessage message = new IMMessage.Builder()
                                            .setBroadcast(true)
                                            .setSource(mySession)
                                            .setClientTarget("client-runtime-h5")
                                            .setClientSource("client-runtime-h5")
                                            .setContent(OnGameEngineInfo.toJSONString(gameInfo))
                                            .setType(isCreate?IMMessage.TYPE.TEXT:IMMessage.TYPE.CTR)
                                            .build();
                                    ssChannel.getIMChannel().send(message, new IMMessageCallback() {
                                        @Override
                                        public void onStart(IMMessage message) {
                                            LogUtil.androidLog(H5CoreOS.TAG, "tv replay custom_data onStart: " + message.getContent());
                                            if (callback != null) {
                                                callback.onStart(message);
                                            }
                                        }

                                        @Override
                                        public void onProgress(IMMessage message, int progress) {
                                            LogUtil.androidLog(H5CoreOS.TAG, "tv replay custom_data onProgress: " + message.getContent() + "--progress:" + progress);
                                            if (callback != null) {
                                                callback.onProgress(message, progress);
                                            }
                                        }

                                        @Override
                                        public void onEnd(IMMessage message, int code, String info) {
                                            LogUtil.androidLog(H5CoreOS.TAG, "tv replay custom_data onEnd: " + message.getContent() + "--code:" + code);
                                            if (callback != null) {
                                                callback.onEnd(message, code, info);
                                            } else {
                                                if(code < 0){

//                                                    Toast.makeText(mContext,"传屏出错，请重试\n"+info,Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();

                                    H5ChannelInstance.getSingleton().open(mContext);
                                }
                            }
                        },0);

                    }
                }
                return true;
            }else if("get_qrcode".equals(gameInfo.engineEvent)){
                // tv 端获取二维码,手机不需要
//                Intent i = new Intent();
//                i.setPackage("swaiotos.channel.iot");
//                i.setAction("coocaa.intent.action.iot_qrinfo");
//                Messenger messenger = new Messenger(new QrCodeHandler());
//                i.putExtra("messenger", messenger);
//                mContext.startService(i);
                Map<String,String> stringStringMap = new HashMap<>();
                stringStringMap.put("applet",gameInfo.mobileGameUrl);
                Log.d(TAG, "sendGameEvent() get_qrcode applet = [" + gameInfo.mobileGameUrl + "]");
//                stringStringMap.put("RUNTIME_NAV_KEY","RUNTIME_NAV_FLOAT_NP");
                CCenterMangerImpl.getCCenterManger(mContext.getApplicationContext()).getCCodeString(stringStringMap, new CCenterManger.CCenterListener() {
                    @Override
                    public void ccodeCallback(String code) {
                        Log.d(TAG,"ccodeCallback code:"+code);
                        JSONObject data = JSON.parseObject(code);
                        String bind_code = data.getString("showCode");
                        String qr = data.getString("qrCode");
                        EventBus.getDefault().post(new OnQrCodeCBData("onGetQRCode",qr,bind_code));
                    }
                });
                return true;
            }else{
                LogUtil.e("sendGameEvent event + "+ gameInfo.engineEvent +" can only run on mobile." );
                return false;
            }
        }else{
            LogUtil.androidLog(H5CoreOS.TAG, "sendGameEvent: " + gameInfo.toString());


            if(gameInfo!=null){
                SSChannel ssChannel = H5ChannelInstance.getSingleton().getSSChannel(mContext);

                if(null!=ssChannel){
                    try {
                        Session targetSession = ssChannel.getSessionManager().getConnectedSession();
                        LogUtil.d("sendToScreenNativeApp session = ? "+targetSession);
                        if(targetSession!=null){
                            try{
                                if(H5RunType.RUNTIME_NETWORK_FORCE_LAN.equalsIgnoreCase(this.networkType)){
                                    if(!isSameWifi(ssChannel,targetSession)) {
                                        LogUtil.d(SSChannel.STREAM_LOCAL + " not  available! ");
                                        SmartApi.startConnectSameWifi(networkType);
                                        return false;
                                    }
                                }
                                Session mySession = ssChannel.getSessionManager().getMySession();

                                IUserInfo info = SmartApi.getUserInfo();
                                if(info!=null){
                                    gameInfo.userID = info.open_id;
                                    gameInfo.userMobile = info.mobile;
                                    gameInfo.userAvatar = info.avatar;
                                    gameInfo.userNick = info.nickName;
                                }
                                boolean isCreate = false;
                                if("create".equals(gameInfo.engineEvent)){
                                    isCreate = true;
                                }

                                IMMessage message = new IMMessage.Builder()
                                        .setTarget(targetSession)
                                        .setSource(mySession)
                                        .setClientTarget("client-runtime-h5")
                                        .setClientSource("client-runtime-h5")
                                        .setContent(OnGameEngineInfo.toJSONString(gameInfo))
                                        .setType(isCreate?IMMessage.TYPE.TEXT:IMMessage.TYPE.CTR)
                                        .build();
                                //手机端h5游戏需要拉平dongle上的app
                                message.setReqProtoVersion(H5_RUNTIME_PROP_VERSION);

                                if(info!=null){
                                    message.putExtra("mobile",info.mobile);
                                    message.putExtra("open_id",info.open_id);

                                    User user = User.builder().userID(info.open_id).token(info.accessToken)
                                            .mobile(info.mobile).nickName(info.nickName).avatar(info.avatar).build();
                                    message.putExtra("owner", User.encode(user));
                                }
                                message.putExtra("showtips","false");
                                message.putExtra("pkgName",mContext.getPackageName());
                                message.putExtra("className",getClass().getName());

                                if(gameInfo!=null){
                                    Map<String, String> readyLoadUrlParams = URLSplitUtil.urlSplit(gameInfo.tvGameUrl);
                                    String tvGameUrlTag = readyLoadUrlParams.get("h5-runtime-tag");

                                    message.putExtra("log_castType",tvGameUrlTag);
                                    message.putExtra("log_appScreenURI",gameInfo.tvGameUrl);
                                }

                                ssChannel.getIMChannel().send(message, new IMMessageCallback() {
                                    @Override
                                    public void onStart(IMMessage message) {
                                        LogUtil.androidLog(H5CoreOS.TAG, "mobile back onStart: " + message.getContent());
                                        if (callback != null) {
                                            callback.onStart(message);
                                        }
                                    }

                                    @Override
                                    public void onProgress(IMMessage message, int progress) {
                                        LogUtil.androidLog(H5CoreOS.TAG, "mobile back onProgress: " + message.getContent() + "--progress:" + progress);
                                        if (callback != null) {
                                            callback.onProgress(message, progress);
                                        }
                                    }

                                    @Override
                                    public void onEnd(IMMessage message, int code, String info) {
                                        LogUtil.androidLog(H5CoreOS.TAG, "mobile back onEnd: " + message.getContent() + "--code:" + code);
                                        if (callback != null) {
                                            callback.onEnd(message, code, info);
                                        }
                                    }
                                });

                                return true;
                            }catch (Exception e){
                                e.printStackTrace();
                                H5ChannelInstance.getSingleton().open(mContext);
                            }
                        }else{
                            showConnectListViewDialog();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showConnectListViewDialog();
                        H5ChannelInstance.getSingleton().open(mContext);
                    }
                }else{
                    showConnectListViewDialog();
                }
            }
        }

        return false;
    }


    private void showConnectListViewDialog(){
        // 跳转到连接设备列表
        if(!H5SSClientService.isTVOrDongle()){
            if(!SmartApi.isDeviceConnect()){
                LogUtil.d(" SmartApi.isDeviceConnect() = false,showConnectListViewDialog.");
                SmartApi.startConnectDevice();
            }else{
                LogUtil.e(" SmartApi.isDeviceConnect() = true,showConnectListViewDialog,something wrong. ");
            }
        }
    }

    private boolean sendToScreenNativeApp(String clientTarget, String sendUrl, String content, Object remoteVersion, IMMessageCallback callback, Boolean isShowCastFrom){
        SSChannel ssChannel = H5ChannelInstance.getSingleton().getSSChannel(mContext);

        if(null!=ssChannel){
            try {
                Session targetSession = ssChannel.getSessionManager().getConnectedSession();
                LogUtil.d("sendToScreenNativeApp session = ? "+targetSession);
                if(targetSession!=null){
                    return sendIMContent(ssChannel,clientTarget,sendUrl,content, remoteVersion, callback, isShowCastFrom);
                }else{
                    showConnectListViewDialog();
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showConnectListViewDialog();
                H5ChannelInstance.getSingleton().open(mContext);
                return false;
            }
        }else{
            showConnectListViewDialog();
            return false;
        }
    }

    public boolean isSameWifi(SSChannel ssChannel,Session targetSession) {
        if(SmartApi.isMobileRuntime()){
            return SmartApi.isSameWifi();
        }
        boolean available = false;
        try {
            LogUtil.d( "available target: " + targetSession + "\n"
                    + "channel: " + SSChannel.STREAM_LOCAL);
            available = ssChannel.getSessionManager().available(targetSession, SSChannel.STREAM_LOCAL);
        } catch (Exception e) {
            LogUtil.d( e.toString());
        }
        LogUtil.d(SSChannel.STREAM_LOCAL + " available: " + available);
        return available;
    }

    private boolean sendIMContent(SSChannel mSsChannel, String clientTarget, String sendUrl, String content, Object remoteVersion, IMMessageCallback callback, Boolean isShowCastFrom){
        try{
            Session targetSession = mSsChannel.getSessionManager().getConnectedSession();

            if(H5RunType.RUNTIME_NETWORK_FORCE_LAN.equalsIgnoreCase(this.networkType)){
                if(!isSameWifi(mSsChannel,targetSession)) {
                    LogUtil.d(SSChannel.STREAM_LOCAL + " not  available! ");
                    SmartApi.startConnectSameWifi(networkType);
                    return false;
                }
            }
            Session mySession = mSsChannel.getSessionManager().getMySession();

            IMMessage message = IMMessage.Builder.createTextMessage(mySession,targetSession,"client-runtime-h5",clientTarget,content);
            IUserInfo info = SmartApi.getUserInfo();
            if(info!=null){
                message.putExtra("mobile",info.mobile);
                message.putExtra("open_id",info.open_id);
                User user = User.builder().userID(info.open_id).token(info.accessToken)
                        .mobile(info.mobile).nickName(info.nickName).avatar(info.avatar).build();
                message.putExtra("owner", User.encode(user));
            }
            if (isShowCastFrom != null) {
                message.putExtra("showtips", "" + isShowCastFrom);
            } else {
                if (this.isShowCastFrom) {
                    message.putExtra("showtips","true");
                } else {
                    message.putExtra("showtips","false");
                }
            }

            message.putExtra("pkgName",mContext.getPackageName());
            message.putExtra("className",getClass().getName());
            message.putExtra("h5-sourceUrl",sendUrl);
            if (remoteVersion != null && remoteVersion.getClass().equals(Integer.class)) {
                message.setReqProtoVersion((Integer) remoteVersion);
            } else {
                // 应用圈的弹幕版本号是1
                message.setReqProtoVersion(1);
            }
            if(H5RunType.RUNTIME_NETWORK_FORCE_WAN.equalsIgnoreCase(this.networkType)) {
                message.putExtra(SSChannel.FORCE_SSE, "true");
            }

            mSsChannel.getIMChannel().send(message, new IMMessageCallback() {
                @Override
                public void onStart(IMMessage message) {
                    LogUtil.androidLog(H5CoreOS.TAG, "mobile back onStart: " + message.getContent());
                    if (callback != null) {
                        callback.onStart(message);
                    }
                }

                @Override
                public void onProgress(IMMessage message, int progress) {
                    LogUtil.androidLog(H5CoreOS.TAG, "mobile back onProgress: " + message.getContent() + "--progress:" + progress);
                    if (callback != null) {
                        callback.onProgress(message, progress);
                    }
                }

                @Override
                public void onEnd(IMMessage message, int code, String info) {
                    LogUtil.androidLog(H5CoreOS.TAG, "mobile back onEnd: " + message.getContent() + "--code:" + code);
                    if (callback != null) {
                        callback.onEnd(message, code, info);
                    }
                }
            });
            return true;
        }catch (Exception e){
            e.printStackTrace();
            H5ChannelInstance.getSingleton().open(mContext);
            return false;
        }
    }

    private boolean broadCastMessage(SsePushBean.EVENT_TYPE type, H5ContentBean data, Object remoteVersion, IMMessageCallback callback, Boolean isShowCastFrom){
        SSChannel ssChannel = H5ChannelInstance.getSingleton().getSSChannel(mContext);

        if(null!=ssChannel){
            try {
                Session targetSession = ssChannel.getSessionManager().getConnectedSession();
                if(H5RunType.RUNTIME_NETWORK_FORCE_LAN.equalsIgnoreCase(this.networkType)){
                    if(!isSameWifi(ssChannel,targetSession)) {
                        LogUtil.d(SSChannel.STREAM_LOCAL + " not  available! ");
                        SmartApi.startConnectSameWifi(networkType);
                        return false;
                    }
                }
                Session mySession = ssChannel.getSessionManager().getMySession();

                if(mySession!=null){
                    return sendBroadcastContent(ssChannel,type,data, remoteVersion, callback, isShowCastFrom);
                }else{
                    showConnectListViewDialog();
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showConnectListViewDialog();
                H5ChannelInstance.getSingleton().open(mContext);
                return false;
            }
        }else{
            showConnectListViewDialog();
            return false;
        }

    }

    private boolean sendCustomBackgroundMsg(SsePushBean.EVENT_TYPE type, H5ContentBean data, Object remoteVersion, IMMessageCallback callback, Boolean isShowCastFrom){
        SSChannel ssChannel = H5ChannelInstance.getSingleton().getSSChannel(mContext);

        if(null!=ssChannel){
            try {
                Session targetSession = ssChannel.getSessionManager().getConnectedSession();
                if(H5RunType.RUNTIME_NETWORK_FORCE_LAN.equalsIgnoreCase(this.networkType)){
                    if(!isSameWifi(ssChannel,targetSession)) {
                        LogUtil.d(SSChannel.STREAM_LOCAL + " not  available! ");
                        SmartApi.startConnectSameWifi(networkType);
                        return false;
                    }
                }
                Session session = ssChannel.getSessionManager().getMySession();
                LogUtil.d("sendToScreenNativeApp session = ? "+session);
                if(session!=null && targetSession!=null){
                    return sendTVOrDongleContent(ssChannel,session,targetSession,type,data, remoteVersion, callback, isShowCastFrom);
                }else{
                    showConnectListViewDialog();
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showConnectListViewDialog();
                H5ChannelInstance.getSingleton().open(mContext);
                return false;
            }
        }else{
            showConnectListViewDialog();
            return false;
        }
    }

    private boolean sendTVOrDongleContent(SSChannel mSsChannel, Session localSession, Session targetSession, SsePushBean.EVENT_TYPE type, H5ContentBean data, Object remoteVersion, IMMessageCallback callback, Boolean isShowCastFrom){
        try{
            SsePushBean ssePushBean = new SsePushBean();
            ssePushBean.setEvent(type.name());
            ssePushBean.setData(H5ContentBean.toJSONString(data));
            String content = JSON.toJSONString(ssePushBean);
            IMMessage message = IMMessage.Builder.createTextMessage(localSession,targetSession,"client-runtime-h5","client-runtime-h5",content);
            IUserInfo info = SmartApi.getUserInfo();
            if(info!=null){
                message.putExtra("mobile",info.mobile);
                message.putExtra("open_id",info.open_id);
                User user = User.builder().userID(info.open_id).token(info.accessToken)
                        .mobile(info.mobile).nickName(info.nickName).avatar(info.avatar).build();
                message.putExtra("owner", User.encode(user));
            }
            if (isShowCastFrom != null) {
                message.putExtra("showtips", "" + isShowCastFrom);
            } else {
                if (this.isShowCastFrom) {
                    message.putExtra("showtips", "true");
                } else {
                    message.putExtra("showtips", "false");
                }
            }

            if(data!=null){
                message.putExtra("log_castType",data.getH5LogType()==null?"H5_ATMOSPHERE":data.getH5LogType());
                message.putExtra("log_appScreenURI",data.getH5ReceivedUrl());
            }
            message.putExtra("pkgName",mContext.getPackageName());
            message.putExtra("className",getClass().getName());
            if (remoteVersion != null && remoteVersion.getClass().equals(Integer.class)) {
                message.setReqProtoVersion((Integer) remoteVersion);
            } else {
                message.setReqProtoVersion(H5_RUNTIME_PROP_VERSION);
            }


            if(H5RunType.RUNTIME_NETWORK_FORCE_WAN.equalsIgnoreCase(this.networkType)) {
                message.putExtra(SSChannel.FORCE_SSE, "true");
            }

            IMMessageCallback imCallback = new IMMessageCallback() {
                @Override
                public void onStart(IMMessage message) {
                    LogUtil.androidLog(TAG, "mobile back onStart: " + message.getContent());
                    if (callback != null) {
                        callback.onStart(message);
                    }
                }

                @Override
                public void onProgress(IMMessage message, int progress) {
                    LogUtil.androidLog(TAG, "mobile back onProgress: " + message.getContent() + "--progress:" + progress);
                    if (callback != null) {
                        callback.onProgress(message, progress);
                    }
                }

                @Override
                public void onEnd(IMMessage message, int code, String info) {
                    LogUtil.androidLog(TAG, "mobile back onEnd: " + message.getContent() + "--code:" + code);
                    if (callback != null) {
                        callback.onEnd(message, code, info);
                    } else {
                        if (code < 0) {
//                            Toast.makeText(mContext, "传屏出错，请重试\n" + info, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            };
//            if(H5SSClientService.isTVOrDongle(mContext)) {
//                mSsChannel.getIMChannel().send(message, imCallback);
//            } else {
//                mSsChannel.getIMChannel().sendBroadCast(message, imCallback);
//            }
            mSsChannel.getIMChannel().send(message,imCallback);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean sendBroadcastContent(SSChannel mSsChannel, SsePushBean.EVENT_TYPE type, H5ContentBean data, Object remoteVersion, IMMessageCallback callback, Boolean isShowCastFrom){
        try{
            SsePushBean ssePushBean = new SsePushBean();
            ssePushBean.setEvent(type.name());
            ssePushBean.setData(H5ContentBean.toJSONString(data));
            String content = JSON.toJSONString(ssePushBean);
            Session mySession = mSsChannel.getSessionManager().getMySession();
            IMMessage message = IMMessage.Builder.createBroadcastTextMessage(mySession,"client-runtime-h5","client-runtime-h5",content);
            IUserInfo info = SmartApi.getUserInfo();
            if(info!=null){
                message.putExtra("mobile",info.mobile);
                message.putExtra("open_id",info.open_id);
                User user = User.builder().userID(info.open_id).token(info.accessToken)
                        .mobile(info.mobile).nickName(info.nickName).avatar(info.avatar).build();
                message.putExtra("owner", User.encode(user));
            }
            if (isShowCastFrom != null) {
                message.putExtra("showtips", "" + isShowCastFrom);
            } else {
                if (this.isShowCastFrom) {
                    message.putExtra("showtips", "true");
                } else {
                    message.putExtra("showtips", "false");
                }
            }
            message.putExtra("pkgName",mContext.getPackageName());
            message.putExtra("className",getClass().getName());
            if(data!=null){
                message.putExtra("log_castType",data.getH5LogType()==null?"H5_ATMOSPHERE":data.getH5LogType());
                message.putExtra("log_appScreenURI",data.getH5ReceivedUrl());
            }

            if (remoteVersion != null && remoteVersion.getClass().equals(Integer.class)) {
                message.setReqProtoVersion((Integer) remoteVersion);
            } else {
                message.setReqProtoVersion(H5_RUNTIME_PROP_VERSION);
            }

            if(H5RunType.RUNTIME_NETWORK_FORCE_WAN.equalsIgnoreCase(this.networkType)) {
                message.putExtra(SSChannel.FORCE_SSE, "true");
            }

            IMMessageCallback imCallback = new IMMessageCallback() {
                @Override
                public void onStart(IMMessage message) {
                    LogUtil.androidLog(TAG, "mobile back onStart: " + message.getContent());
                    if (callback != null) {
                        callback.onStart(message);
                    }
                }

                @Override
                public void onProgress(IMMessage message, int progress) {
                    LogUtil.androidLog(TAG, "mobile back onProgress: " + message.getContent() + "--progress:" + progress);
                    if (callback != null) {
                        callback.onProgress(message, progress);
                    }
                }

                @Override
                public void onEnd(IMMessage message, int code, String info) {
                    LogUtil.androidLog(TAG, "mobile back onEnd: " + message.getContent() + "--code:" + code);
                    if (callback != null) {
                        callback.onEnd(message, code, info);
                    } else {
                        if (code < 0) {
//                            Toast.makeText(mContext, "传屏出错，请重试\n" + info, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            };
//            if(H5SSClientService.isTVOrDongle(mContext)) {
//                mSsChannel.getIMChannel().send(message, imCallback);
//            } else {
//                mSsChannel.getIMChannel().sendBroadCast(message, imCallback);
//            }
            mSsChannel.getIMChannel().send(message,imCallback);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

}
