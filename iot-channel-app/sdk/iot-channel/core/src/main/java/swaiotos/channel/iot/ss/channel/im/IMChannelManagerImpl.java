package swaiotos.channel.iot.ss.channel.im;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;
import swaiotos.channel.iot.ss.server.data.MessageRoomData;
import swaiotos.channel.iot.ss.server.data.RoomHasOnline;
import swaiotos.channel.iot.ss.server.http.api.HttpResult;
import swaiotos.channel.iot.ss.server.http.api.HttpSubscribe;
import swaiotos.channel.iot.ss.server.http.api.HttpThrowable;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.ss.session.SessionManager;
import swaiotos.channel.iot.utils.NetUtils;
import swaiotos.channel.iot.utils.SyncObject;

/**
 * @ClassName: IMChannelImpl
 * @Author: lu
 * @CreateDate: 2020/4/10 3:53 PM
 * @Description:
 */
public class IMChannelManagerImpl implements IMChannelManager, SessionManager.OnSessionUpdateListener {
    private static final String TAG = "im";
    private IMChannel mLocalChannel, mCloudChannel;
    private SSContext mSSContext;
    private Receiver mReceiver;
    private Context mContext;

    private Map<String, SyncObject<IMMessage>> mSyncMessages = new LinkedHashMap<>();

    public IMChannelManagerImpl(Context context, SSContext ssContext, IMChannel localIMChannel, IMChannel cloudIMChannel) {
        mContext = context;
        mSSContext = ssContext;
        mLocalChannel = localIMChannel;
        mCloudChannel = cloudIMChannel;

        openLocalServer();
    }

    private IMChannel switchChannel(IMChannel channel, IMMessage message) {
        IMChannel _c = channel;
        if (channel == mLocalChannel) {
            channel = mCloudChannel;
            message.putExtra(SSChannel.FORCE_SSE, "true");
        } else {
            channel = mLocalChannel;
        }
        Log.d(TAG, "send with " + _c + " failed, switch to " + channel + " retry!");
        return channel;
    }

    private void performSend(IMMessage message, IMMessageCallback callback) throws Exception {
        String data = message.encode();

        String isSSE = message.getExtra(SSChannel.FORCE_SSE);
        boolean bSse = Boolean.parseBoolean(isSSE);  //消息强制走SSE

        if (message.isBroadcastMessage()) {
            /*if (!bSse && mLocalChannel.serverSend(message, callback)) {
                Log.d(TAG, "broadcast send to serverSend" + "  data:" + data);
            }*/

            List<Session> sessions = mSSContext.getSessionManager().getServerSessions();
            //List<String> hasSendList = mLocalChannel.serverSendList();
            for (Session session : sessions) {
                /*boolean hasSend = false;
                if (hasSendList != null && hasSendList.size() > 0) {
                    String sid = session.getId();
                    for (String item : hasSendList) {
                        if (item.equals(sid)) {
                            hasSend = true;
                            break;
                        }
                    }
                }
                if (hasSend) {
                    Log.d(TAG, "broadcast send to serverSend  has send so continue");
                    continue;
                }*/

                IMChannel channel = getChannel(session, message, bSse);
                Log.d(TAG, "broadcast send to " + channel.type() + "  data:" + data);
                try {
                    channel.send(session, message, callback);
                } catch (Exception e) {
                    e.printStackTrace();
                    channel = switchChannel(channel, message);
                    channel.send(session, message, callback);
                }
            }
        } else {
            if (!bSse && mLocalChannel.serverSend(message, callback)) {
                Log.d(TAG, "single send to serverSend" + "  data:" + data);
                return;
            }

            IMChannel channel = getChannel(message, bSse);
            Log.d(TAG, "single send to " + channel.type() + "  data:" + data);
            try {
                channel.send(message.getTarget(), message, callback);
            } catch (Exception e) {
                e.printStackTrace();
                channel = switchChannel(channel, message);
                channel.send(message.getTarget(), message, callback);
            }
        }
    }

