package swaiotos.channel.iot.im;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.sdk.entity.IMMessage;
import com.coocaa.sdk.entity.Session;
import com.skyworth.dpclientsdk.ConnectState;
import com.skyworth.dpclientsdk.StreamSourceCallback;
import com.skyworth.dpclientsdk.TcpClient;
import com.skyworthiot.iotssemsg.IotSSEMsgLib;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import swaiotos.channel.iot.SkyServer;
import swaiotos.channel.iot.callback.ConnectCallback;
import swaiotos.channel.iot.callback.ConnectStatusListener;
import swaiotos.channel.iot.callback.NotifyListener;
import swaiotos.channel.iot.callback.ResultListener;
import swaiotos.channel.iot.config.PortConfig;
import swaiotos.channel.iot.db.bean.Device;
import swaiotos.channel.iot.db.helper.DeviceHelper;
import swaiotos.channel.iot.entity.VersionCheck;
import swaiotos.channel.iot.sse.SSEPushModel;
import swaiotos.channel.iot.ui.DialogActivity;
import swaiotos.channel.iot.utils.DeviceUtils;

public class IMChannel {
    private static final String TAG = "yao";

    private static final String SSE_TAG = "swaiot-os-iotchannel-ctr";
    private static final String SSE_SERVER_TAG = "swaiot-os-iotchannel-ctr-server";
    public static final String SSE_MSG_TAG = "swaiot-os-iotchannel";

    public static final String FORCE_SSE = "force-sse";
    public static final String PROTO_VERSION = "proto-version";
    public static final String STREAM_LOCAL = "stream-local";  //局域网Stream通道标识


    private String mSid;
    private final Session mySession;
    private Session targetSession;

    private final Map<String, SyncObject<Message>> mSyncMessages = new LinkedHashMap<>();

    private TcpClient tcpClient;
    private final SSEPushModel sseClient;

    private final Context mContext;
    private final SkyServer skyServer;
    private final Handler mHandler;
    private ConnectStatusListener mConnectListener;
    private int connectStatus = 0;
    private final List<SSEPushModel.LoginCallback> mSSECallbackList;
    private ConnectCallback mConnectCallBack;
    /**
     * 协议监听
     */
    private final Map<String, NotifyListener> mNotifyListenerHap;

    /**
     * 协议消息派发
     */
    private final ConcurrentHashMap<String, ResultListener> mCommonListener;

