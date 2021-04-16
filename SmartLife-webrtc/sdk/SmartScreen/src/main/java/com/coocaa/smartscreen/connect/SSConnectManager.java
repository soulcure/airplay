package com.coocaa.smartscreen.connect;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;

import com.coocaa.smartscreen.BleClientManager;
import com.coocaa.smartscreen.businessstate.object.User;
import com.coocaa.smartscreen.connect.callback.BindCallback;
import com.coocaa.smartscreen.connect.callback.ConnectCallback;
import com.coocaa.smartscreen.connect.service.MainSSClientService;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.account.TpTokenInfo;
import com.coocaa.smartscreen.data.channel.AccountParams;
import com.coocaa.smartscreen.data.channel.CmdData;
import com.coocaa.smartscreen.data.channel.DeviceParams;
import com.coocaa.smartscreen.data.channel.LocalMediaParams;
import com.coocaa.smartscreen.data.channel.PlayParams;
import com.coocaa.smartscreen.data.channel.ReverseScreenParams;
import com.coocaa.smartscreen.data.channel.SkySourceAccountData;
import com.coocaa.smartscreen.data.channel.StartAppParams;
import com.coocaa.smartscreen.data.channel.events.ConnectEvent;
import com.coocaa.smartscreen.data.channel.events.UnbindEvent;
import com.coocaa.smartscreen.data.device.TvProperty;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.callback.RepositoryCallback;
import com.coocaa.smartscreen.repository.service.DeviceRepository;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.coocaa.smartscreen.uiimpl.IToast;
import com.coocaa.smartscreen.utils.DeviceListManager;
import com.coocaa.smartscreen.utils.NetworkUtils;
import com.coocaa.smartscreen.utils.SpUtil;
import com.coocaa.smartscreen.utils.TouchEventUtil;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.skyworth.bleclient.BluetoothClientCallback;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import androidx.annotation.NonNull;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.ss.SSAdminChannel;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceAdminManager;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.DeviceManager;
import swaiotos.channel.iot.ss.device.IConnectResult;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.ss.session.SessionManager;


/**
 * @ClassName SSConnectManager
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/3/30
 * @Version TODO (write something)
 */
public class SSConnectManager {
    private static final String TAG = SSConnectManager.class.getSimpleName();

    private static final int CONNECT_SUCCESS = 0;
    private static final int CONNECT_FAILURE = 1;
    private static final int UNBIND = 3;
    private static final int UNBIND_BY_DEVICE = 4;

    private static final int ON_SESSION_CONNECT = 5;
    private static final int ON_SESSION_UPDATE = 6;
    private static final int ON_SESSION_DISCONNECT = 7;

    private static final int CONNECT_HISTORY_SUCCESS = 8;
    private static final int CONNECT_HISTORY_FAILURE = 9;
    private static final int ON_CHECK_CONNECT = 10;

    private static final int ON_CONNECTING = 11;
    private static final int ON_HISTORY_CONNECTING = 12;

    public static final String TARGET_CLIENT_TEST = "ss-clientID_12345";
    public static final String TARGET_CLIENT_APP_STORE = "ss-clientID-appstore_12345";
    public static final String TARGET_CLIENT_MEDIA_PLAYER = "ss-clientID-UniversalMediaPlayer";
    public static final String TARGET_CLIENT_MOVIE = "ss-clientID-movie";
    public static final String TARGET_CAPTURE_APP = "com.sficast.capture.app";
    public static final String TARGET_APPSTATE = "ss-iotclientID-9527";

    public static final String HISTORY_LSID = "history_lsid";
    public static final String HISTORY_DEVICE = "history_device";

    private static final String RESPONSE = "response";

    private static SSConnectManager mInstance;

    private Context mContext;
    private IToast mToast;

    private boolean canSyncAccount;

    private SSAdminChannel ssChannel;
    private Session my;
    private Session target;

    private Device mDevice;

    private TvProperty mTvProperty;

    private int mConnectState;
    public final static int CONNECT_NOTHING = 0;
    public final static int CONNECT_SSE = 1;
    public final static int CONNECT_LOCAL = 2;
    public final static int CONNECT_BOTH = 3;

    private int mConnectingState;
    public final static int CONNECTING_NOTHING = 0;
    public final static int CONNECTING_SSE = 1;
    public final static int CONNECTING_LOCAL = 2;
    public final static int CONNECTING_BOTH = 3;

    private final MyHandler mHandler = new MyHandler(this);

    private CountDownLatch sseLatch = new CountDownLatch(1);

    private ReentrantLock rLock = new ReentrantLock();
    private final long CONNECT_TIME = 5000 * 2;//连接失败，拉长时间
    private final long CONNECT_LOCK_TIME = CONNECT_TIME + 500;

    //投屏限制
    public final static String FORCE_LAN = "FORCE_LAN"; //强制局域网
    public final static String FORCE_WAN = "FORCE_WAN"; //强制广域网
    public final static String NORMAL = "NORMAL"; //广域网/局域网都可用
    public final static int PUSH_INVALID_NOT_CONNECT = 1; //没有连接设备
    public final static int PUSH_INVALID_NOT_SAME_WIFI = 2; //不是同一wifi
    public final static int PUSH_VALID = 3; //可以push
    public final static int PUSH_INVALID_NOT_PING = 4;//同一wif但是无法PING通

    private class MyHandler extends Handler {

        //对Activity的弱引用
        private final WeakReference<SSConnectManager> mActivity;

        public MyHandler(SSConnectManager activity) {
            super(Looper.getMainLooper());
            mActivity = new WeakReference<SSConnectManager>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle bundle = msg.getData();
            Session session = null;
            switch (msg.what) {
                case ON_CONNECTING:
                    Log.d(TAG, "handleMessage: ON_CONNECTING");
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.onConnecting();
                    }
                    break;

                case ON_HISTORY_CONNECTING:
                    Log.d(TAG, "handleMessage: ON_HISTORY_CONNECTING");
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.onHistoryConnecting();
                    }
                    break;

                case CONNECT_SUCCESS:
                    Log.d(TAG, "handleMessage: CONNECT_SUCCESS");
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.onSuccess(new ConnectEvent(true, mDevice, ""));
                    }
                    //获取视频源
                    getVideoSourceCmd();
                    break;

