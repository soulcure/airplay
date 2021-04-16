package swaiotos.runtime.h5;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coocaa.businessstate.BusinessStateTvReport;
import com.coocaa.businessstate.object.BusinessState;
import com.coocaa.businessstate.object.User;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.IUserInfo;

import java.util.Set;

import swaiotos.channel.iot.IOTChannel;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.runtime.h5.common.util.EmptyUtils;
import swaiotos.runtime.h5.common.util.LogUtil;
import swaiotos.runtime.h5.core.os.model.SendMessageManager;
import swaiotos.runtime.h5.remotectrl.CmdData;

public class H5ChannelInstance implements Runnable {
    public static final String sourceClient = "ss-clientID-SmartScreen";

    public interface OnReceiveMsg {
        void onReceive(String target, String content);
    }

    public static class DeviceInfo {
        public String deviceId;
        public String deviceName;
        public String deviceType;
        public boolean isTempDevice;
    }

    private volatile static H5ChannelInstance instance; //声明成 volatile
    private Context mContext;
    private SSChannel ssChannel;
    private boolean isThreadRunning = false;

    private OnReceiveMsg onReceiveMsg;

    private H5ChannelInstance() {
    }

    public void setOnReceiveMsg(OnReceiveMsg callback) {
        onReceiveMsg = callback;
    }


    public OnReceiveMsg getOnReceiveMsg() {
        return onReceiveMsg;
    }

    public static H5ChannelInstance getSingleton() {
        if (instance == null) {
            synchronized (H5ChannelInstance.class) {
                if (instance == null) {
                    instance = new H5ChannelInstance();
                }
            }
        }
        return instance;
    }

    private void initChannel(Context context) {
        if (ssChannel == null && !isThreadRunning) {
            isThreadRunning = true;
            new Thread(this).start();
        }
    }

    public SSChannel getSSChannel(Context context) {
        synchronized (H5ChannelInstance.this) {
            mContext = context;
            initChannel(mContext);
            return ssChannel;
        }
    }

    public void open(Context context) {
        mContext = context;
        IOTChannel.mananger.open(mContext, getPackageName(), new IOTChannel.OpenCallback() {

            @Override
            public void onConntected(SSChannel channel) {
                LogUtil.androidLog("onConntected");
                synchronized (H5ChannelInstance.this) {
                    ssChannel = channel;
                }
            }

            @Override
            public void onError(String s) {
                LogUtil.androidLog("open channel erro:" + s);
                synchronized (H5ChannelInstance.this) {
                    ssChannel = null;
                }
            }
        });
    }

    @Override
    public void run() {
        IOTChannel.mananger.open(mContext, getPackageName(), new IOTChannel.OpenCallback() {

            @Override
            public void onConntected(SSChannel channel) {
                LogUtil.androidLog("onConntected");
                synchronized (H5ChannelInstance.this) {
                    ssChannel = channel;
                    isThreadRunning = false;
                }
            }

            @Override
            public void onError(String s) {
                LogUtil.androidLog("open channel erro:" + s);
                synchronized (H5ChannelInstance.this) {
                    isThreadRunning = false;
                    ssChannel = null;
                }
            }
        });
    }

    public boolean isLAN() {
        return false;
    }

    public boolean sendMsg(String target, String content) {
        return sendMsg(target, content, null);
    }

    public boolean sendMsg(String target, String content, IMMessageCallback callback) {
        return sendMsg(target, content, callback, true);
    }