    private final StreamSourceCallback mStreamSourceCallback = new StreamSourceCallback() {
        @Override
        public void onConnectState(ConnectState connectState) {
            Log.e(TAG, "StreamSourceCallback onConnectState : " + connectState);

            if (connectState == ConnectState.CONNECT) {
                startHeartBeat();
                /*if (!TextUtils.isEmpty(ip) && ip.equals(tcpClient.getIp())) {
                    //todo this
                }*/
                //第二位 local
                if (mConnectListener != null) {
                    connectStatus = connectStatus | 1 << 1;
                    mConnectListener.onConnectStatus(connectStatus, "local tcpClient connect success");
                }

                if (mConnectCallBack != null) {
                    mConnectCallBack.onSuccess(null);
                    mConnectCallBack = null;
                }

                loginLocal();
            } else {
                stopHeartBeat();

                if (mConnectListener != null) {
                    connectStatus = connectStatus & 1;
                    mConnectListener.onConnectStatus(connectStatus, "local tcpClient connect fail");
                }
                if (mConnectCallBack != null) {
                    mConnectCallBack.onConnectFail(-1, "local tcpClient connect fail");
                    mConnectCallBack = null;
                }

                try {
                    /*Session target = ssContext.getSessionManager().getConnectedSession();
                    if (target != null) {
                        String ip = target.getExtra(SSChannel.STREAM_LOCAL);

                        if (!TextUtils.isEmpty(ip) && ip.equals(tcpClient.getIp())) {
                            ssContext.getSessionManager().connectChannelSessionState(
                                    Constants.COOCAA_IOT_CHANNEL_TYPE_LOCAL, Constants.COOCAA_IOT_CHANNEL_STATE_DISCONNECT);
                        }

                    }*/

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onData(String s) {
            notifyMessage(s);
        }

        @Override
        public void onData(byte[] bytes) {
            String msg = new String(bytes);
            notifyMessage(msg);
        }

        @Override
        public void ping(String s) {
            Log.d(TAG, "socket client receive ping---" + s);
        }

        @Override
        public void pong(String s) {
            Log.d(TAG, "socket client receive pong---" + s);
            heartBeatCount--;
        }
    };

    public IMChannel(SkyServer server) {
        mContext = server;
        skyServer = server;
        mySession = new Session();
        sseClient = new SSEPushModel(server);

        mHandler = new Handler(Looper.getMainLooper());
        mNotifyListenerHap = new ConcurrentHashMap<>();
        mCommonListener = new ConcurrentHashMap<>();
        mSSECallbackList = new ArrayList<>();
        SSEPushModel.LoginCallback sseCallback = new SSEPushModel.LoginCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "sse login  onSuccess sid=" + mSid);
                if (mConnectListener != null) {
                    //SSE使用第一位
                    connectStatus = connectStatus | 1;
                    mConnectListener.onConnectStatus(connectStatus, "sse login success sid=" + mSid);
                }
            }

            @Override
            public void onFail() {
                Log.e(TAG, "sse login  onFail sid=" + mSid);
                if (mConnectListener != null) {
                    connectStatus = connectStatus & 1 << 1;
                    mConnectListener.onConnectStatus(connectStatus, "sse login fail sid=" + mSid);
                }
            }
        };
        mSSECallbackList.add(sseCallback);

        sseClient.setReceiveListener(new SSEPushModel.SSEReceiver() {
            @Override
            public void onReceive(String tag, String message) {
                try {
                    if (TextUtils.isEmpty(tag)) {
                        Log.e(TAG, "sseClient onReceive empty tag and message=" + message);
                        return;
                    }
                    Log.d(TAG, "sseClient onReceive message=" + message);

                    if (SSE_TAG.equals(tag)) {
                        Message msg = new Message(message);
                        if (msg.cmd == Message.CMD.REPLY) {
                            handleReply(msg);
                        }
                    } else if (SSE_SERVER_TAG.equals(tag)) {
                        Message msg = new Message(message);
                        switch (msg.cmd) {
                            case UPDATE:
                                handleUpdate(msg);
                                break;
                            case ONLINE:
                                handleOnline(msg);
                                break;
                            case OFFLINE:
                                handleOffline(msg);
                                break;
                            case UNBIND:
                                handleUnBind(msg);
                                break;

                        }
                    } else {
                        notifyMessage(message);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    public Session getMySession() {
        String ip = DeviceUtils.getLocalIPAddress(mContext);
        mySession.putExtra(STREAM_LOCAL, ip);

        return mySession;
    }

    public String getSid() {
        return mSid;
    }

    public void setSid(String sid) {
        mSid = sid;
        mySession.setId(sid);
    }

    public Session getTargetSession() {
        return targetSession;
    }

    public void addNotifyListener(String targetClient, NotifyListener listener) {
        if (!TextUtils.isEmpty(targetClient) && listener != null) {
            mNotifyListenerHap.put(targetClient, listener);
        }
    }

    public void removeNotifyListener(String targetClient) {
        if (!TextUtils.isEmpty(targetClient)) {
            mNotifyListenerHap.remove(targetClient);
        }
    }

    public void setConnectListener(ConnectStatusListener listener) {
        this.mConnectListener = listener;
    }

    public void loginSSE(String sid, SSEPushModel.LoginCallback callback) {
        mSid = sid;
        mySession.setId(sid);
        if (callback != null) {
            mSSECallbackList.add(callback);
        }
        sseClient.initPushSEE(sid, mSSECallbackList);
    }


    public Session connect(final String lsid, long timeout) throws TimeoutException {
        final Object lock = new Object();
        Device device = DeviceHelper.instance().toQueryDeviceBySid(mContext, lsid);
        if (device != null) {
            String json = device.getZpAttributeJson();
            DeviceState deviceState = DeviceState.parse(json);
            if (deviceState != null) {
                final Session sessionDevice = deviceState.toSession();
                String ip = sessionDevice.getExtra(STREAM_LOCAL);
                if (!TextUtils.isEmpty(ip)) {
                    ConnectCallback listener = new ConnectCallback() {
                        @Override
                        public void onSuccess(Session session) {
                            targetSession = sessionDevice;
                            synchronized (lock) { // 激活线程
                                lock.notify();
                            }
                            // todo join room
                        }

                        @Override
                        public void onConnectFail(int code, String msg) {
                            targetSession = null;
                            synchronized (lock) { // 激活线程
                                lock.notify();
                            }
                        }
                    };

                    if (tcpClient != null) {
                        if (tcpClient.isOpen()) {
                            targetSession = sessionDevice;
                            if (mContext instanceof SkyServer) {
                                SkyServer skyServer = (SkyServer) mContext;
                                skyServer.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onSuccess(null);
                                    }
                                }, 10);
                            }

                        } else {
                            mConnectCallBack = listener;
                            tcpClient.reOpen(ip, PortConfig.STREAM_PORT, mStreamSourceCallback);

                        }
                    } else {
                        mConnectCallBack = listener;
                        tcpClient = new TcpClient(ip, PortConfig.STREAM_PORT, mStreamSourceCallback);
                        tcpClient.open();
                    }

                    synchronized (lock) {
                        try {
                            lock.wait(timeout);// 发送完消息后，线程进入等待状态
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (targetSession != null) {
                        return targetSession;
                    }
                }
            }
        }  //end of local connect

        final RuntimeException[] res = new RuntimeException[1];
        Message message = new Message(Message.CMD.CONNECT, mSid);
        message.payload.put("session", mySession.encode());

        SSEPushModel.SendMessageCallBack callBack = new SSEPushModel.SendMessageCallBack() {
            @Override
            public void onSendErr(IotSSEMsgLib.SSESendResultEnum sseSendResultEnum) {
                if (sseSendResultEnum == IotSSEMsgLib.SSESendResultEnum.TARGETKNOWERROR) {
                    //其他错误
                    Log.e(TAG, "connect 发送失败，其他错误");
                    res[0] = new RuntimeException("connect 发送失败，其他错误");
                } else if (sseSendResultEnum == IotSSEMsgLib.SSESendResultEnum.TARGETOFFLINEERROR) {
                    //发送失败 ，对方离线
                    Log.e(TAG, "connect 发送失败，对方离线");
                    res[0] = new RuntimeException("connect 发送失败，对方离线");
                } else if (sseSendResultEnum == IotSSEMsgLib.SSESendResultEnum.TARGETONLINESUCCESS) {
                    //发送成功，对方在线
                    Log.d(TAG, "connect 发送成功，对方在线");
                    // todo join room
                }
            }
        };
        Message reply = sendSync(lsid, message.id, message.toString(), timeout, callBack);

        if (reply != null) {
            Log.e(TAG, "connect success!");
            String sessionStr = reply.getPayload("session");
            try {
                targetSession = Session.Builder.decode(sessionStr);
                String ip = targetSession.getExtra(STREAM_LOCAL);
                if (!TextUtils.isEmpty(ip)) {
                    tcpClient = new TcpClient(ip, PortConfig.STREAM_PORT, mStreamSourceCallback);
                    tcpClient.open();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            targetSession = null;
            if (res[0] != null) {
                throw res[0];
            } else {
                throw new TimeoutException("time out for " + timeout);
            }
        }

        return targetSession;
    }


    public void sendMessageBySSE(final IMMessage message, final ResultListener listener) {
        if (sseClient.isSSEConnected()) {
            try {
                String data = message.encode();
                String targetSid = message.getTarget().getId();
                sseClient.sendSSEMessage(targetSid, message.getId(), SSE_MSG_TAG, data,
                        new SSEPushModel.SendMessageCallBack() {
                            @Override
                            public void onSendErr(IotSSEMsgLib.SSESendResultEnum sseSendResultEnum) {
                                switch (sseSendResultEnum) {
                                    case TARGETKNOWERROR:
                                        if (listener != null) {
                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    listener.onResult(ResultListener.SSE_UNKNOWN_ERROR, "sse unknown error");
                                                }
                                            });

                                        }
                                        break;
                                    case TARGETOFFLINEERROR:
                                        if (listener != null) {
                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    listener.onResult(ResultListener.SSE_OFFLINE, "sse offline");
                                                }
                                            });
                                        }
                                        break;
                                    case TARGETONLINESUCCESS:
                                        if (listener != null) {
                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    listener.onResult(ResultListener.SUCCESS, "send sse message success");
                                                }
                                            });
                                        }
                                        break;
                                }
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "sendMessageBySSE Fail By IOException!!!");
            }
        } else {
            Log.e(TAG, "sendMessageBySSE Fail By OFFLINE!!!");
        }

    }