    @Override
    public void send(IMMessage message, IMMessageCallback callback) throws Exception {
        if (validate(message)) {
            performSend(message, callback);
        }
    }

    @Override
    public void send(IMMessage message) throws Exception {
        send(message, null);
    }

    @Override
    public IMMessage sendSync(IMMessage message, IMMessageCallback callback, long timeout) throws Exception {
        if (validate(message)) {
            String messageId = message.getId();
            SyncObject<IMMessage> reply;
            synchronized (mSyncMessages) {
                reply = mSyncMessages.get(messageId);
                if (reply == null) {
                    reply = new SyncObject<>();
                    mSyncMessages.put(messageId, reply);
                    performSend(message, callback);
                }
            }
            return reply.get(timeout);
        }
        return null;
    }

    @Override
    public IMMessage sendSync(IMMessage message, long timeout) throws Exception {
        return sendSync(message, null, timeout);
    }

    @Override
    public void reset(String sid, String token) throws Exception {
        mSSContext.reset(sid, token);
    }

    @Override
    public void reset(String sid, String token, String userId) throws Exception {
        mSSContext.reset(sid, token, userId);
    }

    @Override
    public void sendBroadCast(final IMMessage message, final IMMessageCallback callback) throws Exception {
        MessageRoomData messageRoomData = new MessageRoomData();
        messageRoomData.setId(message.getId());
        MessageRoomData.MessageData msgData = new MessageRoomData.MessageData();
        msgData.setId(message.getId());
        msgData.setClient_source(message.getClientSource());
        msgData.setClient_target(message.getClientTarget());
        msgData.setExtra(message.getExtra());
        msgData.setReply(false);
        msgData.setContent(message.getContent());
        msgData.setType(message.getType().name());
        messageRoomData.setData(JSONObject.toJSONString(msgData));

        mSSContext.getServerInterface().sendBroadCastRoomMessage(messageRoomData, new HttpSubscribe<HttpResult<RoomHasOnline>>() {
            @Override
            public void onSuccess(HttpResult<RoomHasOnline> result) {
                if (callback != null) {
                    if (result != null && !TextUtils.isEmpty(result.code)) {
                        if (result.code.equals(Constants.COOCAA_SUCCESS)) {
                            if (result.data.getHostOnline() == Constants.COOCAA_ONLIEN) {
                                callback.onEnd(message, Constants.COOCAA_ONLIEN, "host online");
                            } else {
                                callback.onEnd(message, Constants.COOCAA_OFFLINE, "host offline");
                            }
                        } else {
                            callback.onEnd(message, Integer.parseInt(result.code), result.msg);
                        }

                    } else {
                        callback.onEnd(message, -1, "Other errors");
                    }
                }
            }

            @Override
            public void onError(HttpThrowable error) {
                if (callback != null)
                    callback.onEnd(message, -2, "http request exception");
            }
        });
    }

