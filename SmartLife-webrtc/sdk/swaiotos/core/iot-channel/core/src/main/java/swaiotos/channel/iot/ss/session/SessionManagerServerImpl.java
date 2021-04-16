package swaiotos.channel.iot.ss.session;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.config.PortConfig;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.ss.server.data.RoomDevices;
import swaiotos.channel.iot.ss.server.http.api.HttpResult;
import swaiotos.channel.iot.ss.server.http.api.HttpSubscribe;
import swaiotos.channel.iot.ss.server.http.api.HttpThrowable;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.DeviceUtil;
import swaiotos.channel.iot.utils.IpV4Util;
import swaiotos.channel.iot.utils.NetUtils;
import swaiotos.channel.iot.utils.WifiAccount;

/**
 * @ClassName: SessionManager
 * @Author: lu
 * @CreateDate: 2020/3/27 4:24 PM
 * @Description:
 */
public class SessionManagerServerImpl implements SessionManagerServer {
    private final List<OnSessionUpdateListener> mConnectedSessionOnUpdateListeners = new ArrayList<>();
    private final List<OnSessionUpdateListener> mServerSessionOnUpdateListeners = new ArrayList<>();
    private final List<OnMySessionUpdateListener> mMySessionOnUpdateListeners = new ArrayList<>();
    private final List<OnRoomDevicesUpdateListener> mRoomDevicesUpdateListeners = new ArrayList<>();
    private final Map<String, Session> mServerSessions = new HashMap<>();
    private List<RoomDevice> mRoomDevices = new ArrayList<>();
    private Session mConnectedSession;
    private final Session mMySession = new Session();
    private SSContext mSSContext;
    private int mChannelConnectState = 0;
    private int mChannelConnectingState = 0;