    public void sendMessage(IMMessage message, ResultListener callback) {
        String msgId = message.getId();
        String forceSSEStr = message.getExtra(FORCE_SSE);
        boolean forceSSE = Boolean.parseBoolean(forceSSEStr);
        if (forceSSE) {
            sendMessageBySSE(message, callback);
            Log.d(TAG, "sendMessage by forceSSE");
        } else if (tcpClient != null && tcpClient.isOpen()) {
            modifyMessage(message);
            mCommonListener.put(msgId, callback);
            tcpClient.sendData(message.encode().getBytes());
            Log.d(TAG, "sendMessage by local tcpClient");
        } else {
            sendMessageBySSE(message, callback);
            Log.d(TAG, "sendMessage by cloud sse");
        }

    }

    public boolean isSSEConnected() {
        return sseClient.isSSEConnected();
    }

    public void reConnectSSE() {
        sseClient.reConnectSSE(mSid);
    }

    public boolean isLocalConnect() {
        if (tcpClient != null) {
            return tcpClient.isOpen();
        }
        return false;
    }


    public void reConnectLocal() {
        stopHeartBeat();
        if (tcpClient != null) {
            tcpClient.close();
            tcpClient.open();
        }
    }

    public void updateMySession(String ip) {
        if (mySession != null) {
            mySession.putExtra(STREAM_LOCAL, ip);
        }
    }


