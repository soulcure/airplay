package swaiotos.channel.iot.ss.channel.base.local;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaCodec;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.skyworth.dpclientsdk.ConnectState;
import com.skyworth.dpclientsdk.MACUtils;
import com.skyworth.dpclientsdk.PduBase;
import com.skyworth.dpclientsdk.StreamSinkCallback;
import com.skyworth.dpclientsdk.StreamSourceCallback;
import com.skyworth.dpclientsdk.TcpClient;
import com.skyworth.dpclientsdk.TcpServer;
import com.skyworth.dpclientsdk.ble.BlePdu;
import com.skyworth.dpclientsdk.ble.BluetoothServer;
import com.skyworth.dpclientsdk.ble.BluetoothServerCallBack;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.analysis.ChannelStatistics;
import swaiotos.channel.iot.ss.analysis.UserBehaviorAnalysis;
import swaiotos.channel.iot.ss.channel.im.IMChannelServer;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.channel.iot.ss.channel.stream.IStreamChannel;
import swaiotos.channel.iot.ss.config.PortConfig;
import swaiotos.channel.iot.ss.device.IConnectResult;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.AppUtils;
import swaiotos.channel.iot.utils.DeviceUtil;
import swaiotos.channel.iot.utils.IpV4Util;
import swaiotos.channel.iot.utils.NetUtils;
import swaiotos.channel.iot.utils.SameLan;
import swaiotos.channel.iot.utils.SpeedTest;
import swaiotos.channel.iot.utils.WifiConnectManager;
import swaiotos.channel.iot.webrtc.DataChannelClient;
import swaiotos.channel.iot.webrtc.DataChannelServer;
import swaiotos.channel.iot.webrtc.Peer;

/**
 * @ClassName: LocalChannelImpl
 * @Author: lu
 * @CreateDate: 2020/4/10 3:49 PM
 * @Description:
 */
public class LocalChannelImpl implements LocalChannel {

    private static final String TAG = "yao";

    private TcpServer tcpServer;
    private DataChannelServer dataChannelServer;
    private final Context mContext;
    private final SSContext ssContext;
    private String mAddress;

    private BluetoothServer bleServer;
    private WifiBroadcastReceiver mWifiBroadcastReceiver;
    private final ConcurrentHashMap<String, SocketChannel> mTcpClients;

    private final List<Callback> mCallbacks = new ArrayList<>();

    public LocalChannelImpl(Context context, SSContext ssContext) {
        this.mContext = context;
        this.ssContext = ssContext;
        this.mTcpClients = new ConcurrentHashMap<>();
    }

    @Override
    public void addCallback(Callback callback) {
        synchronized (mCallbacks) {
            if (!mCallbacks.contains(callback)) {
                mCallbacks.add(callback);
            }
        }
    }

    @Override
    public void removeCallback(Callback callback) {
        synchronized (mCallbacks) {
            mCallbacks.remove(callback);
        }
    }

    @Override
    public String open() throws IOException {
        performOpen();
        NetUtils.NetworkReceiver.register(mContext, mNetworkReceiver);
        return getAddress();
    }

    private void performOpen() {
        mAddress = DeviceUtil.getLocalIPAddress(mContext);
    }

    @Override
    public String getAddress() {
        return DeviceUtil.getLocalIPAddress(mContext);
    }

    @Override
    public boolean available() {
        boolean res = !TextUtils.isEmpty(mAddress);
        Log.d("yao", "LocalChannelImpl available =" + res);
        return res;
    }

    @Override
    public void close() throws IOException {
        NetUtils.NetworkReceiver.unregister(mContext, mNetworkReceiver);
        if (mWifiBroadcastReceiver != null)
            mContext.unregisterReceiver(mWifiBroadcastReceiver);
        performClose();
    }

    private void performClose() {
        tcpServer.close();
        mAddress = null;
    }

    @Override
    public void setLocalChannelMonitor(LocalChannelMonitor monitor) {

    }

    @Override
    public boolean available(IStreamChannel.Receiver receiver) {
        return true;
    }