    /**
     *
     * @param target
     * @param content
     * @param callback
     * @param needUpgradeTarget 是否需要拉平升级版本
     * @return
     */
    public boolean sendMsg(String target, String content, IMMessageCallback callback, boolean needUpgradeTarget) {
        if (ssChannel == null) {
            LogUtil.e("sendMsg(), channel == null");
            return false;
        }
        try {
            Session sourceSession = ssChannel.getSessionManager().getMySession();
            Session targetSession = ssChannel.getSessionManager().getConnectedSession();
            IMMessage message = IMMessage.Builder.createTextMessage(sourceSession, targetSession,
                    sourceClient, target, content);
            if(needUpgradeTarget) {
                message.setReqProtoVersion(SendMessageManager.H5_RUNTIME_PROP_VERSION);
            }
            IUserInfo info = SmartApi.getUserInfo();
            if (info != null) {
                if (info.mobile != null)
                    message.putExtra("mobile", info.mobile);
                if (info.open_id != null)
                    message.putExtra("open_id", info.open_id);
                User user = User.builder().userID(info.open_id).token(info.accessToken)
                        .mobile(info.mobile).nickName(info.nickName).avatar(info.avatar).build();
                message.putExtra("owner", User.encode(user));
            }
            message.putExtra("showtips", "false");
            ssChannel.getIMChannel().send(message, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    public void sendWebRTCVoice(String content) {
        try {
            final String SOURCE_CLIENT = "ss-clientID-WebRTC-Sound";
            final String TARGET_CLIENT = "com.coocaa.webrtc.airplay.voice";

            Session sourceSession = ssChannel.getSessionManager().getMySession();
            Session target = ssChannel.getSessionManager().getConnectedSession();

            IMMessage message = IMMessage.Builder.createTextMessage(sourceSession, target,
                    SOURCE_CLIENT, TARGET_CLIENT, content);
            message.putExtra(SSChannel.FORCE_SSE, "true");//强制云端
            message.putExtra("target-client", SOURCE_CLIENT);//回复消息target


            LogUtil.e("sendWebRTCVoice message=" + message.encode());
            ssChannel.getIMChannel().send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /**
     *
     * @param targetSid
     * @param target
     * @param content
     * @param callback
     * @return
     */
    public boolean sendSSEMsg(String targetSid, String target, String content,
                              IMMessageCallback callback) {
        if (ssChannel == null) {
            LogUtil.e("sendSSEMsg(), channel == null");
            return false;
        }
        try {
            Session sourceSession = ssChannel.getSessionManager().getMySession();
            Session targetSession = new Session();
            targetSession.setId(targetSid);

            IMMessage message = IMMessage.Builder.createTextMessage(sourceSession, targetSession,
                    sourceClient, target, content);
            message.putExtra(SSChannel.FORCE_SSE, "true");

            IUserInfo info = SmartApi.getUserInfo();
            if (info != null) {
                if (info.mobile != null)
                    message.putExtra("mobile", info.mobile);
                if (info.open_id != null)
                    message.putExtra("open_id", info.open_id);
                User user = User.builder().userID(info.open_id).token(info.accessToken)
                        .mobile(info.mobile).nickName(info.nickName).avatar(info.avatar).build();
                message.putExtra("owner", User.encode(user));
            }
            message.putExtra("showtips", "false");
            ssChannel.getIMChannel().send(message, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public void sendMsg(IMMessage message, IMMessageCallback callback) {
        try {
            ssChannel.getIMChannel().send(message, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean sendBroadcast(String target, String content) {
        return sendBroadcast(target, content, null);
    }

    public boolean sendBroadcast(String target, String content, IMMessageCallback callback) {
        return sendBroadcast(target, content, sourceClient, callback);
    }

    public boolean sendBroadcast(String target, String content, String client, IMMessageCallback callback) {
        if (ssChannel == null) {
            LogUtil.e("sendBroadcast(), channel == null");
            return false;
        }
        try {
            Session sourceSession = ssChannel.getSessionManager().getMySession();
            IMMessage message = IMMessage.Builder.createBroadcastTextMessage(sourceSession,
                    client, target, content);
            ssChannel.getIMChannel().sendBroadCast(message, callback);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean sendStatusBroadcast(String target, String content) {
        try {
            String id = getTypeFromContent(content);
            BusinessStateTvReport.getDefault().updateBusinessState(BusinessState.builder().id(id)
                    .owner(H5CacheState.owner).values(content).build());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private String getTypeFromContent(String content) {
        String type = "H5_PAGE_GAME";
        try {
            JSONObject jsonObject = JSON.parseObject(content);
            JSONObject valuesObject = JSON.parseObject(jsonObject.get("values").toString());
            String pagetype = valuesObject.get("pagetype").toString();
            if(!TextUtils.isEmpty(pagetype)) {
                type = pagetype;
            }
        } catch (Exception e) {
            Log.d("ExtLog", "getTypeFromContent error : " + e.toString());
        }
        return mContext.getPackageName() + "$" + type;
    }

    public boolean sendVideo(String target, String content) {
        return sendVideo(target, content, null);
    }


    public boolean sendVideo(String target, String content, IMMessageCallback callback) {
        if (ssChannel == null) {
            LogUtil.e("sendVideo(), channel == null");
            return false;
        }
        try {
            Session sourceSession = ssChannel.getSessionManager().getMySession();
            Session targetSession = ssChannel.getSessionManager().getConnectedSession();

            IMMessage message = new IMMessage.Builder()
                    .setSource(sourceSession)
                    .setTarget(targetSession)
                    .setClientSource(sourceClient)
                    .setClientTarget(target)
                    .setContent(content)
                    .setType(IMMessage.TYPE.VIDEO)
                    .build();
            message.putExtra("showtips", "false");

            ssChannel.getIMChannel().send(message, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean sendImage(String target, String content) {
        return sendImage(target, content, null);
    }


    public boolean sendImage(String target, String content, IMMessageCallback callback) {
        if (ssChannel == null) {
            LogUtil.e("sendImage(), channel == null");
            return false;
        }
        try {
            Session sourceSession = ssChannel.getSessionManager().getMySession();
            Session targetSession = ssChannel.getSessionManager().getConnectedSession();

            IMMessage message = new IMMessage.Builder()
                    .setSource(sourceSession)
                    .setTarget(targetSession)
                    .setClientSource(sourceClient)
                    .setClientTarget(target)
                    .setContent(content)
                    .setType(IMMessage.TYPE.IMAGE)
                    .build();
            message.putExtra("showtips", "false");

            ssChannel.getIMChannel().send(message, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean sendText(String target, String content, IMMessageCallback callback) {
        return sendText(target, content, null, callback);
    }

    public boolean sendText(String target, String content, String owner, IMMessageCallback callback) {
        return sendText(target, content, owner, callback, -1);
    }

    public boolean sendText(String target, String content, String owner, IMMessageCallback callback, int protoVersion) {
        if (ssChannel == null) {
            LogUtil.e("sendLive(), channel == null");
            return false;
        }
        try {
            Session sourceSession = ssChannel.getSessionManager().getMySession();
            Session targetSession = ssChannel.getSessionManager().getConnectedSession();

            IMMessage message = new IMMessage.Builder()
                    .setSource(sourceSession)
                    .setTarget(targetSession)
                    .setClientSource(sourceClient)
                    .setClientTarget(target)
                    .setContent(content)
                    .setType(IMMessage.TYPE.TEXT)
                    .build();
            message.putExtra("showtips", "false");
            if(protoVersion >= 0) {
                message.setReqProtoVersion(protoVersion);
            }
            if(!TextUtils.isEmpty(owner)) {
                message.putExtra("owner", owner);
            }
            ssChannel.getIMChannel().send(message, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean sendAudio(String target, String content) {
        return sendAudio(target, content, null);
    }

    public boolean sendAudio(String target, String content, IMMessageCallback callback) {
        if (ssChannel == null) {
            LogUtil.e("sendAudio(), channel == null");
            return false;
        }
        try {
            Session sourceSession = ssChannel.getSessionManager().getMySession();
            Session targetSession = ssChannel.getSessionManager().getConnectedSession();

            IMMessage message = new IMMessage.Builder()
                    .setSource(sourceSession)
                    .setTarget(targetSession)
                    .setClientSource(sourceClient)
                    .setClientTarget(target)
                    .setContent(content)
                    .setType(IMMessage.TYPE.AUDIO)
                    .build();
            message.putExtra("showtips", "false");

            ssChannel.getIMChannel().send(message, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean sendCommonMsg(String target, String content, String msgType) {
        return sendCommonMsg(target, content, null);
    }

    public boolean sendCommonMsg(String target, String content, String msgString, String extras, IMMessageCallback callback) {
        if (ssChannel == null) {
            LogUtil.e("sendControl(), channel == null");
            return false;
        }

        try {
            Session sourceSession = ssChannel.getSessionManager().getMySession();
            Session targetSession = ssChannel.getSessionManager().getConnectedSession();
            IMMessage message = new IMMessage.Builder()
                    .setSource(sourceSession)
                    .setTarget(targetSession)
                    .setClientSource(sourceClient)
                    .setClientTarget(target)
                    .setContent(content)
                    .setType(parseType(msgString))
                    .build();
            message.putExtra("showtips", "false");

            int version = SendMessageManager.H5_RUNTIME_PROP_VERSION;
            boolean isController = false;
            JSONObject extraObject = null;
            try {
                extraObject = JSON.parseObject(extras);

                Set<String> keySet = extraObject.keySet();
                if(keySet != null) {
                    for(String key : keySet) {
                        if("protoVersion".equals(key)) {
                            try {
                                version = Integer.parseInt(extraObject.getString("protoVersion"));
                            } catch (Exception e) {
                            }
                        } else {
                            message.putExtra(key, extraObject.getString(key));
                        }
                    }
                }
                if(EmptyUtils.isNotEmpty(extraObject)){
                    isController = extraObject.getBooleanValue("isController");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            message.setReqProtoVersion(version);
            if(!isController){
                IUserInfo info = SmartApi.getUserInfo();
                if (info != null) {
                    User user = User.builder().userID(info.open_id).token(info.accessToken)
                            .mobile(info.mobile).nickName(info.nickName).avatar(info.avatar).build();
                    message.putExtra("owner", User.encode(user));
                }
            }
            ssChannel.getIMChannel().send(message, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private IMMessage.TYPE parseType(String typeString) {
        IMMessage.TYPE mgsType = null;
        if (!TextUtils.isEmpty(typeString)) {
            try {
                mgsType = Enum.valueOf(IMMessage.TYPE.class, typeString);
            } catch (Exception e) {
                LogUtil.w("parseType(), typeString = " + typeString + ", exception = " + e.getMessage());
            }
        }

        return mgsType == null ? IMMessage.TYPE.TEXT : mgsType;
    }


    public DeviceInfo getConnectDeviceInfo() {
        if (ssChannel == null) {
            LogUtil.e("getConnectDeviceInfo(), channel == null");
            return null;
        }
        try {
            Device device = ssChannel.getDeviceManager().getCurrentDevice();

            DeviceInfo info = new DeviceInfo();

            info.deviceId = device.getLsid();
            if (device.getInfo() instanceof TVDeviceInfo) {
                info.deviceName = ((TVDeviceInfo) device.getInfo()).mNickName;
            }
            info.deviceType = device.getZpRegisterType();
            info.isTempDevice = device.isTempDevice();

            return info;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getSpaceId() {
        if (ssChannel == null) {
            LogUtil.e("getSpaceId(), channel == null");
            return null;
        }
        try {
            Session session = ssChannel.getSessionManager().getConnectedSession();
            if (session != null) {
                return session.getExtra("spaceId");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendKey(int code) {
        try {
            CmdData data = new CmdData(code + "", CmdData.CMD_TYPE.KEY_EVENT.toString(), "");
            String cmd = data.toJson();
            Session sourceSession = ssChannel.getSessionManager().getMySession();
            Session targetSession = ssChannel.getSessionManager().getConnectedSession();
            IMMessage message = IMMessage.Builder.createTextMessage(sourceSession, targetSession, sourceClient, "ss-clientID-appstore_12345", cmd);
            ssChannel.getIMChannel().send(message, sendKeyCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private IMMessageCallback sendKeyCallback = new IMMessageCallback() {

        @Override
        public void onStart(IMMessage message) {

        }

        @Override
        public void onProgress(IMMessage message, int progress) {

        }

        @Override
        public void onEnd(IMMessage message, int code, String info) {

        }
    };

    private String getPackageName() {
        return H5SSClientService.isTVOrDongle() ? "swaiotos.channel.iot" : "com.coocaa.smartscreen";
    }

}