    public void disconnect(String targetSid) {
        if (!TextUtils.isEmpty(targetSid)
                && targetSession != null
                && targetSid.equals(targetSession.getId())) {
            tcpClient.close();
            targetSession = null;
            connectStatus = 0;

            if (mConnectListener != null) {
                mConnectListener.onTargetSessionUpdate(null);
                mConnectListener.onConnectStatus(connectStatus, "移除连接");
            }
        }

    }

    public void close(String targetSid) {
        if (!TextUtils.isEmpty(targetSid)
                && targetSession != null
                && targetSid.equals(targetSession.getId())) {
            targetSession = null;
            connectStatus = 0;
        }

        if (sseClient != null) {
            sseClient.disconnect();
        }
        if (tcpClient != null) {
            tcpClient.close();
        }


        if (mConnectListener != null) {
            mConnectListener.onTargetSessionUpdate(null);
            mConnectListener.onConnectStatus(connectStatus, "移除连接");
        }

    }


    private IMMessage modifyMessage(IMMessage message) {
        switch (message.getType()) {
            case VIDEO:
            case IMAGE:
            case AUDIO:
            case DOC:
                String path = message.getContent();
                String url = skyServer.fileServer(path);
                message.setContent(url);
                break;
            default:
                break;
        }
        return message;
    }

