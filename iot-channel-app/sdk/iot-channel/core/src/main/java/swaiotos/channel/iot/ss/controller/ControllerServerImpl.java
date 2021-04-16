package swaiotos.channel.iot.ss.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.skyworthiot.iotssemsg.IotSSEMsgLib;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.analysis.UserBehaviorAnalysis;
import swaiotos.channel.iot.ss.analysis.data.SSeMsgError;
import swaiotos.channel.iot.ss.channel.base.sse.SSEChannel;
import swaiotos.channel.iot.ss.channel.im.IMChannelServer;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.channel.iot.ss.channel.im.local.LocalIMChannel;
import swaiotos.channel.iot.ss.client.event.QueryConnectRoomDeviceEvent;
import swaiotos.channel.iot.ss.config.PortConfig;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.IConnectResult;
import swaiotos.channel.iot.ss.device.PhoneDeviceInfo;
import swaiotos.channel.iot.ss.server.data.JoinToLeaveData;
import swaiotos.channel.iot.ss.server.http.api.HttpResult;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.DeviceUtil;
import swaiotos.channel.iot.utils.NetUtils;
import swaiotos.channel.iot.utils.SameLan;
import swaiotos.channel.iot.utils.SpaceAccountManager;
import swaiotos.channel.iot.utils.SyncObject;
import swaiotos.channel.iot.utils.ThreadManager;
import swaiotos.channel.iot.utils.WifiAccount;

/**
 * @ClassName: Controller
 * @Author: lu
 * @CreateDate: 2020/3/30 2:45 PM
 * @Description:
 */
public class ControllerServerImpl implements ControllerServer, DeviceStateManager.OnDeviceStateChangeListener {
    private static final String SSE_TAG = "swaiot-os-iotchannel-ctr";
    private static final String SSE_SERVER_TAG = "swaiot-os-iotchannel-ctr-server";

    public enum CMD {
        CONNECT,
        DISCONNECT,
        UPDATE,
        GET_CLIENT,
        REPLY,
        ONLINE,
        OFFLINE,
        BIND,
        UNBIND,
        UPDATE_DEVICE_INFO,
        JOIN,
        LEAVE
    }

    public static class Message {
        final String id;
        final long timestamp;
        final CMD cmd;
        final String source;
        final Map<String, String> payload;

        Message(String id, CMD cmd, String source, Map<String, String> payload) {
            this.id = id;
            this.cmd = cmd;
            this.source = source;
            this.timestamp = System.currentTimeMillis();
            this.payload = payload != null ? payload : new LinkedHashMap<String, String>();
        }

        Message(String in) throws JSONException {
            JSONObject object = new JSONObject(in);
            id = object.getString("id");
            cmd = CMD.valueOf(object.getString("cmd"));
            source = object.getString("source");
            timestamp = object.getLong("timestamp");
            payload = new LinkedHashMap<>();
            JSONObject extra = object.getJSONObject("payload");
            Iterator<String> keys = extra.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                payload.put(key, extra.getString(key));
            }
        }

        public Message(CMD cmd, String source) {
            this(cmd, source, null);
        }

        public Message(CMD cmd, String source, Map<String, String> payload) {
            this(UUID.randomUUID().toString(), cmd, source, payload);
        }

        public Message reply(String source) {
            return new Message(id, CMD.REPLY, source, null);
        }

        public String getPayload(String key) {
            return payload.get(key);
        }

        public void putPayload(String key, String value) {
            payload.put(key, value);
        }