    private NetUtils.NetworkReceiver mNetworkReceiver = new NetUtils.NetworkReceiver() {
        @Override
        public void onConnected() {
            String ip = NetUtils.getLocalAddress(mSSContext.getContext());
            perforMySession(ip);
            queryServerConnectedDevices(); //add wyh:获取当前room下的连接数
            mSSContext.getDeviceManager().updateLsid(null, 1);
        }

        @Override
        public void onDisconnected() {
            perforClearMySession();
            //disConnectedSession(); //colin add
            connectChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_NOT_NET, Constants.COOCAA_IOT_CHANNEL_STATE_DISCONNECT);
        }
    };

    public SessionManagerServerImpl(SSContext ssContext) {
        mSSContext = ssContext;

    }

    @Override
    public void open() {
        String ip = NetUtils.getLocalAddress(mSSContext.getContext());
        perforMySession(ip);

        //dongle业务请求连接设备数
        ScheduledExecutorService connDeviceScheduledExecutorService = Executors.newScheduledThreadPool(1);
        connDeviceScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                queryServerConnectedDevices(); //add wyh:获取当前room下的连接数
            }
        }, 0, 10, TimeUnit.MINUTES);

        NetUtils.NetworkReceiver.register(mSSContext.getContext(), mNetworkReceiver);
        reConnectLocalChannel();
    }

    /**
     * core进程启动的时候，通过mConnectedSession在配置文件存在，初始化本地通道
     */
    private void reConnectLocalChannel() {
        String sessionStr = ShareUtls.getInstance(mSSContext.getContext()).getString(Constants.COOCAA_LAST_CONNECT_SESSION, "");
        AndroidLog.androidLog("connectSessionString:" + sessionStr);
        if (!TextUtils.isEmpty(sessionStr)) {
            try {
                mSSContext.getIMChannel().openClient(Session.Builder.decode(sessionStr), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void perforMySession(String ip) {
        String spaceId = mSSContext.getController().getSpaceAccount();    //空间ID
        if (!TextUtils.isEmpty(spaceId)) {
            updateMySession(Constants.COOCAA_IOT_SPACE_ID, spaceId, false);
        }
        //mySession update
        int port = PortConfig.getLocalServerPort(mSSContext.getContext().getPackageName());
        Log.d("yao", "performMySession:" + ip);

        WifiAccount.WifiAcc wifiAcc = WifiAccount.getCurWifiPassword(mSSContext.getContext());
        String type = DeviceUtil.getNetworkType(mSSContext.getContext());

        updateMySession("ssid", wifiAcc.ssid, false);
        updateMySession("password", wifiAcc.password, false);
        updateMySession("net", type, false);

        updateMySession(SSChannel.IM_CLOUD, mSSContext.getLSID(), false);
        updateMySession(SSChannel.IM_LOCAL, ip + ":" + port, false);
        updateMySession(SSChannel.STREAM_LOCAL, ip, false);
        updateMySession(SSChannel.ADDRESS_LOCAL, ip, true);  //notify 更新

    }

    private void perforClearMySession() {
        Log.d("yao", "perforClearMySession");

        String spaceId = mSSContext.getController().getSpaceAccount();    //空间ID
        if (!TextUtils.isEmpty(spaceId)) {
            updateMySession(Constants.COOCAA_IOT_SPACE_ID, "", false);
        }

        updateMySession("ssid", "", false);
        updateMySession("password", "", false);
        updateMySession("net", "", false);

        updateMySession(SSChannel.IM_CLOUD, "", false);
        updateMySession(SSChannel.IM_LOCAL, "", false);
        updateMySession(SSChannel.STREAM_LOCAL, "", false);
        updateMySession(SSChannel.ADDRESS_LOCAL, "", true); //notify 更新
    }


    @Override
    public Session getMySession() {
        String ip = NetUtils.getLocalAddress(mSSContext.getContext());
        String local = mMySession.getExtra(SSChannel.STREAM_LOCAL);
        Log.d("yao", "getMySession check my ip:" + ip);
        if ((!TextUtils.isEmpty(local)
                && !TextUtils.isEmpty(ip)
                && !local.equals(ip))
                || (TextUtils.isEmpty(local) && !TextUtils.isEmpty(ip)) ) {
            Log.e("yao", "getMySession my ip change to:" + ip);
            perforMySession(ip);
        }
        return mMySession;
    }

    @Override
    public void addConnectedSessionOnUpdateListener(OnSessionUpdateListener listener) throws Exception {
        synchronized (mConnectedSessionOnUpdateListeners) {
            if (!mConnectedSessionOnUpdateListeners.contains(listener)) {
                mConnectedSessionOnUpdateListeners.add(listener);
            }
        }
    }

    @Override
    public void removeConnectedSessionOnUpdateListener(OnSessionUpdateListener listener) throws Exception {
        synchronized (mConnectedSessionOnUpdateListeners) {
            mConnectedSessionOnUpdateListeners.remove(listener);
        }
    }

    @Override
    public void setConnectedSession(Session session) {
        mConnectedSession = session;
        mConnectedSession.putExtra("connectStatus", "" + mChannelConnectState);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (mConnectedSessionOnUpdateListeners) {
                    for (OnSessionUpdateListener listener : mConnectedSessionOnUpdateListeners) {
                        try {
                            listener.onSessionConnect(mConnectedSession);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        mSSContext.post(runnable);
    }

    @Override
    public Session getConnectedSession() throws Exception {
        return mConnectedSession;
    }

    @Override
    public void clearConnectedSession() {
        final Session session = mConnectedSession;
        if (session != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    synchronized (mConnectedSessionOnUpdateListeners) {
                        for (OnSessionUpdateListener listener : mConnectedSessionOnUpdateListeners) {
                            try {
                                listener.onSessionDisconnect(session);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            };
            mSSContext.post(runnable);
        }
        mConnectedSession = null;
//        ShareUtls.getInstance(mSSContext.getContext()).putString("ConnectedSession","");
    }

    @Override
    public void clearConnectedSessionByUser() throws Exception {
        AndroidLog.androidLog("----clearConnectedSessionByUser----user clear connected session--");
        mConnectedSession = null;
        ShareUtls.getInstance(mSSContext.getContext()).putString("ConnectedSession", "");
    }


    /**
     * 网络断开时候 立即通知client端： onSessionDisconnect
     */
    private void disConnectedSession() {
        if (mConnectedSession != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    synchronized (mConnectedSessionOnUpdateListeners) {
                        for (OnSessionUpdateListener listener : mConnectedSessionOnUpdateListeners) {
                            try {
                                AndroidLog.androidLog("----listener---");
                                listener.onSessionDisconnect(mConnectedSession);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            };
            mSSContext.post(runnable);
        }
    }


    @Override
    public void updateMySession(String key, String value, boolean notify) {
        synchronized (mMySession) {
            if (TextUtils.isEmpty(value)) {
                value = "";
            }
            mMySession.putExtra(key, value);
            sessionChanged = true;

        }
        if (notify) {
            dispatchMySessionUpdate();
        }
    }

    private final Runnable dispatchMySessionUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mMySessionOnUpdateListeners) {
                for (OnMySessionUpdateListener listener : mMySessionOnUpdateListeners) {
                    listener.onMySessionUpdate(getMySession());
                }
            }
        }
    };

    private void dispatchMySessionUpdate() {
//        mSSContext.removeCallbacks(dispatchMySessionUpdateRunnable);
        mSSContext.postDelay(dispatchMySessionUpdateRunnable, 0);
    }

    @Override
    public void addOnMySessionUpdateListener(OnMySessionUpdateListener listener) {
        synchronized (mMySessionOnUpdateListeners) {
            if (!mMySessionOnUpdateListeners.contains(listener)) {
                mMySessionOnUpdateListeners.add(listener);
            }
        }
    }

    @Override
    public void removeOnMySessionUpdateListener(OnMySessionUpdateListener listener) {
        synchronized (mMySessionOnUpdateListeners) {
            mMySessionOnUpdateListeners.remove(listener);
        }
    }

    @Override
    public void addServerSessionOnUpdateListener(OnSessionUpdateListener listener) {
        synchronized (mServerSessionOnUpdateListeners) {
            if (!mServerSessionOnUpdateListeners.contains(listener)) {
                mServerSessionOnUpdateListeners.add(listener);
            }
        }
    }

    @Override
    public void removeServerSessionOnUpdateListener(OnSessionUpdateListener listener) {
        synchronized (mServerSessionOnUpdateListeners) {
            mServerSessionOnUpdateListeners.remove(listener);
        }
    }

    @Override
    public void addRoomDevicesOnUpdateListener(OnRoomDevicesUpdateListener listener) throws Exception {
        synchronized (mRoomDevicesUpdateListeners) {
            if (!mRoomDevicesUpdateListeners.contains(listener)) {
                mRoomDevicesUpdateListeners.add(listener);
            }
        }
    }

    @Override
    public void removeRoomDevicesOnUpdateListener(OnRoomDevicesUpdateListener listener) throws Exception {
        synchronized (mRoomDevicesUpdateListeners) {
            mRoomDevicesUpdateListeners.remove(listener);
        }
    }


    @Override
    public boolean hasServerSession(final Session session) {
        String lsid = session.getId();
        if (mServerSessions.containsKey(lsid)) {
            Log.d("yao", " reConnectSession hasServerSession true ,lsid = " + lsid);
            return true;
        }
        Log.d("yao", "reConnectSession hasServerSession false");
        return false;
    }


    @Override
    public boolean addServerSession(final Session session) {
        synchronized (mServerSessions) {
            String lsid = session.getId();
            if (!TextUtils.isEmpty(lsid)
                    && lsid.length() == 32
                    && !mServerSessions.containsKey(lsid)) {
                Log.d("yao", "send to addServerSession sid=" + lsid);
                mServerSessions.put(lsid, session);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        synchronized (mServerSessionOnUpdateListeners) {
                            for (OnSessionUpdateListener listener : mServerSessionOnUpdateListeners) {
                                listener.onSessionConnect(session);
                            }
                        }
                    }
                };
                mSSContext.post(runnable);
                return true;
            }
        }

        Log.d("yao", "addServerSession false");
        return false;
    }

    @Override
    public boolean removeServerSession(final Session session) {
        synchronized (mServerSessions) {
            String lsid = session.getId();
            if (mServerSessions.containsKey(lsid)) {
                Log.d("yao", "send to removeServerSession sid=" + lsid);
                mServerSessions.remove(lsid);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        synchronized (mServerSessionOnUpdateListeners) {
                            for (OnSessionUpdateListener listener : mServerSessionOnUpdateListeners) {
                                listener.onSessionDisconnect(session);
                            }
                        }
                    }
                };
                mSSContext.post(runnable);
                return true;
            }
        }
        return false;
    }

    @Override
    public void queryConnectedRoomDevices() {
        queryServerConnectedDevices();
    }

    @Override
    public void connectChannelSessionState(int type, int state) {
        String info = null;
        if (type == Constants.COOCAA_IOT_CHANNEL_TYPE_NOT_NET) {  //无连接
            mChannelConnectState = 0x00;
        } else if (type == Constants.COOCAA_IOT_CHANNEL_TYPE_SSE) { //SSE连接
            //第一位是sse
            if (state == Constants.COOCAA_IOT_CHANNEL_STATE_CONNECT) {
                mChannelConnectState = mChannelConnectState | 0x01;
                info = "SSE 连接成功";
            } else {
                mChannelConnectState = mChannelConnectState & 0x02;
                info = "SSE 断开连接";
            }
        } else if (type == Constants.COOCAA_IOT_CHANNEL_TYPE_LOCAL) { //本地连接
            //第二位是local
            if (state == Constants.COOCAA_IOT_CHANNEL_STATE_CONNECT) {
                mChannelConnectState = mChannelConnectState | 0x02;
                info = "local 连接成功";
            } else {
                mChannelConnectState = mChannelConnectState & 0x01;
                info = "local 断开连接";
            }
        }

        AndroidLog.androidLog("SessionManagerServerImpl--mChannelConnectState:" +
                mChannelConnectState + " type:" + type + " state：" + state);
        if (mConnectedSession != null) {
            mConnectedSession.putExtra("connectStatus", "" + mChannelConnectState);
        }
        mSSContext.getDeviceManager().loginState(mChannelConnectState, info);
    }

    @Override
    public void connectingChannelSessionState(int type, int state) {
        String info = null;
        if (type == Constants.COOCAA_IOT_CHANNEL_TYPE_SSE) { //SSE连接
            //第一位是sse
            if (state == Constants.COOCAA_IOT_CHANNEL_TYPE_CONNECTING) {
                mChannelConnectingState = mChannelConnectingState | 0x01;
                info = "SSE 连接中";
            } else {
                mChannelConnectingState = mChannelConnectingState & 0x02;
                info = "SSE 结束";
            }
        } else if (type == Constants.COOCAA_IOT_CHANNEL_TYPE_LOCAL) { //本地连接
            //第二位是local
            if (state == Constants.COOCAA_IOT_CHANNEL_TYPE_CONNECTING) {
                mChannelConnectingState = mChannelConnectingState | 0x02;
                info = "local 连接中";
            } else {
                mChannelConnectingState = mChannelConnectingState & 0x01;
                info = "local 结束";
            }
        }

        AndroidLog.androidLog("SessionManagerServerImpl--mChannelConnectingState:" +
                mChannelConnectingState + " type:" + type + " state：" + state);
        mSSContext.getDeviceManager().loginConnectingState(mChannelConnectingState, info);
    }

    @Override
    public void saveHandlerConnectSession(String handleConnect) {
        ShareUtls.getInstance(mSSContext.getContext()).putString(Constants.COOCAA_LAST_CONNECT_SESSION, handleConnect);
    }

    @Override
    public void clearHandlerConnectSession(String lsId) {
        try {
            AndroidLog.androidLog("clearHandlerConnectSession lsId:" + lsId);
            String handleConnectSessionStr = ShareUtls.getInstance(mSSContext.getContext()).getString(Constants.COOCAA_LAST_CONNECT_SESSION, "");
            if (!TextUtils.isEmpty(handleConnectSessionStr)) {
                try {
                    Session handleConnectSession = Session.Builder.decode(handleConnectSessionStr);
                    AndroidLog.androidLog("clearHandlerConnectSession handleConnectSession sid:" + handleConnectSession.getId());
                    if (!TextUtils.isEmpty(handleConnectSession.getId()) && lsId.equals(handleConnectSession.getId())) {
                        ShareUtls.getInstance(mSSContext.getContext()).putString(Constants.COOCAA_LAST_CONNECT_SESSION, "");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean sessionChanged = true;

    @Override
    public void setSessionChanged(boolean b) {
        sessionChanged = b;
    }

    @Override
    public boolean getSessionChanged() {
        return sessionChanged;
    }

    @Override
    public void refreshChannelState() {
        mSSContext.getDeviceManager().loginState(mChannelConnectState, "");
    }

    private void queryServerConnectedDevices() {
        if (!NetUtils.isConnected(mSSContext.getContext())) return;

        try {
            mSSContext.getServerInterface().getRoomDevices(mSSContext.getAccessToken(), new HttpSubscribe<HttpResult<RoomDevices>>() {
                @Override
                public void onSuccess(HttpResult<RoomDevices> result) {
                    if (result != null && !TextUtils.isEmpty(result.code) && result.code.equals("0") && result.data != null) {
                        final RoomDevices roomDevices = result.data;
                        mSSContext.post(new Runnable() {
                            @Override
                            public void run() {

                                AndroidLog.androidLog("mRoomDevices SIZE start:" + mRoomDevices.size() + " roomDevices.getDeviceCount():" + roomDevices.getDeviceCount());
                                mRoomDevices.clear();
                                List<RoomDevice> serverRoomDevices = roomDevices.getDevices();
                                if (serverRoomDevices != null) {
                                    mRoomDevices.addAll(serverRoomDevices);

                                    try {
                                        if (mServerSessions.size() > 0 && serverRoomDevices.size() > 1) {
                                            int size = mServerSessions.size();
                                            String[] listSid = mServerSessions.keySet().toArray(new String[size]);
                                            for (int k = 0; k < listSid.length; k++) {
                                                String serverSessionSid = listSid[k];
                                                boolean isExit = false;

                                                for (int i = 0; i < serverRoomDevices.size(); i++) {
                                                    String sid = serverRoomDevices.get(i).getSid();
                                                    if (serverRoomDevices.get(i).getIsHost() == 0 && sid.equals(serverSessionSid)) {
                                                        isExit = true;
                                                        break;
                                                    }
                                                }
                                                if (!isExit) {
                                                    mServerSessions.remove(serverSessionSid);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                                AndroidLog.androidLog("mRoomDevices SIZE end:" + mRoomDevices.size() + " roomDevices.getDeviceCount():" + roomDevices.getDeviceCount());

                                synchronized (mRoomDevicesUpdateListeners) {
                                    for (OnRoomDevicesUpdateListener listener : mRoomDevicesUpdateListeners) {
                                        listener.onRoomDevicesUpdate(roomDevices.getDeviceCount());
                                    }
                                }
                            }
                        });
                    }
                }

                @Override
                public void onError(HttpThrowable error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Runnable> mUpdateRunnables = new HashMap<>();

    @Override
    public boolean updateSession(final Session session) {
        boolean b = false;
        String lsid = session.getId();
        Runnable runnable = null;

        sessionChanged = true;

        if (mConnectedSession != null && mConnectedSession.equals(session)) {
            mConnectedSession = session;
            mConnectedSession.putExtra("connectStatus", "" + mChannelConnectState);

            runnable = new Runnable() {
                @Override
                public void run() {
                    synchronized (mConnectedSessionOnUpdateListeners) {
                        for (OnSessionUpdateListener listener : mConnectedSessionOnUpdateListeners) {
                            listener.onSessionUpdate(mConnectedSession);
                        }
                    }
                }
            };
            b = true;
        } else {
            synchronized (mServerSessions) {
                if (mServerSessions.containsKey(lsid)) {
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            synchronized (mServerSessionOnUpdateListeners) {
                                for (OnSessionUpdateListener listener : mServerSessionOnUpdateListeners) {
                                    listener.onSessionUpdate(session);
                                }
                            }
                        }
                    };
                    b = true;
                }
            }
        }

        if (b && runnable != null) {
            synchronized (mUpdateRunnables) {
                mUpdateRunnables.remove(lsid);
                mUpdateRunnables.put(lsid, runnable);
            }
            mSSContext.postDelay(runnable, 1000);
        }
        return b;
    }

    @Override
    public Session getServerSession(String lsid) {
        synchronized (mServerSessions) {
            return mServerSessions.get(lsid);
        }
    }

    @Override
    public List<Session> getServerSessions() throws Exception {
        synchronized (mServerSessions) {
            return new ArrayList<>(mServerSessions.values());
        }
    }

    @Override
    public List<RoomDevice> getRoomDevices() throws Exception {
        AndroidLog.androidLog("SessionManagerServerImpl getRoomDevices:" + mRoomDevices.size());
        return mRoomDevices;
    }

    @Override
    public boolean containServerSession(Session session) {
        synchronized (mServerSessions) {
            return mServerSessions.containsKey(session.getId());
        }
    }

    @Override
    public boolean available(Session session, String channel) throws Exception {
        if (SSChannel.IM_CLOUD.equals(channel)) {
            return mSSContext.getIMChannel().availableCloud(session);
        } else {
            return checkCommonNet(session) && mSSContext.getIMChannel().availableLocal(session);
        }
    }

    @Override
    public boolean isConnectSSE() throws Exception {
        return mSSContext.getIMChannel().isConnectSSE();
    }

    @Override
    public void updateMyLSID(boolean notify) {
        try {
            mMySession.setId(mSSContext.getLSID());
            mMySession.putExtra(SSChannel.IM_CLOUD, mSSContext.getLSID());
            if (true) {
                dispatchMySessionUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkCommonNet(Session session) {
        try {
            Session mySession = mSSContext.getSessionManager().getMySession();
            if (session == null || session.getExtras().size() <= 0
                    || mySession == null || mySession.getExtras().size() <= 0)
                return false;
            String sessionIP = session.getExtras().get(SSChannel.STREAM_LOCAL);
            Map<String, String> extras = mySession.getExtras();
            String mySessionIP = extras.get(SSChannel.STREAM_LOCAL);


            Log.e("yao", "same wifi check my ip=" + mySessionIP + "  target ip=" + sessionIP);
            if (TextUtils.isEmpty(sessionIP) || TextUtils.isEmpty(mySessionIP)) {
                return false;
            }

            if (IpV4Util.checkSameSegmentByDefault(sessionIP, mySessionIP)) {
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