                case CONNECT_HISTORY_SUCCESS:
                    Log.d(TAG, "handleMessage: CONNECT_HISTORY_SUCCESS");
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.onHistorySuccess(new ConnectEvent(true, mDevice, ""));
                    }
                    //获取视频源
                    getVideoSourceCmd();
                    break;

                case CONNECT_FAILURE:
                    Log.d(TAG, "handleMessage: CONNECT_FAILURE");
                    String s = bundle.getString("msg");
                    if (!TextUtils.isEmpty(s)) {
                        Log.d(TAG, s);
                    }
                    for (ConnectCallback callback : connectCallbacks) {
                        Log.d(TAG, "handleMessage: CONNECT_FAILURE device = " + mDevice);
                        callback.onFailure(new ConnectEvent(false, mDevice, s));
                    }
                    break;

                case CONNECT_HISTORY_FAILURE:
                    Log.d(TAG, "handleMessage: CONNECT_HISTORY_FAILURE");
                    for (ConnectCallback callback : connectCallbacks) {
                        Log.d(TAG, "handleMessage: CONNECT_HISTORY_FAILURE device = " + mDevice);
                        callback.onHistoryFailure(new ConnectEvent(false, mDevice, bundle.getString("msg")));
                    }
                    break;

                case ON_CHECK_CONNECT:
                    Log.d(TAG, "handleMessage: onCheckConnect");
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.onCheckConnect(new ConnectEvent(true, mDevice, ""));
                    }
                    break;

                case UNBIND:
                    Log.d(TAG, "handleMessage: UNBIND");
                    UnbindEvent unbindEvent = bundle.getParcelable("UnbindEvent");
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.onUnbind(unbindEvent);
                    }
                    break;

                case UNBIND_BY_DEVICE:
                    Log.d(TAG, "handleMessage: UNBIND");
                    UnbindEvent unbindByDeviceEvent = bundle.getParcelable("UnbindEvent");
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.onUnbindByDevice(unbindByDeviceEvent);
                    }
                    break;

                case ON_SESSION_CONNECT:
                    Log.d(TAG, "handleMessage: ON_SESSION_CONNECT");
                    session = bundle.getParcelable("session");
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.onSessionConnect(session);
                    }
                    break;

                case ON_SESSION_UPDATE:
                    Log.d(TAG, "handleMessage: ON_SESSION_UPDATE");
                    session = bundle.getParcelable("session");
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.onSessionUpdate(session);
                    }
                    break;

                case ON_SESSION_DISCONNECT:
                    Log.d(TAG, "handleMessage: ON_SESSION_DISCONNECT");
                    session = bundle.getParcelable("session");
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.onSessionDisconnect(session);
                    }
                    break;


            }
        }
    }

    /**
     * callback start
     */
    private Set<ConnectCallback> connectCallbacks;

    public void addConnectCallback(ConnectCallback callback) {
        connectCallbacks.add(callback);
        Log.d(TAG, "after addConnectCallback: " + connectCallbacks.size());
    }

    public void removeConnectCallback(ConnectCallback callback) {
        connectCallbacks.remove(callback);
    }

    private LinkedHashSet<ScreenshotCallback> screenshotCallbacks;

    public interface ScreenshotCallback {
        void onSuccess();

        void onFailure(String s);
    }

    public void addScreenshotCallback(ScreenshotCallback callback) {
        screenshotCallbacks.add(callback);
    }

    public void removeScreenshotCallback(ScreenshotCallback callback) {
        screenshotCallbacks.remove(callback);
    }

    /**
     * callback end
     */


    private SSConnectManager() {
    }

    public void init(Context context) {
        if (null == mContext) {
            mContext = context;
            connectCallbacks = new CopyOnWriteArraySet<>();
            screenshotCallbacks = new LinkedHashSet<>();
            open();
            BleClientManager.instance(mContext).addScanCallBack(scanCallBack);
            DeviceListManager.getInstance().init(mContext);
        }
    }

    public void destroyManager() {
        mContext = null;
        mHandler.removeCallbacksAndMessages(null);
        connectCallbacks.clear();
        screenshotCallbacks.clear();
        removeOnDeviceChangedListener();
        removeOnDeviceBindListener();
        removeOnSessionUpdateListener();
        removeOnMySessionUpdateListener();
        removeOnDevicesReflushListener();
        removeOnDeviceInfoUpdateListener();
        BleClientManager.instance(mContext).removeScanCallBack(scanCallBack);
    }

    public synchronized static SSConnectManager getInstance() {
        if (mInstance == null) {
            mInstance = new SSConnectManager();
        }
        return mInstance;
    }

    public void setToast(IToast toast) {
        mToast = toast;
    }

    public Session getTarget() {
        return target;
    }

    public Session getMy() {
        return my;
    }

    public Device getDevice() {
        return mDevice;
    }

    public String getVideoSource() {
        String source = "iqiyi";
        if (null != mDevice) {
            DeviceInfo deviceInfo = mDevice.getInfo();
            if (deviceInfo != null && deviceInfo.type() == DeviceInfo.TYPE.TV) {
                TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) deviceInfo;
                Log.d(TAG, "getVideoSource: " + source);
                if ("tencent".equals(tvDeviceInfo.mMovieSource)
                        || "qq".equals(tvDeviceInfo.mMovieSource)) {
                    source = "qq";
                }
            }
        }
        Log.d(TAG, "getVideoSource: final source is " + source);
        return source;
    }

    public boolean isConnectedChannel() {
        if (null == ssChannel) {
            open();
            return false;
        } else {
            return true;
        }
    }

    public synchronized List<Device> getDevices() {
        try {
            if (!isConnectedChannel()) {
                return null;
            }
            return ssChannel.getDeviceAdminManager().getDevices();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            return null;
        }
    }

    //获取设备列表，强制刷新在线状态
    public synchronized List<Device> getDeviceOnlineStatus() {
        try {
            if (!isConnectedChannel()) {
                return null;
            }
            List<Device> deviceList = new ArrayList<>();
            List<Device> getDeviceOnlineStatus = ssChannel.getDeviceAdminManager().getDeviceOnlineStatus();
            if (getDeviceOnlineStatus != null) {
                deviceList.addAll(getDeviceOnlineStatus);
            }
            return deviceList;
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            return null;
        }
    }

    public synchronized void checkDeviceList(List<Device> devices) {
        if (null != devices && !devices.isEmpty()) {
            Log.d(TAG, "updateStatusByDevices: 有设备");
            boolean hasHistoryDevice = false;
            Device historyDevice = getHistoryDevice();
            if (null != historyDevice) {
                for (int i = 0; i < devices.size(); i++) {
                    Device device = devices.get(i);
                    if (device.equals(historyDevice)) {
                        hasHistoryDevice = true;
                        //更新设备的其他信息
                        device.setLastConnectTime(historyDevice.getLastConnectTime());
                        saveHistoryDevice(device);
                    }
                }
                Log.d(TAG, "updateStatusByDevices hasHistoryDevice :" + hasHistoryDevice);
                if (!hasHistoryDevice) {
                    Log.d(TAG, "updateStatusByDevices: 设备列表没有历史设备，清空历史设备");
                    disconnect();
                    clearHistoryDevice();
                }
            }
        } else {
            Log.d(TAG, "updateStatusByDevices: 设备列表空，清空历史设备");
            disconnect();
            clearHistoryDevice();
        }
    }

    //获取当前连接的设备，判断是否连接要用isConnected()
    public synchronized Session getConnectSession() {
        Session session = null;
        try {
            session = ssChannel.getSessionManager().getConnectedSession();
            Log.d(TAG, "getConnectSession: " + session);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        return session;
    }

    public synchronized Session getMySession() {
        Session session = null;
        try {
            session = ssChannel.getSessionManager().getMySession();
            Log.d(TAG, "getMySession: " + session);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        return session;
    }

    public synchronized boolean isConnectSSE() {
        boolean isConnectSSE = false;
        try {
            isConnectSSE = ssChannel.getSessionManager().isConnectSSE();
            Log.d(TAG, "isConnectSSE: " + isConnectSSE);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        return isConnectSSE;
    }

    /**
     * @return {@link #CONNECTING_NOTHING} 未连接
     * {@link #CONNECTING_SSE} sse连接
     * {@link #CONNECTING_LOCAL} 本地连接
     * {@link #CONNECTING_BOTH} 都连接
     */
    public int getConnectingState() {
        Log.d(TAG, "getConnectingState: " + mConnectingState);
        return mConnectingState;
    }

    public boolean isConnecting() {
        return mConnectingState > 0;
    }

    /**
     * @return {@link #CONNECT_NOTHING} 未连接
     * {@link #CONNECT_SSE} sse连接
     * {@link #CONNECT_LOCAL} 本地连接
     * {@link #CONNECT_BOTH} 都连接
     */
    public int getConnectState() {
        Log.d(TAG, "getConnectState: " + mConnectState);
        return mConnectState;
    }

    /**
     * 后续使用{@link #getConnectState()}
     *
     * @return
     */
    public boolean isSameWifi() {
        Log.d(TAG, "isSameWifi: " + mConnectState);
        return mConnectState == CONNECT_LOCAL
                || mConnectState == CONNECT_BOTH;
//        return getWifiConnectStatus() != 0;
    }

    /**
     * 后续使用{@link #getConnectState()}
     */
    public boolean isConnected() {
        Log.d(TAG, "isConnected: " + mConnectState);
        return mConnectState > CONNECT_NOTHING;
        /*boolean isConnected = false;
        try {
            Session session = ssChannel.getSessionManager().getConnectedSession();
            if (null != session) {
                //1代表云端，2代码本地，3代表云端+本地
                if ("1".equals(session.getExtra("connectStatus"))
                        || "2".equals(session.getExtra("connectStatus"))
                        || "3".equals(session.getExtra("connectStatus"))) {
                    isConnected = true;
                }
            }
            Log.d(TAG, "isConnected: " + session);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        return isConnected;*/
    }


    /**
     * 判断本地连接
     * 后续使用{@link #getConnectState()}
     *
     * @return 1代表连接了同一个wifi, 但是局域网无法ping通，适合发送云端指令的场景， 2 代表局域网可以ping通，适合发送图片，视频和文档
     */
    @Deprecated
    public int getWifiConnectStatus() {
        int res = 0;
        if (available(SSChannel.STREAM_LOCAL)) {
            res = 2;
        } else {
            WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            String wifiName = ssid.replace("\"", "").trim();
            if (!TextUtils.isEmpty(wifiName) && target != null) {
                String dongleWifi = target.getExtra("ssid");
                if (!TextUtils.isEmpty(dongleWifi) && dongleWifi.equals(wifiName)) {
                    res = 1;
                }
            }
        }
        return res;
    }

    public boolean available(String channel) {
        boolean available = false;
        try {
            Log.d(TAG, "available target: " + target + "\n"
                    + "channel: " + channel);
            available = ssChannel.getSessionManager().available(target, channel);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        Log.d(TAG, channel + " available: " + available);
        return available;
    }

    /**
     * 是否可以push
     *
     * @param networkForceType 可以传null，表示默认局域网，取值范围 {@link SSConnectManager#FORCE_LAN} or {@link SSConnectManager#FORCE_WAN} or {@link SSConnectManager#NORMAL}
     * @return
     */
    public int checkPushValid(String networkForceType) {
        if (mConnectState == 2 || mConnectState == 3) {
            Log.d(TAG, "checkPushValid loginState=" + mConnectState);
            return PUSH_VALID;
        }

        if (!isConnected()) {
            return PUSH_INVALID_NOT_CONNECT;
        }
        if (FORCE_LAN.equals(networkForceType) || TextUtils.isEmpty(networkForceType)) {
            int state = getWifiConnectStatus();
            if (state == 0) {
                return PUSH_INVALID_NOT_SAME_WIFI;
            } else if (state == 1) {
                return PUSH_INVALID_NOT_PING;
            }
        }
        return PUSH_VALID;
    }

    public boolean isSupportSyncScreen() {
        boolean isSupportSyncScreen = false;
        if (null != mTvProperty) {
            isSupportSyncScreen = mTvProperty.isSupportSyncScreen;
        }
        return isSupportSyncScreen;
    }

    //新流程start

    /**
     * 绑定channel service
     *
     * @param
     * @param
     */
    public synchronized void open() {
        Log.d(TAG, "open: 绑定channel service");
        String channelPackage = mContext.getPackageName();
        //pad端，pad端需要设置固定包名
        if ("com.coocaa.interconnected".equals(channelPackage)) {
            channelPackage = "swaiotos.channel.iot";
        }
        Log.d(TAG, "open: " + channelPackage);
        IOTAdminChannel.mananger.open(mContext, channelPackage, new IOTAdminChannel.OpenCallback() {

            @Override
            public void onConntected(SSAdminChannel channel) {
                Log.d(TAG, "onConntected: 绑定channel成功");
                ssChannel = IOTAdminChannel.mananger.getSSAdminChannel();
                BleClientManager.instance(mContext).setSsChannel(ssChannel,
                        new BleClientManager.InitDevicesCallBack() {
                            @Override
                            public void onResult(List<Device> list) {
                                connectHistory();
                            }
                        });

                try {
                    my = ssChannel.getSessionManager().getMySession();
                    Log.d(TAG, "my: " + my.getExtras());
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
                addOnDeviceChangedListener();
                addOnDeviceBindListener();
                addOnSessionUpdateListener();
                addOnMySessionUpdateListener();
                addOnDevicesReflushListener();
                addOnDeviceInfoUpdateListener();

            }

            @Override
            public void onError(String s) {
                Log.d(TAG, "onError: " + s);
                Bundle bundle = new Bundle();
                bundle.putString("msg", s);
                Message message = Message.obtain();
                message.setData(bundle);   //message.obj=bundle  传值也行
                message.what = CONNECT_FAILURE;
                mHandler.sendMessage(message);
            }
        });
        Log.d(TAG, "open end");
    }

    //重设智屏账号：目前只有手机端用,必须先reset再disconnect
    public void resetLsid(String lsid, String token) {
        try {
            ssChannel.getIMChannel().reset(lsid, token);
            disconnect();
            clearHistoryDevice();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    //重设智屏账号：目前只有手机端用,必须先reset再disconnect
    public void resetLsid(String lsid, String token, String userId) {
        try {
            ssChannel.getIMChannel().reset(lsid, token, userId);
            disconnect();
            clearHistoryDevice();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    //退出账号，需要当前连接的设备离开房间
    public void leaveRoom() {
        try {
            ssChannel.getController().leave("1");
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    public void bind(final String bindCode, final BindCallback bindCallback) {
        Log.d(TAG, "bind: 绑定设备");
        String token = null;
        try {
            //手机端才会有token，并且要单独捕获异常处理
            token = Repository.get(LoginRepository.class).queryDeviceRegisterLoginInfo().access_token;
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        try {
//            String token = SpUtil.getString(mContext, SpUtil.Keys.DEVICE_ACCESS_TOKEN);
            Log.d(TAG, "bindCode:" + bindCode + " token: " + token);
            ssChannel.getDeviceAdminManager().startBind(token, bindCode, new DeviceAdminManager.OnBindResultListener() {

                @Override
                public void onSuccess(final String bindCode, final Device device) {
                    Log.d(TAG, "onSuccess: bindCode = " + bindCode + "   device = " + device);
                    if (null != bindCallback) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                bindCallback.onSuccess(bindCode, device);
                            }
                        });
                    }
                    connect(device);
                    canSyncAccount = true;

                    BleClientManager.instance(mContext).addDevice(device);
                }

                @Override
                public void onFail(final String bindCode, final String errorType, final String msg) {
                    Log.d(TAG, "onFail: bindCode = " + bindCode + " errorType = " + errorType + " msg = " + msg);
                    if (null != bindCallback) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                bindCallback.onFail(bindCode, errorType, msg);
                            }
                        });
                    }
                }

            }, 20 * 1000);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            open();
            if (null != bindCallback) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        bindCallback.onFail(bindCode, "", "channel服务启动绑定失败");
                    }
                });
            }
        }
    }

    /**
     * @param uniqueId     空间id or 设备sid
     * @param type         0：lsId（设备sid） 1：spaceId(空间id)
     * @param bindCallback
     */
    public void tempBind(final String uniqueId, final int type, final BindCallback bindCallback) {
        Log.d(TAG, "bind2: 绑定设备");
        String token = null;
        try {
            //手机端才会有token，并且要单独捕获异常处理
            token = Repository.get(LoginRepository.class).queryDeviceRegisterLoginInfo().access_token;
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        try {
            ssChannel.getDeviceAdminManager().startTempBindDirect(token, uniqueId, type, new DeviceAdminManager.OnBindResultListener() {

                @Override
                public void onSuccess(final String bindCode, final Device device) {
                    Log.d(TAG, "onSuccess: bindCode = " + bindCode + "   device = " + device);
                    if (null != bindCallback) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                bindCallback.onSuccess(bindCode, device);
                            }
                        });
                    }
                    connect(device);
                    canSyncAccount = true;

                    BleClientManager.instance(mContext).addDevice(device);
                }

                @Override
                public void onFail(final String bindCode, final String errorType, final String msg) {
                    Log.d(TAG, "onFail: bindCode = " + bindCode + " errorType = " + errorType + " msg = " + msg);
                    if (null != bindCallback) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                bindCallback.onFail(bindCode, errorType, msg);
                            }
                        });
                    }
                }

            }, 20 * 1000);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            open();
            if (null != bindCallback) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        bindCallback.onFail(uniqueId, "", "channel服务启动绑定失败");
                    }
                });
            }
        }
    }

    public synchronized void connect(final Device device) {
        if (device == null) {
            return;
        }
        //连接前先断开现有的连接
        try {
            //add wyh 当前连接设备 = 目前需要连接的设备device 就不触发disconnect
            String connectSessionId = ssChannel.getSessionManager().getConnectedSession().getId();
            if (TextUtils.isEmpty(connectSessionId) || TextUtils.isEmpty(device.getLsid()) || !connectSessionId.equals(device.getLsid())) {
                disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        device.setLastConnectTime(System.currentTimeMillis());
        saveHistoryDevice(device);

        HomeIOThread.executeInSingleThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "call connect: " + device.getLsid() + " run.");
                boolean lockRet = false;
                try {
                    Log.d(TAG, "call connect try lock");
                    lockRet = rLock.tryLock(CONNECT_LOCK_TIME, TimeUnit.MILLISECONDS);
                    Log.d(TAG, "call connect try lock end.");
                    Log.d(TAG, "连接: " + device.getLsid());
                    sseLatch.await();
                    Log.d(TAG, "wait sseLatch finish 连接设备: " + device.getLsid());
                    Log.d(TAG, "connect checkout isConnectSSE: " + isConnectSSE());
                    mHandler.sendEmptyMessage(ON_CONNECTING);

                    long time = System.currentTimeMillis();
                    target = ssChannel.getController().connect(device.getLsid(), CONNECT_TIME);
                    long usedTimes = System.currentTimeMillis() - time;
                    Log.d(TAG, "sdk connect target && used times=" + usedTimes);

                    Log.d(TAG, "connect: 成功");
                    //连接成功 保存当前设备；
                    mDevice = device;
                    saveHistoryDevice(device);
                    mHandler.sendEmptyMessage(CONNECT_SUCCESS);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, e.toString());

                    Bundle bundle = new Bundle();
                    String msg = e.toString();
                    if (msg.contains("time out")) {
                        msg = "连接超时";
                    }
                    bundle.putString("msg", msg);
                    Message message = Message.obtain();
                    message.setData(bundle);   //message.obj=bundle  传值也行
                    message.what = CONNECT_FAILURE;
                    mHandler.sendMessage(message);
                    Log.d(TAG, "connect: return 111");
                    return;
                } finally {
                    if (lockRet) {
                        rLock.unlock();
                    }
                }

            }
        });
    }

    public synchronized void disconnect() {
        Log.d(TAG, "disconnect: " + target);
        if (null != target) {

            CmdData data = new CmdData("disconnect", CmdData.CMD_TYPE.STATE.toString(), "");
            String cmd = data.toJson();
            sendTextMessage(cmd, TARGET_APPSTATE);

            //耗时操作，在线程执行
            HomeIOThread.removeTask(disconnectRunnable);
            HomeIOThread.executeInSingleThread(disconnectRunnable);
        }
    }

    private Runnable disconnectRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                ssChannel.getController().disconnect(target);
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
        }
    };

    public void connectHistory() {
        if (!NetworkUtils.isAvailable(mContext)) {
            Log.d(TAG, "connectHistory: 当前网络不可用");
            return;
        }

        HomeIOThread.removeTask(connectHistoryRunnable);
        HomeIOThread.executeInSingleThread(connectHistoryRunnable);
    }

    private Runnable connectHistoryRunnable = new Runnable() {
        @Override
        public void run() {
            boolean lockRet = false;
            try {
                lockRet = rLock.tryLock(CONNECT_LOCK_TIME, TimeUnit.MILLISECONDS);
                if (!isConnectSSE()) {
                    Log.d(TAG, "connectHistory: 未登录sse服务器");
                    return;
                }

                //从历史设备获取
                Device device = getHistoryDevice();

                if (device == null) {
                    Log.d(TAG, "connectHistory: 连接历史为空");
                    return;
                }

                if (!isHistoryDeviceValid()) {
                    Log.d(TAG, "connectHistory: isHistoryDeviceValid = false");
                    return;
                }

                Log.d(TAG, "connectHistory: 时间合法 == ");

                //从设备列表获取，拿最新的设备状态
                device = getDeviceByLsid(device.getLsid());

                if (device == null) {
                    Log.d(TAG, "connectHistory: 设备列表不存在该设备");
                    return;
                }

                if (device.getStatus() == 0) {
                    Log.d(TAG, "connectHistory: 当前设备不在线");
                    return;
                }

                if (isConnected()) {
                    Log.d(TAG, "connectHistory: 已连接设备，不去重连历史设备了，从设备列表更新数据");
                    target = getConnectSession();
                    mDevice = device;
                    mHandler.sendEmptyMessage(ON_CHECK_CONNECT);
                    return;
                }

                Log.d(TAG, "connectHistory 连接设备: " + device.getLsid());
                mHandler.sendEmptyMessage(ON_HISTORY_CONNECTING);

                long time = System.currentTimeMillis();
                target = ssChannel.getController().connect(device.getLsid(), CONNECT_TIME);
                long usedTimes = System.currentTimeMillis() - time;
                Log.d(TAG, "sdk connect connectHistory: 成功 && used times=" + usedTimes);
                //连接成功 保存当前设备；
                mDevice = device;
                saveHistoryDevice(device);
                mHandler.sendEmptyMessage(CONNECT_HISTORY_SUCCESS);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());

                Bundle bundle = new Bundle();
                String msg = e.toString();
                if (msg.contains("time out")) {
                    msg = "连接超时";
                }
                bundle.putString("msg", msg);
                Message message = Message.obtain();
                message.setData(bundle);   //message.obj=bundle  传值也行
                message.what = CONNECT_HISTORY_FAILURE;
                mHandler.sendMessage(message);
                Log.d(TAG, "connectHistory: return 111");
                return;
            } finally {
                if (lockRet) {
                    rLock.unlock();
                }
            }
        }
    };

    public void unbind(String accessToken, String lsid, int type) {
        Log.d(TAG, "unbind: ");
        try {
            ssChannel.getDeviceAdminManager().unBindDevice(accessToken, lsid, type, new DeviceAdminManager.unBindResultListener() {
                @Override
                public void onSuccess(String lsid) {
                    Log.d(TAG, "unbind onSuccess: " + lsid);

                    BleClientManager.instance(mContext).removeDevice(lsid);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("UnbindEvent", new UnbindEvent(lsid, true, null, null));
                    Message message = Message.obtain();
                    message.setData(bundle);   //message.obj=bundle  传值也行
                    message.what = UNBIND;
                    mHandler.sendMessage(message);

                    Device device = getHistoryDevice();
                    if (null != device
                            && lsid.equals(device.getLsid())) {
                        Log.d(TAG, "onSuccess: 解绑的是历史设备，清空历史，断开连接");
                        disconnect();
                        clearHistoryDevice();
                    }


                }

                @Override
                public void onFail(String lsid, String errorType, String msg) {
                    Log.d(TAG, "unbind onFail: " + "lisd = " + lsid + "\n"
                            + "errorType = " + errorType + "\n"
                            + "msg = " + msg + "\n");
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("UnbindEvent", new UnbindEvent(lsid, false, errorType, msg));
                    Message message = Message.obtain();
                    message.setData(bundle);   //message.obj=bundle  传值也行
                    message.what = UNBIND;
                    mHandler.sendMessage(message);
                }
            });
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    public void selectDevice(Device device) {
        for (ConnectCallback callback : connectCallbacks) {
            callback.onDeviceSelected(new ConnectEvent(false, device, ""));
        }
    }

    @Deprecated
    public String getHistoryLsid() {
        return SpUtil.getString(mContext, HISTORY_LSID, "");
    }

    @Deprecated
    public void setHistoryLsid(String lsid) {
        SpUtil.putString(mContext, HISTORY_LSID, lsid);
    }

    public void saveHistoryDevice(Device device) {
        String jsonStr = "";
        if (null != device) {
            jsonStr = new Gson().toJson(device);
        }
        Log.d(TAG, "saveHistoryDevice: " + jsonStr);
        SpUtil.putString(mContext, HISTORY_DEVICE, jsonStr);
    }

    public void clearHistoryDevice() {
        Log.d(TAG, "clearHistoryDevice: ");
        SpUtil.putString(mContext, HISTORY_DEVICE, "");
    }

    public Device getHistoryDevice() {
        Device device = null;
        String jsonStr = SpUtil.getString(mContext, HISTORY_DEVICE);
        Log.d(TAG, "getHistoryDevice: " + jsonStr);

        if (!TextUtils.isEmpty(jsonStr)) {
            device = new Gson().fromJson(jsonStr, new TypeToken<Device<TVDeviceInfo>>() {
            }.getType());
        }
        return device;
    }

    //判断历史设备的合法性
    public boolean isHistoryDeviceValid() {
        Device device = getHistoryDevice();
        if (null == device) {
            Log.d(TAG, "isConnectTimeValid: device is null");
            return false;
        } else if (device.getLastConnectTime() > 0
                && System.currentTimeMillis() - device.getLastConnectTime() > 60 * 60 * 1000
                && !TextUtils.isEmpty(device.getMerchantId())) {
            Log.d(TAG, "isConnectTimeValid: 超过1小时限制");
            return false;
        } else {
            Log.d(TAG, "isConnectTimeValid: 时间合法");
            return true;
        }
    }

    public String getDeviceName(Device device) {
        if (null == device) {
            Log.e(TAG, "getDeviceName: device is null");
            return "";
        }

        DeviceInfo deviceInfo = device.getInfo();
        if (null != deviceInfo) {
            switch (deviceInfo.type()) {
                case TV:
                    TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) deviceInfo;
                    return tvDeviceInfo.mNickName;
            }
        }

        if (!TextUtils.isEmpty(device.getMerchantName())
                && !TextUtils.isEmpty(device.getSpaceName())) {
            return device.getMerchantName() + "-" + device.getSpaceName();
        }

        return "";
    }

    private Device getDeviceByLsid(String lsid) {

        List<Device> deviceList = getDevices();
        for (Device device :
                deviceList) {
            if (device.getLsid().equals(lsid)) {
                return device;
            }
        }

        return null;
    }
    //新流程end

    public void getVideoSourceCmd() {
        Log.d(TAG, "getVideoSource: ");
        CmdData data = new CmdData(PlayParams.CMD.GET_SOURCE.toString(), CmdData.CMD_TYPE.MEDIA.toString(), "");
        String cmd = data.toJson();
        sendTextMessage(cmd, TARGET_CLIENT_MOVIE);
    }

    public void sendAccountCmd() {
        if (!canSyncAccount) {
            return;
        }
        canSyncAccount = false;
        CmdData data = new CmdData(AccountParams.CMD.GET_ACCESS_TOKEN.toString(),
                CmdData.CMD_TYPE.ACCOUNT.toString(), "");
        String cmd = data.toJson();
        Log.d(TAG, "sendAccountCmd: " + cmd);
        sendTextMessage(cmd, TARGET_CLIENT_APP_STORE);
    }

    public void sendTextMessage(String cmd, String targetClient) {
        sendTextMessage(cmd, targetClient, -1);
    }

    public void sendTextMessage(String cmd, String targetClient, int protoVersion) {
        IMMessage message = IMMessage.Builder.createTextMessage(my, target, MainSSClientService.AUTH, targetClient, cmd);
        sendMessage(message, protoVersion);
    }

    public void sendImageMessage(String name, File content, String targetClient, IMMessageCallback imMessageCallback) {
        IMMessage message = IMMessage.Builder.createImageMessage(my, target, MainSSClientService.AUTH, targetClient, content);
        CmdData cmdData = new CmdData(LocalMediaParams.CMD.PLAY.toString(),
                CmdData.CMD_TYPE.LOCAL_MEDIA.toString(), new LocalMediaParams(name).toJson());
        message.putExtra(RESPONSE, cmdData.toJson());
        message.putExtra("showtips", "true");
        sendMessage(message, imMessageCallback);
    }

    public void sendImageMessage(String name, File content, String targetClient, boolean isNeedTip, IMMessageCallback imMessageCallback) {
        Log.d(TAG, "sendImageMessage: why_test target = " + target);
        IMMessage message = IMMessage.Builder.createImageMessage(my, target, MainSSClientService.AUTH, targetClient, content);
        CmdData cmdData = new CmdData(LocalMediaParams.CMD.PLAY.toString(),
                CmdData.CMD_TYPE.LOCAL_MEDIA.toString(), new LocalMediaParams(name).toJson());
        message.putExtra(RESPONSE, cmdData.toJson());
        if (isNeedTip) {
            message.putExtra("showtips", "true");
        } else {
            message.putExtra("showtips", "false");
        }
        Log.d(TAG, "sendImageMessage: why_test before sendMessage");
        sendMessage(message, imMessageCallback);
    }

    public void sendAudioMessage(String name, File content, String targetClient, IMMessageCallback imMessageCallback) {
        IMMessage message = IMMessage.Builder.createAudioMessage(my, target, MainSSClientService.AUTH, targetClient, content);
        CmdData cmdData = new CmdData(LocalMediaParams.CMD.PLAY.toString(),
                CmdData.CMD_TYPE.LOCAL_MEDIA.toString(), new LocalMediaParams(name).toJson());
        message.putExtra(RESPONSE, cmdData.toJson());
        message.putExtra("showtips", "true");
//        sendMessage(message);
        sendMessage(message, imMessageCallback);
    }

    public void sendVideoMessage(String name, File content, String targetClient, IMMessageCallback imMessageCallback) {
        IMMessage message = IMMessage.Builder.createVideoMessage(my, target, MainSSClientService.AUTH, targetClient, content);
        CmdData cmdData = new CmdData(LocalMediaParams.CMD.PLAY.toString(),
                CmdData.CMD_TYPE.LOCAL_MEDIA.toString(), new LocalMediaParams(name).toJson());
        message.putExtra(RESPONSE, cmdData.toJson());
        message.putExtra("showtips", "true");
//        sendMessage(message);
        sendMessage(message, imMessageCallback);
    }

    public void sendVideoMessage(String name, File content, String targetClient, boolean isNeedTip, IMMessageCallback imMessageCallback) {
        IMMessage message = IMMessage.Builder.createVideoMessage(my, target, MainSSClientService.AUTH, targetClient, content);
        CmdData cmdData = new CmdData(LocalMediaParams.CMD.PLAY.toString(),
                CmdData.CMD_TYPE.LOCAL_MEDIA.toString(), new LocalMediaParams(name).toJson());
        message.putExtra(RESPONSE, cmdData.toJson());
        if (isNeedTip) {
            message.putExtra("showtips", "true");
        } else {
            message.putExtra("showtips", "false");
        }
        sendMessage(message, imMessageCallback);
    }

    public void sendWebRTCMessage(String content) {
        final String TARGET_CLIENT = "com.coocaa.webrtc.airplay";

        IMMessage message = IMMessage.Builder.createTextMessage(my, target,
                MainSSClientService.AUTH, TARGET_CLIENT, content);
        message.putExtra(SSChannel.FORCE_SSE, "true");//强制云端
        message.putExtra("target-client", MainSSClientService.AUTH);//回复消息target
        sendMessage(message);
    }

    public void sendWebRTCVoice(String content) {
        final String TARGET_CLIENT = "com.coocaa.webrtc.airplay.voice";

        IMMessage message = IMMessage.Builder.createTextMessage(my, target,
                MainSSClientService.AUTH, TARGET_CLIENT, content);
        message.putExtra(SSChannel.FORCE_SSE, "true");//强制云端
        message.putExtra("target-client", "ss-clientID-WebRTC-Sound");//回复消息target
        sendMessage(message);
    }

    public void sendMessage(IMMessage message, IMMessageCallback imMessageCallback) {
        try {
//            message.putExtra(SSChannel.FORCE_SSE,"true");//强行开启sse通道

            if (!NetworkUtils.isAvailable(mContext)) {
                showToast("当前网络不可用，请检查网络设置");
                return;
            }
            CoocaaUserInfo coocaaUserInfo = Repository.get(LoginRepository.class).queryCoocaaUserInfo();
            User user = User.builder().userID(coocaaUserInfo.open_id).token(coocaaUserInfo.access_token)
                    .mobile(coocaaUserInfo.mobile).nickName(coocaaUserInfo.nick_name).avatar(coocaaUserInfo.avatar).build();
            message.putExtra("mobile", coocaaUserInfo.mobile);
            message.putExtra("open_id", coocaaUserInfo.open_id);
            message.putExtra("owner", User.encode(user));
            ssChannel.getIMChannel().send(message, imMessageCallback);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    public void sendMessage(IMMessage message) {
        sendMessage(message, -1);
    }

    /**
     * @param message
     * @param protoVersion 拉平版本
     */
    public void sendMessage(IMMessage message, int protoVersion) {
        try {
//            message.putExtra(SSChannel.FORCE_SSE,"true");//强行开启sse通道

            if (!NetworkUtils.isAvailable(mContext)) {
                showToast("当前网络不可用，请检查网络设置");
                return;
            }
            CoocaaUserInfo coocaaUserInfo = Repository.get(LoginRepository.class).queryCoocaaUserInfo();
            User user = User.builder().userID(coocaaUserInfo.open_id).token(coocaaUserInfo.access_token)
                    .mobile(coocaaUserInfo.mobile).nickName(coocaaUserInfo.nick_name).avatar(coocaaUserInfo.avatar).build();
            message.putExtra("mobile", coocaaUserInfo.mobile);
            message.putExtra("open_id", coocaaUserInfo.open_id);
            message.putExtra("owner", User.encode(user));
            if (protoVersion >= 0) {
                message.setReqProtoVersion(protoVersion);
            }
            ssChannel.getIMChannel().send(message, new IMMessageCallback() {
                @Override
                public void onStart(IMMessage message) {
//                    Log.d(TAG, "onStart: " + message);
                }

                @Override
                public void onProgress(IMMessage message, int progress) {
//                    Log.d(TAG, "onProgress: " + progress);
                }

                @Override
                public void onEnd(IMMessage message, int code, String info) {
//                    Log.d(TAG, "onEnd: code=" + code + "\n info:" + info);
                }
            });
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    /**
     * 网络测试
     */
    public void sendMessageSSETest(@NonNull String lsid, @NonNull IConnectResult result) {
        try {
            ssChannel.getController().connectSSETest(lsid, result);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    public void sendMessageLocalTest(@NonNull String lsid, @NonNull IConnectResult result) {
        try {
            ssChannel.getController().connectLocalTest(lsid, result);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    public void reqClientProto(final String id, final String targetClient) {
        Log.d(TAG, "reqClientProto target=" + targetClient + ", id=" + id);
        if (ssChannel == null)
            return;
        IMMessage message = new IMMessage.Builder()
                .setTarget(target)
                .setSource(my)
                .setClientTarget(targetClient)
                .setClientSource(MainSSClientService.AUTH)
                .setType(IMMessage.TYPE.PROTO)
                .setId(id)
                .build();
        try {
            ssChannel.getIMChannel().send(message, new IMMessageCallback() {
                @Override
                public void onStart(IMMessage message) {
                }

                @Override
                public void onProgress(IMMessage message, int progress) {
                }

                @Override
                public void onEnd(IMMessage message, int code, String info) {
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "reqClientProto target=" + targetClient + ", error=" + e.toString());
            e.printStackTrace();
        }
    }

    private void showToast(String string) {
        if (null != mToast) {
            mToast.show(string);
        }
    }

    /*本地listener start*/
    private void addOnDeviceChangedListener() {
        try {
            ssChannel.getDeviceAdminManager().addOnDeviceChangedListener(mOnDeviceChangedListener);
        } catch (RemoteException e) {
            Log.d(TAG, e.toString());
        }
    }

    private void removeOnDeviceChangedListener() {
        try {
            ssChannel.getDeviceAdminManager().removeOnDeviceChangedListener(mOnDeviceChangedListener);
        } catch (RemoteException e) {
            Log.d(TAG, e.toString());
        }
    }

    DeviceAdminManager.OnDeviceChangedListener mOnDeviceChangedListener = new DeviceAdminManager.OnDeviceChangedListener() {
        @Override
        public void onDeviceOffLine(final Device device) {
            Log.d(TAG, "OnDeviceChangedListener onDeviceOffLine: " + device.getLsid());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.onDeviceOffLine(device);
                    }
                }
            });
        }

        @Override
        public void onDeviceOnLine(final Device device) {
            Log.d(TAG, "OnDeviceChangedListener onDeviceOnLine: " + device.getLsid());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.onDeviceOnLine(device);
                    }
                }
            });
        }

        @Override
        public void onDeviceUpdate(final Device device) {
            Log.d(TAG, "OnDeviceChangedListener onDeviceUpdate: " + device.getLsid());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.onDeviceUpdate(device);
                    }
                }
            });
        }
    };

    private void addOnDeviceBindListener() {
        try {
            ssChannel.getDeviceAdminManager().addDeviceBindListener(mOnDeviceBindListener);
        } catch (RemoteException e) {
            Log.d(TAG, e.toString());
        }
    }

    private void removeOnDeviceBindListener() {
        try {
            ssChannel.getDeviceAdminManager().removeDeviceBindListener(mOnDeviceBindListener);
        } catch (RemoteException e) {
            Log.d(TAG, e.toString());
        }
    }

    DeviceAdminManager.OnDeviceBindListener mOnDeviceBindListener = new DeviceAdminManager.OnDeviceBindListener() {
        @Override
        public void onDeviceBind(String lsid) {
            Log.d(TAG, "onDeviceBind: " + lsid);
        }

        @Override
        public void onDeviceUnBind(String lsid) {
            Log.d(TAG, "onDeviceUnBind: " + lsid);
            Bundle bundle = new Bundle();
            bundle.putParcelable("UnbindEvent", new UnbindEvent(lsid, true, null, null));
            Message message = Message.obtain();
            message.setData(bundle);   //message.obj=bundle  传值也行
            message.what = UNBIND_BY_DEVICE;
            mHandler.sendMessage(message);

            if (!TextUtils.isEmpty(lsid)) {
                Device device = getHistoryDevice();
                if (null != device &&
                        lsid.equals(device.getLsid())) {
                    Log.d(TAG, "onDeviceUnBind: 解绑的是历史设备，清空历史，断开连接");
                    disconnect();
                    clearHistoryDevice();
                }
            }
        }
    };

    private void addOnSessionUpdateListener() {
        try {
            ssChannel.getSessionManager().addConnectedSessionOnUpdateListener(mOnSessionUpdateListener);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    private void removeOnSessionUpdateListener() {
        try {
            ssChannel.getSessionManager().removeConnectedSessionOnUpdateListener(mOnSessionUpdateListener);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    SessionManager.OnSessionUpdateListener mOnSessionUpdateListener = new SessionManager.OnSessionUpdateListener() {
        @Override
        public void onSessionConnect(Session session) {
            Log.d(TAG, "OnSessionUpdateListener onSessionConnect: " + session);

            Device device = getHistoryDevice();
            if (null != device && device.getLsid().equals(session.getId())) {
                target = session;
                //更新设备时间
                device.setLastConnectTime(System.currentTimeMillis());
                saveHistoryDevice(device);
                //获取电视机账号信息
                sendAccountCmd();

                Bundle bundle = new Bundle();
                bundle.putParcelable("session", session);
                Message message = Message.obtain();
                message.setData(bundle);   //message.obj=bundle  传值也行
                message.what = ON_SESSION_CONNECT;
                mHandler.sendMessage(message);
            } else {
                disconnect();
                clearHistoryDevice();
            }

        }

        @Override
        public void onSessionUpdate(Session session) {
            Log.d(TAG, "OnSessionUpdateListener onSessionUpdate: " + session);
            if (null != target && target.getId().equals(session.getId())) {
                target = session;

                Bundle bundle = new Bundle();
                bundle.putParcelable("session", session);
                Message message = Message.obtain();
                message.setData(bundle);   //message.obj=bundle  传值也行
                message.what = ON_SESSION_UPDATE;
                mHandler.sendMessage(message);
            }
        }

        @Override
        public void onSessionDisconnect(Session session) {
            Log.d(TAG, "OnSessionUpdateListener onSessionDisconnect: " + session);
//            tryConnectAfterSessionDisconnect(session);
            if (null != target && target.getId().equals(session.getId())) {
                Log.d(TAG, "onSessionDisconnect: 断开连接 清空target");
                target = null;
//                mDevice = null;
                mTvProperty = null;

                Bundle bundle = new Bundle();
                bundle.putParcelable("session", session);
                Message message = Message.obtain();
                message.setData(bundle);   //message.obj=bundle  传值也行
                message.what = ON_SESSION_DISCONNECT;
                mHandler.sendMessage(message);
            }
        }
    };

    private void addOnMySessionUpdateListener() {
        try {
            ssChannel.getSessionManager().addOnMySessionUpdateListener(mOnMySessionUpdateListener);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    private void removeOnMySessionUpdateListener() {
        try {
            ssChannel.getSessionManager().removeOnMySessionUpdateListener(mOnMySessionUpdateListener);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    SessionManager.OnMySessionUpdateListener mOnMySessionUpdateListener = new SessionManager.OnMySessionUpdateListener() {
        @Override
        public void onMySessionUpdate(Session mySession) {
            Log.d(TAG, "onMySessionUpdate: " + mySession);
            my = mySession;
            Log.d(TAG, "onMySessionUpdate: my" + my);
            //wifi重连的时候去连接下历史设备
            if (NetworkUtils.isAvailable(mContext))
                connectHistory();
        }
    };

    private void addOnDevicesReflushListener() {
        try {
            ssChannel.getDeviceAdminManager().addDevicesReflushListener(mOnDevicesReflushListener);
        } catch (RemoteException e) {
            Log.d(TAG, e.toString());
        }
    }

    private void removeOnDevicesReflushListener() {
        try {
            ssChannel.getDeviceAdminManager().removeDevicesReflushListener(mOnDevicesReflushListener);
        } catch (RemoteException e) {
            Log.d(TAG, e.toString());
        }
    }

    DeviceManager.OnDevicesReflushListener mOnDevicesReflushListener = new DeviceManager.OnDevicesReflushListener() {
        @Override
        public void onDeviceReflushUpdate(final List<Device> devices) {
            checkDeviceList(devices);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.onDeviceReflushUpdate(devices);
                    }
                }
            });
        }
    };

    private void addOnDeviceInfoUpdateListener() {
        try {
            ssChannel.getDeviceAdminManager().addDeviceInfoUpdateListener(mOnDeviceInfoUpdateListener);
        } catch (RemoteException e) {
            Log.d(TAG, e.toString());
        }
    }

    private void removeOnDeviceInfoUpdateListener() {
        try {
            ssChannel.getDeviceAdminManager().removeDeviceInfoUpdateListener(mOnDeviceInfoUpdateListener);
        } catch (RemoteException e) {
            Log.d(TAG, e.toString());
        }
    }

    DeviceManager.OnDeviceInfoUpdateListener mOnDeviceInfoUpdateListener = new DeviceManager.OnDeviceInfoUpdateListener() {
        @Override
        public void onDeviceInfoUpdate(List<Device> devices) {
            Log.d(TAG, "onDeviceInfoUpdate: ");
        }

        @Override
        public void sseLoginSuccess() {
            Log.d(TAG, "sseLoginSuccess: ");
            sseLatch.countDown();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.sseLoginSuccess();
                    }
                }
            });
        }

        @Override
        public void loginState(final int code, final String info) {//code 0 未连接; 1sse 连接； 2 本地连接； 3 都连接
            Log.d(TAG, "loginState code=" + code + " info=" + info);
            mConnectState = code;
            switch (code) {
                case 0: //未连接到网络
                    break;
                case 1: //only connect sse
                    // can refresh UI
                    /*if (target == null) {
                        Log.d(TAG, "connect from loginState code target=null");
                        try {
//                            Device device = ssChannel.getDeviceManager().getCurrentDevice();//连着设备的时候无法连另一台
                            Device device = getHistoryDevice();
                            if (device != null) {
                                Log.d(TAG, "connect from loginState code= 1");
                                target = ssChannel.getController().connect(device.getLsid(), CONNECT_TIME);
                                mDevice = device;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }*/
                    break;
                case 2: //only connect local
                    break;
                case 3: //both connect sse and local
                    break;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.loginState(code, info);
                    }
                }
            });
        }

        @Override
        public void loginConnectingState(final int code, final String info) {
            Log.d(TAG, "loginConnectingState: " + code + "  info: " + info);
            mConnectingState = code;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (ConnectCallback callback : connectCallbacks) {
                        callback.loginConnectingState(code, info);
                    }
                }
            });
        }

    };

    BleClientManager.ScanCallBack scanCallBack = new BleClientManager.ScanCallBack() {
        @Override
        public void onUpdateDevices(List<Device> list) {
            for (Device device : list) {
                Log.d(TAG, "onUpdateDevices: mDevice" + mDevice);
                Log.d(TAG, "onUpdateDevices: device = " + device.getLsid() + " status = " + device.getBleStatus());
                if (null != mDevice
                        && mDevice.getLsid().equals(device.getLsid())
                        && device.getBleStatus() == 0) {
                    Log.d(TAG, device.getLsid() + "离开围栏，断开连接");
                    disconnect();
                }
            }
        }

        @Override
        public void onStateChange(BluetoothClientCallback.DeviceState res) {

        }
    };
    /*本地listener end*/
}