        @Override
        public String toString() {
            JSONObject object = new JSONObject();
            try {
                object.put("id", id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                object.put("cmd", cmd.name());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                object.put("source", source);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                object.put("timestamp", timestamp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                object.put("payload", new JSONObject(payload));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return object.toString();
        }
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || TextUtils.isEmpty(intent.getAction())) {
                return;
            }
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                //获取当前的wifi状态int类型数据
                int mWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                if (mWifiState == WifiManager.WIFI_STATE_ENABLED) {
                    performDeviceState(true);
                }
            }
        }
    }


    private SSEChannel mSseChannel;
    private LocalIMChannel mLocalIMChannel;
    private SSContext mSSContext;
    private final Map<String, SyncObject<Message>> mSyncMessages = new LinkedHashMap<>();
    private DeviceStateManager mDeviceStateManager;
    private final List<OnDeviceAliveChangeListener> mOnDeviceAliveChangeListeners = new ArrayList<>();
    private final List<OnDeviceBindStatusListener> mOnDeviceBindStatusListener = new ArrayList<>();
    private final List<OnDeviceDistanceChangeListener> mOnDeviceDistanceChangeListener = new ArrayList<>();
    private SpaceAccountManager mSpaceAccountManager;
    private String mLsId;

    public ControllerServerImpl(SSContext ssContext, SSEChannel sseChannel, LocalIMChannel localIMChannel) {
        mSseChannel = sseChannel;
        mLocalIMChannel = localIMChannel;
        mSSContext = ssContext;
        mDeviceStateManager = new DeviceStateManagerImpl(ssContext);
        mSpaceAccountManager = new SpaceAccountManager(ssContext);
        EventBus.getDefault().register(this);
    }

    private WifiBroadcastReceiver mWifiBroadcastReceiver;

    @Override
    public void open() {
        mDeviceStateManager.open();
        performDeviceState(true);
        mDeviceStateManager.addMyDeviceOnDeviceStateChangeListener(this);
        mSpaceAccountManager.register(mSSContext.getContext());
        mSseChannel.addReceiver(SSE_TAG, mControllerReceiver);
        mSseChannel.addReceiver(SSE_SERVER_TAG, mControllerServerReceiver);
        NetUtils.NetworkReceiver.register(mSSContext.getContext(), mNetworkReceiver);

        mWifiBroadcastReceiver = new WifiBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//监听wifi是开关变化的状态
        mSSContext.getContext().registerReceiver(mWifiBroadcastReceiver, filter);
    }

    @Override
    public void close() {
        mDeviceStateManager.removeMyDeviceOnDeviceStateChangeListener(this);
        mDeviceStateManager.close();
        mSpaceAccountManager.unregister(mSSContext.getContext());
        mSseChannel.removeReceiver(SSE_TAG);
        mSseChannel.removeReceiver(SSE_SERVER_TAG);

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        mSSContext.getContext().unregisterReceiver(mWifiBroadcastReceiver);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(QueryConnectRoomDeviceEvent event) {
        if (mSSContext != null) {
            mSSContext.getSessionManager().queryConnectedRoomDevices();
        }
    }


    @Override
    public void addOnDeviceAliveChangeListener(OnDeviceAliveChangeListener listener) {
        synchronized (mOnDeviceAliveChangeListeners) {
            if (!mOnDeviceAliveChangeListeners.contains(listener)) {
                mOnDeviceAliveChangeListeners.add(listener);
            }
        }
    }

    @Override
    public void removeOnDeviceAliveChangeListener(OnDeviceAliveChangeListener listener) {
        synchronized (mOnDeviceAliveChangeListeners) {
            mOnDeviceAliveChangeListeners.remove(listener);
        }
    }

    @Override
    public void addOnDeviceBindStatusListener(OnDeviceBindStatusListener listener) {
        synchronized (mOnDeviceBindStatusListener) {
            if (!mOnDeviceBindStatusListener.contains(listener)) {
                mOnDeviceBindStatusListener.add(listener);
            }
        }
    }

    @Override
    public void removeOnDeviceBindStatusListener(OnDeviceBindStatusListener listener) {
        synchronized (mOnDeviceBindStatusListener) {
            mOnDeviceBindStatusListener.remove(listener);
        }
    }

    @Override
    public void addOnDeviceDistanceChangeListener(OnDeviceDistanceChangeListener listener) {
        synchronized (mOnDeviceDistanceChangeListener) {
            if (!mOnDeviceDistanceChangeListener.contains(listener)) {
                mOnDeviceDistanceChangeListener.add(listener);
            }
        }
    }

    @Override
    public void removeOnDeviceDistanceChangeListener(OnDeviceDistanceChangeListener listener) {
        synchronized (mOnDeviceDistanceChangeListener) {
            mOnDeviceDistanceChangeListener.remove(listener);
        }
    }

    private SSEChannel.Receiver mControllerServerReceiver = new SSEChannel.Receiver() {
        @Override
        public void onReceive(String message) {
            Log.d("sse", "ControllerServer onReceive:" + message);
            AndroidLog.androidLog("ControllerServer onReceive:" + message);
            try {
                Message msg = new Message(message);
                switch (msg.cmd) {
                    case UPDATE: {
                        handleUpdate(msg);
                        break;
                    }
                    case ONLINE: {
                        handleOnline(msg);
                        break;
                    }
                    case OFFLINE: {
                        handleOffline(msg);
                        break;
                    }
                    case JOIN: {
                        handleJoin(msg);
                        break;
                    }
                    case LEAVE: {
                        handleLeave(msg);
                        break;
                    }
                    case BIND: {
                        handleBind(msg);
                        break;
                    }
                    case UNBIND: {
                        handleUnBind(msg);
                        break;
                    }
                    case UPDATE_DEVICE_INFO: {
                        handleUpdateDeviceInfo(msg);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private SSEChannel.Receiver mControllerReceiver = new SSEChannel.Receiver() {
        @Override
        public void onReceive(String message) {
            Log.d("sse", "Controller onReceive:" + message);
            AndroidLog.androidLog("Controller onReceive:" + message);
            try {
                Message msg = new Message(message);
                switch (msg.cmd) {
                    case CONNECT: {
                        handleConnect(msg);
                        break;
                    }
                    case DISCONNECT: {
                        handleDisconnect(msg);
                        break;
                    }
                    case GET_CLIENT: {
                        handleGetClientVersion(msg);
                        break;
                    }
                    case REPLY: {
                        handleReply(msg);
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    private void handleOnline(Message msg) {
        String lsid = msg.source;
        Log.d("yao", "handleOnline = " + lsid);

        try {
            //在线通知并刷新在线设备数
            mSSContext.getSessionManager().queryConnectedRoomDevices();

            final Session connectedSession = mSSContext.getSessionManager().getConnectedSession();
            if (connectedSession != null && connectedSession.getId().equals(lsid)) { //for mobile
                boolean localConnect = mSSContext.getIMChannel().availableLocal(connectedSession);
                Log.d("yao", "mobile cur local connect status=" + localConnect);
                ThreadManager.getInstance().ioThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connectSSE(connectedSession.getId(), 10000, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        synchronized (mOnDeviceAliveChangeListeners) {
            for (OnDeviceAliveChangeListener listener : mOnDeviceAliveChangeListeners) {
                listener.onDeviceOnline(lsid);
            }
        }
    }

    private void handleOffline(Message msg) {
        String lsid = msg.source;
        Log.d("yao", "handleOffline = " + lsid);

        synchronized (mOnDeviceAliveChangeListeners) {
            for (OnDeviceAliveChangeListener listener : mOnDeviceAliveChangeListeners) {
                listener.onDeviceOffline(lsid);
            }
        }
        try {
            Session connectedSession = mSSContext.getSessionManager().getConnectedSession();
            Session serviceConn = mSSContext.getSessionManager().getServerSession(lsid);

            if (connectedSession != null && connectedSession.getId().equals(lsid)) { //for mobile
                boolean localConnect = mSSContext.getIMChannel().availableLocal(connectedSession);
                Log.d("yao", "mobile cur local connect status=" + localConnect);
                if (localConnect) {
                    Log.d("yao", "mobile当前本地连接正常，不断开连接");
                    mSSContext.getSessionManager().connectChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_SSE,
                            Constants.COOCAA_IOT_CHANNEL_STATE_DISCONNECT);
                } else {
                    //离线通知并刷新在线设备数
                    mSSContext.getSessionManager().queryConnectedRoomDevices();  //手机offline,如果本地连接正常，不刷新dongle设备连接数
                    mSSContext.getIMChannel().closeClient(connectedSession, true);
                    mSSContext.getSessionManager().connectChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_NOT_NET,
                            Constants.COOCAA_IOT_CHANNEL_STATE_DISCONNECT);
//                    mSSContext.getController().disconnect(connectedSession);
                }

            } else if (serviceConn != null) {  //for tv

                boolean localConnect = mSSContext.getIMChannel().availableLocal(serviceConn);
                Log.d("yao", "TV cur local connect status=" + localConnect);
                if (localConnect) {
                    Log.d("yao", "TV当前本地连接正常，不断开连接");
                } else {
                    handleDisconnect(serviceConn);
                    //离线通知并刷新在线设备数
                    mSSContext.getSessionManager().queryConnectedRoomDevices();  //手机offline,如果本地连接正常，不刷新dongle设备连接数
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleBind(Message msg) {
        try {
            String content = msg.getPayload("content");
            JSONObject object = new JSONObject(content);
            String sid = object.getString("sid");
            Log.d("iot", "handleUnBind  sid:" + sid);
            if (TextUtils.isEmpty(sid))
                return;
            synchronized (mOnDeviceBindStatusListener) {
                for (OnDeviceBindStatusListener listener : mOnDeviceBindStatusListener) {
                    listener.onDeviceBind(sid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUnBind(Message msg) {
        try {
            String content = msg.getPayload("content");
            JSONObject object = new JSONObject(content);
            String sid = object.getString("sid");
            Log.d("iot", "handleUnBind  sid:" + sid);

            if (TextUtils.isEmpty(sid))
                return;

            synchronized (mOnDeviceBindStatusListener) {
                for (OnDeviceBindStatusListener listener : mOnDeviceBindStatusListener) {
                    listener.onDeviceUnBind(sid);
                }
            }
            List<Session> sessions = mSSContext.getSessionManager().getServerSessions();
            if (sessions.size() > 0) {
                for (int i = 0; i < sessions.size(); i++) {
                    String clientId = sessions.get(i).getId();
                    if (!TextUtils.isEmpty(clientId) && clientId.equals(sid)) {
                        handleDisconnect(sessions.get(i));
                        break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通知好友已离开
     */
    private void handleLeave(final Message msg) {
        mSSContext.getSessionManager().queryConnectedRoomDevices();
    }

    /**
     * 通知好友进入
     */
    private void handleJoin(final Message msg) {
        mSSContext.getSessionManager().queryConnectedRoomDevices();
    }

    /**
     * 刷新设备列表
     */
    private void handleUpdateDeviceInfo(Message message) {
        try {
            mSSContext.getDeviceManager().onDeviceInfoUpdateList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String content = message.getPayload("content");
        if (!TextUtils.isEmpty(content) && content.contains("re_registration")) {
            //重新注册导致刷新设备列表  并通知关于老帐号对于的好友解绑回调
            String sid = message.source;
            AndroidLog.androidLog("handleUpdateDeviceInfo sid:" + sid);
            if (TextUtils.isEmpty(sid))
                return;
            synchronized (mOnDeviceBindStatusListener) {
                for (OnDeviceBindStatusListener listener : mOnDeviceBindStatusListener) {
                    listener.onDeviceUnBind(sid);
                }
            }
        }

    }

    private void handleReply(Message message) {
        Log.d("yao", "handleReply = " + message.source);

        SyncObject<Message> object;
        synchronized (mSyncMessages) {
            object = mSyncMessages.get(message.id);
            mSyncMessages.remove(message.id);
        }
        if (object != null) {
            object.set(message);
        }
    }


    private Map<String, Long> connectMap = new HashMap<>();
    private Session targetSession;

    @Override
    public Session connect(final String lsid, final long timeout) throws Exception {
        mLsId = lsid;
        targetSession = null;

        Session session = connectLocal(lsid, timeout);
        if (session != null) {
            ThreadManager.getInstance().ioThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        connectSSE(lsid, timeout, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return session;
        } else {
            return connectSSE(lsid, timeout, true);
        }
    }


    public Session connectLocal(final String lsid, long timeout) throws Exception {
        final Object lock = new Object();
        final Session sessionDevice = mSSContext.getDeviceManager().getLocalSessionBySid(lsid);
        AndroidLog.androidLog("----connect----sessionDevice:" + sessionDevice);
        if (sessionDevice != null) {
            final String ip = sessionDevice.getExtra(SSChannel.STREAM_LOCAL);
            if (!TextUtils.isEmpty(ip) && !SameLan.isInSameLAN(ip)) {
                AndroidLog.androidLog("----connect----not isInSameLAN");
                return null;
            }

            mSSContext.getIMChannel().openClient(sessionDevice, new IMChannelServer.TcpClientResult() {
                @Override
                public void onResult(int code, String message) {
                    if (code == 0
                            && !TextUtils.isEmpty(mLsId)
                            && !TextUtils.isEmpty(lsid)
                            && lsid.equals(mLsId)) {
                        targetSession = sessionDevice;
                        mSSContext.getSessionManager().setConnectedSession(targetSession);
                        mSSContext.getSessionManager().addServerSession(targetSession);
                        mSSContext.getDeviceManager().updateCurrentDevice(targetSession);
                        try {
                            Session my = mSSContext.getSessionManager().getMySession();
                            final String mySessionStr = my.encode();
                            connectJoinRoom(lsid, mySessionStr);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        targetSession = null;
                    }
                    synchronized (lock) { // 激活线程
                        lock.notify();
                    }
                }
            });

            synchronized (lock) {
                try {
                    lock.wait(timeout);// 发送完消息后，线程进入等待状态
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (targetSession != null) {
                String start = "local connect success Sid=" + lsid;
                Log.d("yao", start);
                return targetSession;
            }
        }
        return null;
    }

    @Override
    public Session connectSSE(final String lsid, long timeout, final boolean isConnectLocal) throws Exception {
        final RuntimeException[] res = new RuntimeException[1];
        String source = mSSContext.getLSID();
        Log.d("sse", "source:" + source + " lsid:" + lsid);

        if (mSSContext.getDeviceManager().validate(source, lsid)) {
            Session my = mSSContext.getSessionManager().getMySession();
            if (my != null) {
                long curTime = System.currentTimeMillis();
                if (connectMap.containsKey(lsid)) {
                    long time = connectMap.get(lsid);
                    long delay = curTime - time;
                    if (delay < timeout
                            && targetSession != null
                            && lsid.equals(targetSession.getId())) {
                        Log.e("sse", "repeat the connect within the timeout period");
                        return targetSession;
                    }
                }
                connectMap.put(lsid, curTime);

                //先触发进入房间，在去连接，防止连接超时导致进入房间接口未调用
                final String mySessionStr = my.encode();
                Message message = new Message(CMD.CONNECT, source);
                message.payload.put("session", mySessionStr);

                SSEChannel.SendMessageCallBack callBack = new SSEChannel.SendMessageCallBack() {
                    @Override
                    public void onSendErro(IotSSEMsgLib.SSESendResultEnum sseSendResultEnum, String iMMessageStr) {
                        if (sseSendResultEnum == IotSSEMsgLib.SSESendResultEnum.TARGETKNOWERROR) {
                            //其他错误
                            Log.e("sse", "connect 发送失败，其他错误");
                            res[0] = new RuntimeException("connect 发送失败，其他错误");
                        } else if (sseSendResultEnum == IotSSEMsgLib.SSESendResultEnum.TARGETOFFLINEERROR) {
                            //发送失败 ，对方离线
                            Log.e("sse", "connect 发送失败，对方离线");
                            synchronized (mOnDeviceAliveChangeListeners) {
                                for (OnDeviceAliveChangeListener listener : mOnDeviceAliveChangeListeners) {
                                    listener.onDeviceOffline(lsid);
                                }
                            }
                            res[0] = new RuntimeException("connect 发送失败，对方离线");
                        } else if (sseSendResultEnum == IotSSEMsgLib.SSESendResultEnum.TARGETONLINESUCCESS) {
                            //发送成功，对方在线
                            Log.d("sse", "connect 发送成功，对方在线");
                        }
                    }
                };
                long time = System.currentTimeMillis();
                AndroidLog.androidLog("message.id:" + message.id + " time1:" + time);

                mSSContext.getSessionManager().connectingChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_SSE,
                        Constants.COOCAA_IOT_CHANNEL_TYPE_CONNECTING);

                Message reply = sendSync(lsid, message.id, message.toString(), timeout, callBack);

                if (reply != null) {
                    Log.e("sse", "connect success!");
                    mSSContext.getSessionManager().connectingChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_SSE,
                            Constants.COOCAA_IOT_CHANNEL_TYPE_CONNECTED);

                    mSSContext.getSessionManager().connectChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_SSE,
                            Constants.COOCAA_IOT_CHANNEL_STATE_CONNECT);

                    AndroidLog.androidLog("sse-mChannelConnectState-success:" + this);

                    //connect 与 disconnect异步调用 只要connect的lsid 与最新mLsid不一致就不返回
                    if (isConnectLocal
                            && !TextUtils.isEmpty(mLsId)
                            && !TextUtils.isEmpty(lsid)
                            && lsid.equals(mLsId)) {

                        String sessionStr = reply.getPayload("session");
                        targetSession = Session.Builder.decode(sessionStr);
                        mSSContext.getSessionManager().setConnectedSession(targetSession);
                        mSSContext.getSessionManager().addServerSession(targetSession);
                        mSSContext.getDeviceManager().updateCurrentDevice(targetSession);
                        mSSContext.getIMChannel().openClient(targetSession, null);
                        connectJoinRoom(lsid, mySessionStr);
                        UserBehaviorAnalysis.reportSSConnect(mSSContext.getLSID(), lsid, System.currentTimeMillis() - time);
                        AndroidLog.androidLog("message.id:" + message.id + " time2:" + System.currentTimeMillis());
                        return targetSession;
                    } else {
                        targetSession = null;
                        throw new Exception("connect failed!");
                    }
                } else {
                    mSSContext.getSessionManager().connectingChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_SSE,
                            Constants.COOCAA_IOT_CHANNEL_TYPE_CONNECTED);
                    targetSession = null;

                    String err = "SEE connect error Sid=" + lsid;
                    Log.d("yao", err);
                    if (res[0] != null) {
                        UserBehaviorAnalysis.reportSSeMsgError(mSSContext.getLSID(), lsid, message.id, CMD.CONNECT.name(), res[0].getMessage(), SSeMsgError.CONNECT, message.toString());
                        throw res[0];
                    } else {
                        UserBehaviorAnalysis.reportSSeMsgError(mSSContext.getLSID(), lsid, message.id, CMD.CONNECT.name(), "time out for " + timeout, SSeMsgError.CONNECT, message.toString());
                        throw new TimeoutException("time out for " + timeout);
                    }
                }
            }
        }
        Log.e("sse", "validate failed!");
        throw new Exception("validate failed!");
    }


    /**
     * 链接的时候加入房间，加入失败重试一次
     */
    private void connectJoinRoom(final String lsid, final String mySessionStr) {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Device> devices = mSSContext.getDeviceManager().getDevices();
                    for (int i = 0; i < devices.size(); i++) {
                        if (!TextUtils.isEmpty(devices.get(i).getLsid())
                                && !TextUtils.isEmpty(lsid) && lsid.equals(devices.get(i).getLsid())
                                && !TextUtils.isEmpty(devices.get(i).getRoomId())) {
                            String roomId = devices.get(i).getRoomId();

                            HttpResult<JoinToLeaveData> joinToLeaveDataHttpResult =
                                    mSSContext.getServerInterface().joinRoom(mSSContext.getAccessToken(), roomId, mySessionStr);

                            if (!TextUtils.isEmpty(joinToLeaveDataHttpResult.code) &&
                                    !joinToLeaveDataHttpResult.code.equals(Constants.COOCAA_SUCCESS)) {
                                mSSContext.getServerInterface().joinRoom(mSSContext.getAccessToken(), roomId, mySessionStr);
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public Session connect(Device device, long timeout) throws Exception {
        return connect(device.getLsid(), timeout);
    }

    @Override
    public void reConnectSession(Session source, boolean forceClose) {
        if (!mSSContext.getSessionManager().hasServerSession(source)) {
            Log.e("yao", "reConnectSession " + source.toString());

            mSSContext.getSessionManager().addServerSession(source);

            if (forceClose) {
                mSSContext.getIMChannel().closeClient(source, false);
            }

            mSSContext.getIMChannel().openClient(source, null);
            mSSContext.getSessionManager().updateSession(source);
            mSSContext.getSessionManager().queryConnectedRoomDevices();
        }
    }

    private void handleConnect(Message message) {
        Log.d("yao", "handleConnect = " + message.source);
        try {
            final String target = message.source;
            Session source = Session.Builder.decode(message.getPayload("session"));
            if (!mSSContext.getSessionManager().addServerSession(source)) {
                mSSContext.getIMChannel().closeClient(source, false);
                mSSContext.getIMChannel().openClient(source, null);
                mSSContext.getSessionManager().updateSession(source);
            } else {
                mSSContext.getIMChannel().openClient(source, null);
            }
            //mSSContext.getDeviceManager().updateLsid(null, 0);

            //记录source到配置文件
            mSSContext.getSessionManager().saveHandlerConnectSession(source.encode());

            String sid = mSSContext.getLSID();
            Log.d("sse", "handleConnect replay msg source:" + sid);

            message = message.reply(sid);

            SSEChannel.SendMessageCallBack callBack = new SSEChannel.SendMessageCallBack() {
                @Override
                public void onSendErro(IotSSEMsgLib.SSESendResultEnum sseSendResultEnum, String iMMessageStr) {
                    if (sseSendResultEnum == IotSSEMsgLib.SSESendResultEnum.TARGETKNOWERROR) {
                        //其他错误
                        Log.e("sse", "connect replay 发送失败，其他错误");
                    } else if (sseSendResultEnum == IotSSEMsgLib.SSESendResultEnum.TARGETOFFLINEERROR) {
                        //发送失败 ，对方离线
                        Log.e("sse", "connect replay 发送失败，对方离线");
                        synchronized (mOnDeviceAliveChangeListeners) {
                            for (OnDeviceAliveChangeListener listener : mOnDeviceAliveChangeListeners) {
                                listener.onDeviceOffline(target);
                            }
                        }
                    } else if (sseSendResultEnum == IotSSEMsgLib.SSESendResultEnum.TARGETONLINESUCCESS) {
                        //发送成功，对方在线
                        Log.d("sse", "connect replay 发送成功，对方在线");
                    }
                }
            };

            message.putPayload("session", mSSContext.getSessionManager().getMySession().encode());

            mSseChannel.send(target, message.id, SSE_TAG, message.toString(), callBack);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("sse", "replay handleConnect---" + e.getMessage());
        }
    }

    @Override
    public void disconnect(final Session target) {
        AndroidLog.androidLog("-----disconnect-----:" + target + " lsid：" + mLsId);
        mLsId = null;
        String source = mSSContext.getLSID();
        Message message = new Message(CMD.DISCONNECT, source);

        Session my = null;
        try {
            my = mSSContext.getSessionManager().getMySession();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (my != null)
            message.payload.put("session", my.encode());
        try {
            mSseChannel.send(target.getId(), message.id, SSE_TAG, message.toString(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSSContext.getIMChannel().closeClient(target, true);
        mSSContext.getSessionManager().clearConnectedSession();

        mSSContext.getSessionManager().connectChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_NOT_NET,
                Constants.COOCAA_IOT_CHANNEL_STATE_DISCONNECT);
        AndroidLog.androidLog("-mChannelConnectState-local-sse---disconnect");

        //廖舟确认离开房间不需要调用离开接口
//        ThreadManager.getInstance().ioThread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    mSSContext.getServerInterface().leaveRoom(mSSContext.getAccessToken(), "0");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    @Override
    public Session join(String roomId, String sid, long timeout) throws Exception {

        Session my = null;
        try {
            my = mSSContext.getSessionManager().getMySession();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (my != null) {
            HttpResult<JoinToLeaveData> data = mSSContext.getServerInterface().joinRoom(mSSContext.getAccessToken(), roomId, my.encode());
            if (data != null && data.code.equals("0")) {
                return connect(sid, timeout);
            } else {
                throw new Exception("join interface error");
            }
        } else {
            throw new Exception("my session null");
        }
    }

    @Override
    public void join(final String roomId, final String sid, final long timeout, final JoinHandlerCallBack callBack) throws Exception {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                try {
                    callBack.handleJoin(join(roomId, sid, timeout));
                } catch (Exception e) {
                    e.printStackTrace();
                    callBack.handleJoinError(e);
                }
            }
        });
    }

    @Override
    public void leave(final String userQuit) throws Exception {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                HttpResult<JoinToLeaveData> data = mSSContext.getServerInterface().leaveRoom(mSSContext.getAccessToken(), userQuit);
                if (data == null)
                    return;
                AndroidLog.androidLog("---leave:" + data.code);
            }
        });
    }

    @Override
    public DeviceStateManager getDeviceStateManager() {
        return mDeviceStateManager;
    }

    @Override
    public void onDeviceStateUpdate(DeviceState state) {
        try {
            mSSContext.getServerInterface().submitDeviceState(state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public DeviceInfo getDeviceInfo() throws Exception {
        return mSSContext.getDeviceInfo();
    }

    @Override
    public void connectSSETest(final String lsid, final IConnectResult result) throws Exception {

        Message message = new Message(CMD.GET_CLIENT, lsid);
        message.payload.put("client", "com.sficast.capture.app");

        SSEChannel.SendMessageCallBack callBack = new SSEChannel.SendMessageCallBack() {
            @Override
            public void onSendErro(IotSSEMsgLib.SSESendResultEnum sseSendResultEnum, String iMMessageStr) {
                if (sseSendResultEnum == IotSSEMsgLib.SSESendResultEnum.TARGETKNOWERROR) {
                    //其他错误
                    Log.e("sse", "connect 发送失败，其他错误");
                    if (result != null) {
                        try {
                            result.onFail(lsid, -2, "云端通信失败");
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }


                } else if (sseSendResultEnum == IotSSEMsgLib.SSESendResultEnum.TARGETOFFLINEERROR) {
                    //发送失败 ，对方离线
                    Log.e("sse", "connect 发送失败，对方离线");
                    synchronized (mOnDeviceAliveChangeListeners) {
                        for (OnDeviceAliveChangeListener listener : mOnDeviceAliveChangeListeners) {
                            listener.onDeviceOffline(lsid);
                        }
                    }
                    if (result != null) {
                        try {
                            result.onFail(lsid, -1, "云端通信失败");
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (sseSendResultEnum == IotSSEMsgLib.SSESendResultEnum.TARGETONLINESUCCESS) {
                    //发送成功，对方在线
                    Log.d("sse", "connect 发送成功，对方在线");
                    if (result != null) {
                        try {
                            result.onProgress(lsid, 0, "connect 发送成功，对方在线");
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        Message reply = sendSync(lsid, message.id, message.toString(), 5000, callBack);
        if (reply != null) {
            if (result != null) {
                try {
                    String version = reply.getPayload("version");
                    result.onProgress(lsid, 1, "云到dongle,发送成功 & version=" + version);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (result != null) {
                try {
                    result.onFail(lsid, -3, "云到dongle,检查失败");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    @Override
    public void connectLocalTest(String ip, final IConnectResult result) throws Exception {
        Session source = mSSContext.getSessionManager().getMySession();
        Session target = mSSContext.getSessionManager().getConnectedSession();
        final String lsid = target.getId();

        String sourceClient = "ss-clientID-SmartScreen";
        String targetClient = "com.sficast.capture.app";
        String text = "{\"type\": \"connect test\"}";

        IMMessage message = IMMessage.Builder.createTextMessage(source, target, sourceClient, targetClient, text);

        mLocalIMChannel.send(message, new IMMessageCallback() {
            @Override
            public void onStart(IMMessage message) {
                try {
                    if (result != null) {
                        result.onProgress(lsid, 1, "开始发送...");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProgress(IMMessage message, int progress) {
                try {
                    if (result != null) {
                        result.onProgress(lsid, 2, "发送中...");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onEnd(IMMessage message, int code, String info) {
                try {
                    if (result != null) {
                        if (code >= 0) {
                            result.onProgress(lsid, code, info);
                        } else {
                            result.onFail(lsid, code, info);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleDisconnect(Message message) {
        Log.d("yao", "handleDisconnect = " + message.source);

        try {
            //设备disconnet，也调用下获取room-devices接口
            mSSContext.getSessionManager().queryConnectedRoomDevices();

            if (TextUtils.isEmpty(message.getPayload("session")))
                return;
            Session session = Session.Builder.decode(message.getPayload("session"));
            handleDisconnect(session);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleDisconnect(Session session) {
        mSSContext.getIMChannel().removeServerConnect(session.getId());
        mSSContext.getSessionManager().removeServerSession(session);
        try {
            Session connected = mSSContext.getSessionManager().getConnectedSession();
            if (connected != null && connected.equals(session)) {
                mSSContext.getSessionManager().clearConnectedSession();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        mSSContext.getIMChannel().closeClient(session, true);
    }

    @Override
    public int getClientVersion(Session target, String client, long timeout) throws Exception {
        String source = mSSContext.getLSID();
        if (mSSContext.getDeviceManager().validate(source, target.getId())) {
            Message message = new Message(CMD.GET_CLIENT, source);
            message.putPayload("client", client);
            Message reply = sendSync(target.getId(), message.id, message.toString(), timeout, null);
            if (reply != null) {
                try {
                    String version = reply.getPayload("version");
                    return Integer.valueOf(version);
                } catch (Exception e) {
                    e.printStackTrace();
                    return -1;
                }
            } else {
                throw new TimeoutException("time out for " + timeout);
            }
        }
        throw new Exception("validate failed!");
    }

    private void handleGetClientVersion(Message message) {
        Log.d("yao", "handleGetClientVersion = " + message.source);

        String target = message.source;
        String client = message.getPayload("client");
        int version = mSSContext.getClientManager().getClientVersion(client);
        message = message.reply(mSSContext.getLSID());
        try {
            message.putPayload("version", String.valueOf(version));
            mSseChannel.send(target, message.id, SSE_TAG, message.toString(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUpdate(Message message) {
        Log.d("yao", "handleUpdate = " + message.source);

        String content = message.getPayload("content");
        DeviceState deviceState = DeviceState.parse(content);
        if (deviceState != null) {
            Session session = deviceState.toSession();
            try {
                if (mSSContext.getSessionManager().available(session, SSChannel.STREAM_LOCAL)) {
                    Log.d("sse", "update session no need reConnect");
                    return;
                }

                if (mSSContext.getDeviceInfo() != null
                        && mSSContext.getDeviceInfo() instanceof PhoneDeviceInfo) { //mobile
                    if (mSSContext.getSessionManager().getConnectedSession() != null) {
                        mSSContext.getIMChannel().reOpenLocalClient(session);
                    }
                } else { //tv
                    if (session != null
                            && !TextUtils.isEmpty(session.getId())
                            && mSSContext.getSessionManager().hasServerSession(session)) {
                        Log.e("sse", "update session 开始重连");
                        AndroidLog.androidLog("update session 开始重连");
                        mSSContext.getIMChannel().reOpenLocalClient(session);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("sse", "update session 重连失败" + e.getMessage());
            }
            AndroidLog.androidLog("SSE handleUpdate:" + session.toString());
            Log.d("sse", "SSE handleUpdate...1111" + session.toString());
            mSSContext.getSessionManager().updateSession(session);
            getDeviceStateManager().updateDeviceState(message.timestamp, deviceState);
        }

    }

    private Message sendSync(String target, String msgId, String message, long timeout,
                             SSEChannel.SendMessageCallBack callBack) {
        SyncObject<Message> object;
        synchronized (mSyncMessages) {
            object = mSyncMessages.get(msgId);
            if (object == null) {
                object = new SyncObject<>();
                mSyncMessages.put(msgId, object);
                try {
                    mSseChannel.send(target, msgId, SSE_TAG, message, callBack);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return object.get(timeout);
    }

    private final NetUtils.NetworkReceiver mNetworkReceiver = new NetUtils.NetworkReceiver() {
        @Override
        public void onConnected() {
            performDeviceState(true);
            if (mSSContext != null)
                mSSContext.getDeviceManager().updateLsid(null, 1); //add wyh 在线以后拉去设备列表
        }

        @Override
        public void onDisconnected() {
            performClearDeviceState();
        }
    };

    @Override
    public void onSpaceAccount(String spaceAccount) {

        String spaceId = null;
        try {
            JSONObject jsonObject = new JSONObject(spaceAccount);
            String bindStatus = jsonObject.getString("bind_status");
            AndroidLog.androidLog("spaceAccount---onSpaceAccount:" + bindStatus);
            if (!TextUtils.isEmpty(bindStatus) && bindStatus.equals("1")) {
                //bindStatus 不为空 且 绑定状态 获取空间id
                spaceId = jsonObject.getString("space_id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AndroidLog.androidLog("spaceAccount---onSpaceAccount:" + spaceId);

        if (TextUtils.isEmpty(spaceId)) {
            return;
        }

        getDeviceStateManager().updateConnective(Constants.COOCAA_IOT_SPACE_ID, spaceId, false);  //空间ID

        performDeviceState(false);
    }

    @Override
    public String getSpaceAccount() {
        String spaceAccount = mSpaceAccountManager.getSpaceAccount(mSSContext.getContext());
        if (!TextUtils.isEmpty(spaceAccount)) {
            try {
                JSONObject jsonObject = new JSONObject(spaceAccount);
                String bindStatus = jsonObject.getString("bind_status");
                AndroidLog.androidLog("spaceAccount---bindStatus:" + bindStatus);
                if (!TextUtils.isEmpty(bindStatus) && bindStatus.equals("1")) {
                    //bindStatus 不为空 且 绑定状态 获取空间id
                    return jsonObject.getString("space_id");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /***
     * 更新deviceState信息
     * @param auto 网络变化以及初始化都为true
     */
    private void performDeviceState(boolean auto) {

        if (auto) {
            String spaceId = getSpaceAccount();    //空间ID
            if (!TextUtils.isEmpty(spaceId)) {
                getDeviceStateManager().updateConnective(Constants.COOCAA_IOT_SPACE_ID, spaceId, false);
            }
        }

        //devicestate update
        String ip = NetUtils.getLocalAddress(mSSContext.getContext());
        int port = PortConfig.getLocalServerPort(mSSContext.getContext().getPackageName());
        String type = DeviceUtil.getNetworkType(mSSContext.getContext());

        WifiAccount.WifiAcc wifiAcc = WifiAccount.getCurWifiPassword(mSSContext.getContext());

        try {
            String ssId = wifiAcc.ssid;
            getDeviceStateManager().updateConnective("ssid", ssId, false);  //添加WIFI ssid
            UserBehaviorAnalysis.wifiSSID = ssId;
        } catch (Exception e) {
            e.printStackTrace();
        }

        getDeviceStateManager().updateConnective("password", wifiAcc.password, false); //添加WIFI password
        getDeviceStateManager().updateConnective("net", type, false); //添加网络类型

        //DeviceState update
        getDeviceStateManager().updateConnective(SSChannel.IM_CLOUD, mSSContext.getLSID(), false);
        getDeviceStateManager().updateConnective(SSChannel.IM_LOCAL, ip + ":" + port, false);
        getDeviceStateManager().updateConnective(SSChannel.STREAM_LOCAL, ip, false);
        getDeviceStateManager().updateConnective(SSChannel.ADDRESS_LOCAL, ip, true);  //true通知SSE下发更新
    }

    private void performClearDeviceState() {
        getDeviceStateManager().updateConnective("ssid", "", false);  //移除WIFI ssid
        getDeviceStateManager().updateConnective("password", "", false); //移除WIFI password
        getDeviceStateManager().updateConnective("net", "", false); //移除网络类型
        getDeviceStateManager().updateConnective(Constants.COOCAA_IOT_SPACE_ID, "", false); //移除空间 id
        //DeviceState update
        getDeviceStateManager().updateConnective(SSChannel.IM_CLOUD, "", false);
        getDeviceStateManager().updateConnective(SSChannel.IM_LOCAL, "", false);
        getDeviceStateManager().updateConnective(SSChannel.STREAM_LOCAL, "", false);
        getDeviceStateManager().updateConnective(SSChannel.ADDRESS_LOCAL, "", false);

    }
}