    @Override
    public int openReceiver(final IStreamChannel.Receiver receiver) {
        //final int port = getRandomPort();
        Log.d(TAG, "tcpServer create---");

        int port = PortConfig.getLocalServerPort(ssContext.getContext().getPackageName());
        if (tcpServer == null) {
            tcpServer = new TcpServer(port, TcpServer.BUFFER_SIZE_LOW,
                    new SSinkCallback(receiver, port, ssContext, mTcpClients));
        } else {
            tcpServer.close();
        }
        tcpServer.open();

        if (dataChannelServer == null
                && new File("/vendor/TianciVersion").exists()) {//for tv
            dataChannelServer = new DataChannelServer(ssContext, receiver);
        }

        if (Constants.isDangle()) {
            if (mWifiBroadcastReceiver == null) {
                Log.e(TAG, "WifiBroadcastReceiver ----");
                mWifiBroadcastReceiver = new WifiBroadcastReceiver(receiver);
                IntentFilter filter = new IntentFilter();
                filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//监听wifi是开关变化的状态
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//监听wifi连接状态广播
                filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//监听wifi列表变化（开启一个热点或者关闭一个热点）
                filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);//wifi的连接错误信息
                mContext.registerReceiver(mWifiBroadcastReceiver, filter);
            }

            if (NetUtils.isConnected(ssContext.getContext())) {
                openBle(receiver); //有网络
            } else {
                String mac = MACUtils.getMac(ssContext.getContext()); //无网络，排除MAC异常情况
                if (!TextUtils.isEmpty(mac) && !mac.equals("020000000000")) {
                    openBle(receiver);
                }
            }

        }
        return port;
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {
        private final IStreamChannel.Receiver receiver;

        public WifiBroadcastReceiver(IStreamChannel.Receiver receiver) {
            this.receiver = receiver;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null || TextUtils.isEmpty(intent.getAction())) {
                return;
            }

            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                //获取当前的wifi状态int类型数据
                int mWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                AndroidLog.androidLog("LocalChannelImpl------------mWifiState:" + mWifiState);
                if (mWifiState == WifiManager.WIFI_STATE_ENABLED) {
                    openBle(receiver);
                }
            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                int linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0);
                Log.e(TAG, "wifi连接错误原因=" + linkWifiResult);
                if (mWifiResult != null && linkWifiResult > 0) {
                    mWifiResult.setErrCode(linkWifiResult);
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.e(TAG, "NetworkInfo---" + info.getState().name());
                if (NetworkInfo.State.DISCONNECTED == info.getState()) {//wifi没连接上
                    Log.e(TAG, "wifi没连接上");
                    if (mWifiResult != null) {
                        mWifiResult.addWifiResult(-1);
                    }
                } else if (NetworkInfo.State.CONNECTING == info.getState()) {//正在连接
                    Log.e(TAG, "wifi正在连接");
                    if (mWifiResult != null) {
                        mWifiResult.addWifiResult(1);
                    }
                } else if (NetworkInfo.State.CONNECTED == info.getState()) {//wifi连接上了
                    Log.e(TAG, "wifi连接上了");
                    if (mWifiResult != null) {
                        mWifiResult.addWifiResult(2);
                    }
                }
            } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                //Log.e(TAG, "网络列表变化了");
            }
        }
    }

    private WifiResult mWifiResult;

    private void openBle(final IStreamChannel.Receiver receiver) {
        Log.d(TAG, "bleServer create---");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bleServer != null) return;

            bleServer = new BluetoothServer(mContext, new BluetoothServerCallBack() {
                @Override
                public void onMessageShow(BlePdu blePdu, BluetoothDevice device) {
                    byte type = blePdu.pduType;

                    if (type == BlePdu.TEMP_CMD) {
                        if (receiver != null) {
                            Log.e(TAG, "BleServer onData ---" + new String(blePdu.body));
                            receiver.onReceive(blePdu.body);
                        }
                    } else if (type == BlePdu.TEMP_PROTO) {
                        try {
                            String s = new String(blePdu.body);
                            JSONObject jsonObject = new JSONObject(s);
                            String proto = jsonObject.optString("proto");
                            if (!TextUtils.isEmpty(proto)) {
                                if (proto.equals("TVDeviceInfo")) {
                                    String deviceInfoStr = jsonObject.optString("deviceInfo"); //手机deviceInfo

                                    String targetSessionStr = jsonObject.optString("session");//手机session

                                    if (ssContext.getDeviceInfo() instanceof TVDeviceInfo) {
                                        Session mySession = ssContext.getSessionManager().getMySession();
                                        TVDeviceInfo info = (TVDeviceInfo) ssContext.getDeviceInfo();

                                        JSONObject jo = new JSONObject();

                                        jo.put("code", 0);
                                        jo.put("msg", "get tv session and DeviceInfo success");
                                        jo.put("proto", "TVDeviceInfo");
                                        jo.put("device", info.encode());
                                        jo.put("session", mySession.encode());

                                        String json = jo.toString();

                                        Log.e(TAG, "BleServer session onData proto---" + json);
                                        byte[] data = json.getBytes();

                                        ByteBuffer byteBuffer = ByteBuffer.allocate(BlePdu.PDU_HEADER_LENGTH + data.length);
                                        byteBuffer.put(BlePdu.pduStartFlag);
                                        byteBuffer.put(BlePdu.TEMP_PROTO);
                                        byteBuffer.putShort((short) data.length);
                                        byteBuffer.put(data);

                                        bleServer.sendMessage(byteBuffer, device);
                                    }
                                } else if (proto.equals("ConfigureWiFi")) {
                                    String ssid = jsonObject.optString("ssid");
                                    String password = jsonObject.optString("password");

                                    String bleDevice = device.getAddress();
                                    Log.e(TAG, "BleClient---" + bleDevice +
                                            "---ConfigureWiFi---" + ssid + "/" + password);

                                    String enc;
                                    if (TextUtils.isEmpty(password)) {
                                        enc = "OPEN";
                                    } else {
                                        enc = "WPA";
                                    }

                                    String sid = "";
                                    try {
                                        Session session = ssContext.getSessionManager().getMySession();
                                        if (session != null) {
                                            sid = session.getId();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        sid = "";
                                    }

                                    if (!TextUtils.isEmpty(ssid)) {
                                        WifiConnectManager.connectWifi(mContext, ssid, password, enc);

                                        mWifiResult = new WifiResult(mContext, ssContext, bleServer,
                                                device, ssid, sid,
                                                new WifiResult.WifiConfigCallBack() {
                                                    @Override
                                                    public void onFinish() {
                                                        mWifiResult = null;
                                                    }
                                                });
                                        mWifiResult.sendWifiResult();

                                        if (password.length() >= 1 && password.length() <= 7) {
                                            ssContext.postDelay(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mWifiResult.addWifiResult(-1);
                                                    mWifiResult.setErrCode(1);
                                                }
                                            }, 2000);
                                        }

                                    }

                                }
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onStartSuccess(String s) {
                    Log.d(TAG, "ble server onStartSuccess " + s);
                }

                @Override
                public void onStartFail(String s) {
                    Log.e(TAG, "ble server onStartFail " + s);
                }
            });

            bleServer.openBle();
        }
    }


    static class SSinkCallback implements StreamSinkCallback {
        IStreamChannel.Receiver receiver;
        int port;
        SSContext ssContext;
        ConcurrentHashMap<String, SocketChannel> loginChannel;

        SSinkCallback(IStreamChannel.Receiver receiver, int id, SSContext ssContext,
                      ConcurrentHashMap<String, SocketChannel> loginMap) {
            this.receiver = receiver;
            this.port = id;
            this.ssContext = ssContext;
            this.loginChannel = loginMap;
        }


        @Override
        public void onConnectState(ConnectState connectState) {
            Log.d(TAG, "SSinkCallback onConnectState : " + connectState);

            String end;
            if (connectState == ConnectState.CONNECT) {
                end = "tcpServer crate success";
                Log.d("logfile", end);
            } else {
                end = "tcpServer crate fail";
                Log.e("logfile", end);
            }
//            LogFile.inStance().toFile(end);
        }

        @Override
        public void onData(String s, SocketChannel socketChannel) {
            Log.e(TAG, "TcpServer onData String---" + s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                String proto = jsonObject.optString("proto");
                if (!TextUtils.isEmpty(proto)) {
                    if (proto.equals("login")) {
                        String sid = jsonObject.optString("sid", "");
                        if (!TextUtils.isEmpty(sid)) {
                            Log.d(TAG, "send to loginChannel put socketChannel sid=" + sid);
                            loginChannel.put(sid, socketChannel);
                        }
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        @Override
        public void onData(byte[] data, SocketChannel channel) {
            if (receiver != null) {
                Log.e(TAG, "TcpServer onData ---" + new String(data));
                receiver.onReceive(data);
                try {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(PduBase.PDU_HEADER_LENGTH + data.length);
                    byteBuffer.putInt(PduBase.pduStartFlag);
                    byteBuffer.put(PduBase.LOCAL_BYTES);
                    byteBuffer.putInt(0);
                    byteBuffer.putInt(0);
                    byteBuffer.putLong(0);
                    byteBuffer.putInt(0);
                    byteBuffer.putInt(0);  //reserved
                    byteBuffer.putInt(data.length);
                    byteBuffer.put(data);
                    byteBuffer.flip();
                    channel.write(byteBuffer);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        @Override
        public void onAudioFrame(MediaCodec.BufferInfo bufferInfo,
                                 ByteBuffer byteBuffer, SocketChannel socketChannel) {

        }

        @Override
        public void onVideoFrame(MediaCodec.BufferInfo bufferInfo,
                                 ByteBuffer byteBuffer, SocketChannel socketChannel) {

        }

        @Override
        public void ping(String s, SocketChannel socketChannel) {
            Log.d(TAG, "socket server receive ping---" + s);
        }

        @Override
        public void pong(String s, SocketChannel socketChannel) {
            Log.d(TAG, "socket server receive pong---" + s);
        }
    }


    @Override
    public void closeReceiver(int channelId) {
        tcpServer.close();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && Constants.isDangle()
                && bleServer != null) {
            bleServer.removeService();   //关闭连接
        }
    }

    @Override
    public IStreamChannel.Sender openSender(String ip, int channelId,
                                            IMChannelServer.TcpClientResult callback,
                                            IMChannelServer.Receiver receiver) {
        AndroidLog.androidLog("StreamChannel openSender...");
        return new StreamSender(ip, channelId, ssContext, callback, receiver);
    }

    @Override
    public boolean available(final IStreamChannel.Sender sender) {
        return sender.available();
    }

    @Override
    public void closeSender(IStreamChannel.Sender sender) {
        if (sender instanceof StreamSender) {
            AndroidLog.androidLog("StreamChannel closeSender...");

            ((StreamSender) sender).stopHeartBeat();
            if (((StreamSender) sender).tcpClient != null) {
                ((StreamSender) sender).tcpClient.close();
                ((StreamSender) sender).stopHeartBeat();
                ((StreamSender) sender).tcpClient = null;
            }

            if (((StreamSender) sender).dataChannelClient != null) {
                ((StreamSender) sender).dataChannelClient.hangup();
            }

        }
    }

    @Override
    public boolean sendServerMessage(IMMessage message) {
        boolean res = false;
        try {
            //使用tcpServer 下发
            if (new File("/vendor/TianciVersion").exists()) {//for tv
                String sid = message.getTarget().getId();
                if (mTcpClients.size() > 0) {
                    IMMessage imMessage = modifyMessageTcp(message, ssContext);
                    byte[] bytes = imMessage.toString().getBytes();

                    ByteBuffer byteBuffer = ByteBuffer.allocate(PduBase.PDU_HEADER_LENGTH + bytes.length);
                    byteBuffer.putInt(PduBase.pduStartFlag);
                    byteBuffer.put(PduBase.LOCAL_STRING); //server to client 的IMMessage消息改为LOCAL_STRING type发送
                    byteBuffer.putInt(0);
                    byteBuffer.putInt(0);
                    byteBuffer.putLong(0);
                    byteBuffer.putInt(0);
                    byteBuffer.putInt(0);  //reserved
                    byteBuffer.putInt(bytes.length);
                    byteBuffer.put(bytes);
                    byteBuffer.flip();

                    Log.d(TAG, "tcp server send channel to sid=" + sid);
                    if (sid.equals("sid-broadcast")) { //广播消息
                        List<String> keys = new ArrayList<>();
                        for (Map.Entry<String, SocketChannel> entry : mTcpClients.entrySet()) {
                            String key = entry.getKey();
                            SocketChannel channel = entry.getValue();
                            if (channel != null) {
                                try {
                                    channel.write(byteBuffer);
                                    res = true;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    keys.add(key);
                                }
                            }
                        }
                        if (keys.size() > 0) {
                            for (String item : keys) {
                                mTcpClients.remove(item);
                            }
                        }
                    } else { //单发消息
                        SocketChannel channel = mTcpClients.get(sid);
                        if (channel != null) {
                            try {
                                channel.write(byteBuffer);
                                return true;
                            } catch (IOException e) {
                                e.printStackTrace();
                                mTcpClients.remove(sid);
                            }
                        }


                    }

                }
                if (dataChannelServer != null) {
                    IMMessage imMessage = modifyMessageTcp(message, ssContext);

                    byte[] bytes = imMessage.toString().getBytes();
                    if (sid.equals("sid-broadcast")) { //广播消息
                        List<String> list = dataChannelServer.getPeerSidList();
                        for (String item : list) {
                            Peer pc = dataChannelServer.getPeerConnect(item);
                            boolean isSend = pc.sendChannelData(bytes);
                            if (!isSend) {
                                pc.getPc().close();
                                dataChannelServer.removePeer(sid);
                            }
                        }
                    } else {
                        Peer pc = dataChannelServer.getPeerConnect(sid);
                        if (pc != null) {
                            return pc.sendChannelData(bytes);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public List<String> serverConnectList() {
        if (new File("/vendor/TianciVersion").exists()) {//for tv
            List<String> list = new ArrayList<>();
            if (mTcpClients != null && mTcpClients.size() > 0) {
                for (Map.Entry<String, SocketChannel> entry : mTcpClients.entrySet()) {
                    String key = entry.getKey();
                    list.add(key);
                }
            }

            if (dataChannelServer != null) {
                List<String> peerList = dataChannelServer.getPeerSidList();
                if (peerList != null && peerList.size() > 0) {
                    list.addAll(peerList);
                }
            }

            return list;
        }
        return null;
    }

    @Override
    public void removeServerConnect(String sid) {
        if (mTcpClients != null
                && mTcpClients.size() > 0
                && new File("/vendor/TianciVersion").exists()) {
            Log.d(TAG, "removeServerConnect sid=" + sid);
            mTcpClients.remove(sid);
        }
    }


    @Override
    public String type() {
        return TAG;
    }


    private NetUtils.NetworkReceiver mNetworkReceiver = new NetUtils.NetworkReceiver() {
        @Override
        public void onConnected() {
            AndroidLog.androidLog("net callback------");
            performOpen();
            synchronized (mCallbacks) {  //by colin add  =, 此回调影响到client重连server
                for (Callback callback : mCallbacks) {
                    callback.onConnected(LocalChannelImpl.this);
                }
            }
        }

        @Override
        public void onDisconnected() {
            synchronized (mCallbacks) {
                for (Callback callback : mCallbacks) {
                    callback.onDisconnected(LocalChannelImpl.this);
                }
            }
        }
    };

    private int getRandomPort() {
        int mLocalPort = 0;
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(0);
            mLocalPort = ss.getLocalPort();
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (ss != null) {
                    ss.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return mLocalPort;
    }


    public static class StreamSender implements IStreamChannel.Sender {
        private static final int HEART_BEAT_INTERVAL = 2; //心跳间隔5秒
        private static final String HEART_BEAT_STR = "Heart Beat Message";

        private TcpClient tcpClient;
        private DataChannelClient dataChannelClient;
        private final SSContext ssContext;
        private final IMChannelServer.Receiver mReceiver;
        private IMChannelServer.TcpClientResult mTcpClientResult;

        private IStreamChannel.SenderMonitor monitor;
        private int heartBeatCount;
        private volatile boolean channelAvailable = false;
        private ScheduledExecutorService heartBeatScheduled;
        private long createTime;
        private final ChannelStatistics mChannelStatistics;
        private long stateTime;

        private final StreamSourceCallback mStreamSourceCallback = new StreamSourceCallback() {
            @Override
            public void onConnectState(ConnectState connectState) {
                Log.e(TAG, "StreamSourceCallback onConnectState : " + connectState);
                if (connectState == ConnectState.CONNECT) {
                    try {
                        //上报本地连接成功
                        UserBehaviorAnalysis.reportLocalConnect(ssContext.getLSID(), System.currentTimeMillis() - createTime);

                        ssContext.getSessionManager().connectingChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_LOCAL,
                                Constants.COOCAA_IOT_CHANNEL_TYPE_CONNECTED);
                        ssContext.getSessionManager().connectChannelSessionState(
                                Constants.COOCAA_IOT_CHANNEL_TYPE_LOCAL, Constants.COOCAA_IOT_CHANNEL_STATE_CONNECT);

                        AndroidLog.androidLog("local-mChannelConnectState-success--:" + this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    changeAvailable(true);
                    startHeartBeat();

                    if (mTcpClientResult != null) {
                        mTcpClientResult.onResult(0, "local tcp client success");
                        mTcpClientResult = null;
                    }
                } else {
                    changeAvailable(false);
                    stopHeartBeat();
                    try {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - stateTime > 1000) {
                            ssContext.getSessionManager().connectingChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_LOCAL,
                                    Constants.COOCAA_IOT_CHANNEL_TYPE_CONNECTED);
                            ssContext.getSessionManager().connectChannelSessionState(
                                    Constants.COOCAA_IOT_CHANNEL_TYPE_LOCAL, Constants.COOCAA_IOT_CHANNEL_STATE_DISCONNECT);
                            AndroidLog.androidLog("local-mChannelConnectState-error--:" + this);

                            Session target = ssContext.getSessionManager().getConnectedSession();
                            if (target != null) {
                                //上报本地连接失败
                                if (connectState == ConnectState.ERROR) {
                                    UserBehaviorAnalysis.reportLocalConnectError(ssContext.getLSID(), target.getId());
                                }
                            }
                        }
                        stateTime = currentTime;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (mTcpClientResult != null) {
                        mTcpClientResult.onResult(-2, "local tcp client fail");
                        mTcpClientResult = null;
                    }

                    //createDataChannel();
                }
            }

            @Override
            public void onData(String content) {
                Log.e(TAG, "StreamSourceCallback onData : " + content);
                try {
                    IMMessage message = IMMessage.Builder.decode(content);
                    if (mReceiver != null) {
                        mReceiver.onReceive(null, message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onData(byte[] bytes) {
                //colin
                String content = new String(bytes);
                try {
                    final IMMessage message = IMMessage.Builder.decode(content);
                    String msgId = message.getId();
                    if (!TextUtils.isEmpty(msgId) && mImMessageCallbackMap.containsKey(msgId)) {
                        IMMessageCallback imMessageCallback = mImMessageCallbackMap.get(msgId);
                        if (imMessageCallback != null) {
                            imMessageCallback.onEnd(message, 0, "tcp send success");
                        }
                        mImMessageCallbackMap.remove(msgId);
                        AndroidLog.androidLog("----mImMessageCallbackMap-size:" + mImMessageCallbackMap.size());
                    }

                    mChannelStatistics.receiverMessage(message, 5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

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

        public StreamSender(String ip, int port, SSContext ssContext,
                            IMChannelServer.TcpClientResult tcpClientResult,
                            IMChannelServer.Receiver receiver) {
            this.ssContext = ssContext;
            this.mReceiver = receiver;
            mTcpClientResult = tcpClientResult;
            mChannelStatistics = new ChannelStatistics(ssContext, ChannelStatistics.CHANNEL.LOCAL);
            mImMessageCallbackMap = new ConcurrentHashMap<>();

            tcpClient = new TcpClient(ip, port, mStreamSourceCallback);
            tcpClient.open();
            createTime = System.currentTimeMillis();
            AndroidLog.androidLog("StreamChannel----openSender:" + tcpClient);
        }

        @Override
        public void setSenderMonitor(IStreamChannel.SenderMonitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public boolean available() {
            if (!channelAvailable) {
                return pingTarget();
            }
            Log.d("yao", "LocalChannelImpl available channelAvailable is = " + channelAvailable);
            return channelAvailable;
        }


        private void createDataChannel() {
            Log.d(TAG, "createDataChannel start...");

            File file = new File("/vendor/TianciVersion");
            if (!file.exists()) {  //for mobile
                if (dataChannelClient != null && dataChannelClient.isOpen()) {
                    return;
                }

                if (dataChannelClient != null) {
                    dataChannelClient.close();
                    dataChannelClient = null;
                }

                StreamSourceCallback callback = new StreamSourceCallback() {
                    @Override
                    public void onConnectState(ConnectState connectState) {
                        Log.e(TAG, "DataChannelCallback onConnectState : " + connectState);
                        if (connectState == ConnectState.DISCONNECT
                                || connectState == ConnectState.ERROR) {
                            if (dataChannelClient != null) {
                                dataChannelClient.iceRestart();
                            }
                        } else if (connectState == ConnectState.CONNECT) {
                            // 统计dataChannel 的连接次数
                        }
                    }

                    @Override
                    public void onData(String content) {
                        Log.e(TAG, "DataChannelCallback onData : " + content);
                        try {
                            IMMessage message = IMMessage.Builder.decode(content);
                            if (mReceiver != null) {
                                mReceiver.onReceive(null, message);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onData(byte[] bytes) {
                    }

                    @Override
                    public void ping(String s) {
                    }

                    @Override
                    public void pong(String s) {
                    }
                };

                dataChannelClient = new DataChannelClient(ssContext, callback);
                dataChannelClient.offer(AppUtils.getWifiInfoSSID(ssContext.getContext()));
                if (dataChannelClient != null) {
                    Log.d("yao", "createDataChannel dataChannelClient hash="
                            + dataChannelClient.hashCode());
                }
            }

        }

        /**
         * ping 对方ip,如果ping通则发起tcpClient重连
         *
         * @return
         */
        private boolean pingTarget() {
            Session target = null;
            try {
                target = ssContext.getSessionManager().getConnectedSession();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (target != null) {
                try {
                    String targetIp = target.getExtra(SSChannel.STREAM_LOCAL);
                    String myIp = DeviceUtil.getLocalIPAddress(ssContext.getContext());
                    boolean isSessionChanged = ssContext.getSessionManager().getSessionChanged();
                    boolean isSameSegment = IpV4Util.checkSameSegmentByDefault(targetIp, myIp);
                    String ipStr = "my ip=" + myIp + " ping target ip=" + targetIp
                            + " && isSameSegment=" + isSameSegment
                            + " && isSessionChanged=" + isSessionChanged;
                    Log.d("yao", ipStr);

                    if (!TextUtils.isEmpty(targetIp)) {
                        if (isSessionChanged || isSameSegment) {
                            boolean inLan = SameLan.isInSameLAN(targetIp);
                            ssContext.getSessionManager().setSessionChanged(false);
                            Log.e("colin", "isInSameLAN---" + inLan);
                            if (inLan) {
                                int port = PortConfig.getLocalServerPort(ssContext.getContext().getPackageName());
                                reOpenConnect(targetIp, port, mStreamSourceCallback);
                                return true;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }


        /**
         * tcpClient 关闭并重连
         *
         * @param ip
         * @param port
         * @param callback
         */
        public void reOpenConnect(String ip, int port, StreamSourceCallback callback) {
            Log.d("yao", "createDataChannel reOpenConnect");
            stopHeartBeat();
            if (tcpClient != null) {
                tcpClient.close();
                tcpClient.reOpen(ip, port, callback);
            }

            createTime = System.currentTimeMillis();
        }


        public void reConnect() {
            Log.d("yao", "createDataChannel reConnect");
            stopHeartBeat();
            if (tcpClient != null) {
                tcpClient.close();
                tcpClient.open();
            }

            /*if (dataChannelClient != null) {
                dataChannelClient.iceRestart();
            }*/
            createTime = System.currentTimeMillis();
        }


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


        private void checkConnectionStatus() {
            try {
                Session target = ssContext.getSessionManager().getConnectedSession();
                if (target != null) {
                    String lsid = target.getId();
                    IConnectResult resultSSE = new IConnectResult() {
                        @Override
                        public IBinder asBinder() {
                            return null;
                        }

                        @Override
                        public void onProgress(String lsid, int code, String info) throws RemoteException {
                            Log.e(TAG, "onProgress code : " + code + "  info=" + info);
                        }

                        @Override
                        public void onFail(String lsid, int code, String info) throws RemoteException {
                            Log.e(TAG, "result onFail code : " + code + "  info=" + info);
                            ssContext.getSessionManager().connectChannelSessionState(
                                    Constants.COOCAA_IOT_CHANNEL_TYPE_SSE, Constants.COOCAA_IOT_CHANNEL_STATE_DISCONNECT);
                        }

                    };
                    ssContext.getController().connectSSETest(lsid, resultSSE);


                    String ip = target.getExtra(SSChannel.STREAM_LOCAL);
                    SpeedTest.ConnectCallback callback = new SpeedTest.ConnectCallback() {

                        @Override
                        public void onFinished(float speed, String unit) {

                        }

                        @Override
                        public void onProgress(float rate, String unit) {

                        }

                        @Override
                        public void onResult(int code, String message) {
                            Log.e(TAG, "SpeedTest result onFail code : " + code + "  info=" + message);
                            if (code < 0) {
                                ssContext.getSessionManager().connectChannelSessionState(
                                        Constants.COOCAA_IOT_CHANNEL_TYPE_LOCAL, Constants.COOCAA_IOT_CHANNEL_STATE_DISCONNECT);
                            }
                        }

                        @Override
                        public void lossRate(String rate) {

                        }
                    };
                    SpeedTest test = new SpeedTest(ip, 2, 1, callback);
                    test.open();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 心跳协议请求
         */
        private void heatBeat() {
            if (heartBeatCount > 2) {
                Log.d(TAG, "socket client heatBeat timeout and reconnect---");
                checkConnectionStatus();
                changeAvailable(false);
                reConnect();
                return;
            }
            heartBeatCount++;
            tcpClient.ping(HEART_BEAT_STR);
            Log.d(TAG, "socket client heatBeat---" + heartBeatCount);
        }

        private final Map<String, IMMessageCallback> mImMessageCallbackMap;

        @Override
        public void send(IMMessage msg, IMMessageCallback callback) throws Exception {
            try {
                if (channelAvailable) {
                    mChannelStatistics.sendMessage(msg);
                    Log.d(TAG, "tcp client sendMessage=" + msg.toString());
                    IMMessage imMessage = modifyMessageTcp(msg, ssContext);
                    byte[] data = imMessage.encode().getBytes();
                    tcpClient.sendData(data);

                    String msgId = msg.getId();
                    if (!TextUtils.isEmpty(msgId)
                            && callback != null
                            && mImMessageCallbackMap != null
                            && !mImMessageCallbackMap.containsKey(msgId)) {
                        mImMessageCallbackMap.put(msgId, callback);
                    }
                } else if (dataChannelClient != null && dataChannelClient.isOpen()) {
                    Log.d(TAG, "dataChannel client sendMessage=" + msg.toString());
                    dataChannelSend(msg, dataChannelClient, ssContext);
                } else {
                    if (callback != null) {
                        callback.onEnd(msg, -1, "tcp local send error");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private synchronized void changeAvailable(boolean available) {
            if (channelAvailable != available)
                channelAvailable = available;
            if (monitor != null) {
                monitor.onAvailableChanged(channelAvailable);
            }
        }


    }


    private static IMMessage modifyMessageTcp(IMMessage message, SSContext ssContext) throws IOException {
        switch (message.getType()) {
            case VIDEO:
            case IMAGE:
            case AUDIO:
            case DOC:
                File source = new File(message.getContent());
                if (source.isFile() && source.exists()) {
                    String msgId = message.getId();
                    String target = ssContext.getWebServer().uploadFile(source);
                    message = IMMessage.Builder.modifyContent(message, target);
                    message.setId(msgId);
                }
                break;
            default:
                break;
        }
        return message;
    }


    private static void dataChannelSend(IMMessage message, DataChannelClient dataChannel,
                                        SSContext ssContext) throws IOException {
        switch (message.getType()) {
            case VIDEO:
            case IMAGE:
            case AUDIO:
                /*File file = new File(message.getContent());
                if (file.isFile() && file.exists()) {
                    dataChannel.sendChannelData(file, message);
                }
                break;*/
            case DOC:
                File source = new File(message.getContent());
                if (source.isFile() && source.exists()) {
                    String msgId = message.getId();
                    String target = ssContext.getWebServer().uploadFile(source);
                    message = IMMessage.Builder.modifyContent(message, target);
                    message.setId(msgId);
                    String msgStr = message.toString();
                    Log.d(TAG, "dataChannelClient text sendMessage=" + msgStr);
                    dataChannel.sendChannelData(msgStr);
                }
                break;
            default: {
                String msgStr = message.toString();
                Log.d(TAG, "dataChannelClient text sendMessage=" + msgStr);
                dataChannel.sendChannelData(msgStr);
            }
            break;
        }
    }


    private static void dataChannelSend(IMMessage message, Peer peer) {
        switch (message.getType()) {
            case VIDEO:
            case IMAGE:
            case AUDIO:
            case DOC: {
                File file = new File(message.getContent());
                if (file.isFile() && file.exists()) {
                    //peer.sendChannelData(file, message);  //todo
                }
            }
            break;
            default: {
                String msgStr = message.toString();
                Log.d(TAG, "dataChannelClient text sendMessage=" + msgStr);
                peer.sendChannelData(msgStr);
            }
            break;
        }
    }

}