    @Override
    public String fileService(String path) throws Exception {
        try {
            return mSSContext.getWebServer().uploadFile(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onReceive(IMChannel channel, IMMessage message) {
        if (channel != null) {
            Log.d(TAG, "onReceive[" + channel.type() + "] " + message);
        } else {
            Log.d(TAG, "onReceive [dataChannelClient] " + message);
        }

        SyncObject<IMMessage> reply;
        String messageId = message.getId();
        synchronized (mSyncMessages) {
            reply = mSyncMessages.get(messageId);
            mSyncMessages.remove(messageId);
        }
        if (reply != null) {
            reply.set(message);
        } else {
            if (mReceiver != null) {
                mReceiver.onReceive(channel, message);
            }
        }
    }

    @Override
    public void open() {
        performOpen();
        try {
            mSSContext.getSessionManager().addServerSessionOnUpdateListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openLocalServer() {
        try {
            mLocalChannel.open();
            mLocalChannel.setReceiver(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void performOpen() {
        try {
            mCloudChannel.open();
            mCloudChannel.setReceiver(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            mSSContext.getSessionManager().removeServerSessionOnUpdateListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        performClose();
    }

    @Override
    public boolean isConnectSSE() {
        boolean res = mCloudChannel.available();
        if (!res && NetUtils.isConnected(mContext)) {
            Log.d("yao", "CloudChannel reOpenSSE");
            mCloudChannel.reOpenSSE();
        }
        return res;
    }

    @Override
    public void removeServerConnect(String sid) {
        mLocalChannel.removeServerConnect(sid);
    }

    private void performClose() {
        try {
            mLocalChannel.setReceiver(null);
            mLocalChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mCloudChannel.setReceiver(null);
            mCloudChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openClient(Session session, TcpClientResult callback) {
        mLocalChannel.openClient(session, callback);
        mCloudChannel.openClient(session, callback);
    }


    @Override
    public void reOpenLocalClient(Session session) {
        mLocalChannel.reOpenLocalClient(session);
    }


    @Override
    public void reOpenSSE() {
        mCloudChannel.reOpenSSE();
    }


    @Override
    public boolean available(Session session) {
        return mLocalChannel.available(session) || mCloudChannel.available(session);
    }

    @Override
    public void closeClient(Session session, boolean forceClose) {
        mLocalChannel.closeClient(session, forceClose);
        mCloudChannel.closeClient(session, forceClose);
    }

    @Override
    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    public void onSessionConnect(Session session) {

    }

    @Override
    public void onSessionUpdate(Session session) {
        //closeClient(session);
        openClient(session, null);
    }

    @Override
    public void onSessionDisconnect(Session session) {

    }

    @Override
    public boolean availableLocal(Session session) {
        return mLocalChannel.available(session);
    }

    @Override
    public boolean availableCloud(Session session) {
        return mCloudChannel.available(session);
    }

    private IMChannel getChannel(Session target, IMMessage message, boolean forceSSE) {
        if (!forceSSE && mLocalChannel.available(target)) {
            Log.d("yao", "getChannel is LocalChannel...");
            return mLocalChannel;
        } else {
            Log.d("yao", "getChannel is CloudChannel...");
            message.putExtra(SSChannel.FORCE_SSE, "true");
            return mCloudChannel;
        }
    }

    private IMChannel getChannel(IMMessage message, boolean forceSSE) {
        return getChannel(message.getTarget(), message, forceSSE);
    }

    private boolean validate(IMMessage message) throws Exception {
        String isSSE = message.getExtra(SSChannel.FORCE_SSE);
        boolean bSse = Boolean.parseBoolean(isSSE);  //消息强制走SSE
        if (bSse) {
            return true;
        }

        String target = message.getTarget().getId();
        Session connectedSession = mSSContext.getSessionManager().getConnectedSession();
        DeviceInfo deviceInfo = mSSContext.getDeviceInfo();

        if (!message.isBroadcastMessage()) {
            if (deviceInfo instanceof TVDeviceInfo) {
                if (hasInServerSession(target) || hasInServerSend(target)) {
                    return true;
                }
            } else if (connectedSession != null && connectedSession.getId().equals(target)) {
                return true;
            }
        } else {

            Log.d("yao", "broadcast to message will not check here");
            return true;
        }
        Log.e("yao", "send to message check validate failure...");
        return false;
    }

    private boolean hasInServerSession(String target) {
        try {
            List<Session> serverSessions = mSSContext.getSessionManager().getServerSessions();
            for (int i = 0; i < serverSessions.size(); i++) {
                if (!TextUtils.isEmpty(serverSessions.get(i).getId()) && serverSessions.get(i).getId().equals(target)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean hasInServerSend(String target) {
        try {
            List<String> hasSendList = mLocalChannel.serverSendList();
            for (int i = 0; i < hasSendList.size(); i++) {
                String item = hasSendList.get(i);
                if (item.equals(target)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