    private Message sendSync(String target, String msgId, String message, long timeout,
                             SSEPushModel.SendMessageCallBack callBack) {
        SyncObject<Message> object;
        synchronized (mSyncMessages) {
            object = mSyncMessages.get(msgId);
            if (object == null) {
                object = new SyncObject<>();
                mSyncMessages.put(msgId, object);
                try {
                    sseClient.sendSSEMessage(target, msgId, SSE_TAG, message, callBack);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return object.get(timeout);
    }


    private void notifyMessage(String msg) {
        try {
            IMMessage imMessage = IMMessage.Builder.decode(msg);
            notifyMessage(imMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void notifyMessage(IMMessage imMessage) {
        try {
            String targetClient = imMessage.getClientTarget();
            IMMessage.TYPE type = imMessage.getType();
            String key = imMessage.getId();
            final String content = imMessage.getContent();

            if (type == IMMessage.TYPE.DIALOG) {
                //show dialog
                VersionCheck vc = new VersionCheck();
                String msg = "电视目前还不支持该操作，需要更新相关服务，确定下载安装吗?";
                String registerType = imMessage.getExtra("registerType");
                if (!TextUtils.isEmpty(registerType) && registerType.equals("dongle")) {
                    msg = "共享屏目前还不支持该操作，需要更新相关服务，确定下载安装吗?";
                }
                DialogActivity.showDialog(mContext, msg, imMessage, vc);
                return;
            }

            final ResultListener callback = mCommonListener.get(key);
            if (callback != null) {
                Log.d(TAG, "tcpClient sendData callback string data=" + imMessage.toString());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(ResultListener.SUCCESS, content);
                        mCommonListener.remove(key);
                    }
                });
            } else {
                Log.d(TAG, "tcpClient onReceive string data=" + imMessage.toString());

                NotifyListener listener = mNotifyListenerHap.get(targetClient);
                if (listener != null) {
                    listener.OnRec(targetClient, imMessage);
                } else {
                    NotifyListener notifyDefault = skyServer.getNotifyDefaultListener();//aidl 自注册派发和startService派发
                    notifyDefault.OnRec(targetClient, imMessage);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleReply(Message message) {
        Log.d(TAG, "handleReply = " + message.source);

        SyncObject<Message> object;
        synchronized (mSyncMessages) {
            object = mSyncMessages.get(message.id);
            mSyncMessages.remove(message.id);
        }
        if (object != null) {
            object.set(message);
        }
    }


    private void handleUpdate(Message msg) {
        Log.d(TAG, "handleUpdate = " + msg.toString());

        String content = msg.getPayload("content");
        DeviceState deviceState = DeviceState.parse(content);
        if (deviceState != null) {
            Session session = deviceState.toSession();
            if (session != null && !TextUtils.isEmpty(session.getId())) {
                Log.e(TAG, "update session 开始重连");
                String ip = session.getExtra(STREAM_LOCAL);

                if (tcpClient == null) {
                    tcpClient = new TcpClient(ip, PortConfig.STREAM_PORT, mStreamSourceCallback);
                    tcpClient.open();
                } else {
                    tcpClient.reOpen(ip, PortConfig.STREAM_PORT, mStreamSourceCallback);
                }
                targetSession = session;

                if (mConnectListener != null) {
                    mConnectListener.onTargetSessionUpdate(session);
                }
            }
        }
    }


    private void handleOnline(Message msg) {
        String lsid = msg.source;
        Log.d(TAG, "handleOnline = " + msg.toString());

        /*if (mConnectListener != null) {
            connectStatus = connectStatus | 0x11;
            mConnectListener.onConnectStatus(connectStatus, "sse offline sid=" + lsid);
        }*/
    }

    private void handleOffline(Message msg) {
        String lsid = msg.source;
        Log.d(TAG, "handleOffline = " + msg.toString());
        /*if (mConnectListener != null) {
            connectStatus = connectStatus | 0x10;
            mConnectListener.onConnectStatus(connectStatus, "sse offline sid=" + lsid);
        }*/

    }


    private void handleUnBind(Message msg) {
        String lsid = msg.source;
        Log.d(TAG, "handleUnBind = " + msg.toString());
        try {
            String content = msg.getPayload("content");
            JSONObject object = new JSONObject(content);
            String sid = object.optString("sid");
            if (!TextUtils.isEmpty(sid)) {
                disconnect(lsid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        skyServer.reqBindDevice(null);//请求设备列表
    }


    private void loginLocal() {
        if (TextUtils.isEmpty(mSid)) {
            return;
        }
        try {
            JSONObject json = new JSONObject();
            json.put("proto", "login");
            json.put("sid", mSid);
            String login = json.toString();

            tcpClient.sendData(login);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private int heartBeatCount;
    private ScheduledExecutorService heartBeatScheduled;
    private static final int HEART_BEAT_INTERVAL = 5; //心跳间隔5秒
    private static final String HEART_BEAT_STR = "Heart Beat Message";

    /**
     * 开始心跳
     */
    private void startHeartBeat() {
        heartBeatCount = 0;
        Log.d(TAG, "socket client startHeartBeat---");
        if (heartBeatScheduled == null) {
            heartBeatScheduled = Executors.newScheduledThreadPool(1);
            heartBeatScheduled.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    heatBeat();
                }
            }, HEART_BEAT_INTERVAL, HEART_BEAT_INTERVAL, TimeUnit.SECONDS);
        }
    }


    /**
     * 停止心跳
     */
    private void stopHeartBeat() {
        Log.d(TAG, "socket client stopHeartBeat---");
        if (heartBeatScheduled != null) {
            heartBeatScheduled.shutdown();
            heartBeatScheduled = null;
        }
    }


    /**
     * 心跳协议请求
     */
    private void heatBeat() {
        if (heartBeatCount > 3) {
            Log.d(TAG, "socket client heatBeat timeout and reconnect---");
            reConnectLocal();
            return;
        }
        heartBeatCount++;
        tcpClient.ping(HEART_BEAT_STR);
        Log.d(TAG, "socket client heatBeat---" + heartBeatCount);
    }


    /**
     * tcpClient 关闭并重连
     *
     * @param ip
     * @param port
     * @param callback
     */
    public void reOpenConnect(String ip, int port, StreamSourceCallback callback) {
        stopHeartBeat();
        if (tcpClient != null) {
            tcpClient.setCallBack(null);
            tcpClient.close();
            tcpClient.reOpen(ip, port, callback);
        }
    }


}
