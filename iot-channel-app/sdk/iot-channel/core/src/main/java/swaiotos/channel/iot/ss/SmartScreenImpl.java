package swaiotos.channel.iot.ss;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import swaiotos.channel.iot.ss.analysis.UserBehaviorAnalysis;
import swaiotos.channel.iot.ss.channel.base.local.LocalChannel;
import swaiotos.channel.iot.ss.channel.base.local.LocalChannelImpl;
import swaiotos.channel.iot.ss.channel.base.sse.SSEChannel;
import swaiotos.channel.iot.ss.channel.base.sse.SSEChannelImpl;
import swaiotos.channel.iot.ss.channel.im.IMChannelManager;
import swaiotos.channel.iot.ss.channel.im.IMChannelManagerImpl;
import swaiotos.channel.iot.ss.channel.im.cloud.CloudIMChannel;
import swaiotos.channel.iot.ss.channel.im.local.LocalIMChannel;
import swaiotos.channel.iot.ss.client.ClientManager;
import swaiotos.channel.iot.ss.client.ClientManagerImpl;
import swaiotos.channel.iot.ss.controller.ControllerServer;
import swaiotos.channel.iot.ss.controller.ControllerServerImpl;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.DeviceManagerServer;
import swaiotos.channel.iot.ss.device.DeviceManagerServerImpl;
import swaiotos.channel.iot.ss.device.IConnectResult;
import swaiotos.channel.iot.ss.device.PadDeviceInfo;
import swaiotos.channel.iot.ss.device.PhoneDeviceInfo;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;
import swaiotos.channel.iot.ss.manager.SmartScreenManager;
import swaiotos.channel.iot.ss.manager.lsid.LSIDInfo;
import swaiotos.channel.iot.ss.manager.lsid.LSIDManager;
import swaiotos.channel.iot.ss.server.ServerInterface;
import swaiotos.channel.iot.ss.server.ServerInterfaceImpl;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.ss.server.http.HttpServiceConfig;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.ss.session.SessionManagerServer;
import swaiotos.channel.iot.ss.session.SessionManagerServerImpl;
import swaiotos.channel.iot.ss.webserver.WebServer;
import swaiotos.channel.iot.ss.webserver.WebServerImpl;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.HT;
import swaiotos.channel.iot.utils.ThreadManager;
import swaiotos.sal.SAL;
import swaiotos.sal.SalModule;
import swaiotos.sal.system.ISystem;

/**
 * @ClassName: SmartScreen
 * @Author: lu
 * @CreateDate: 2020/3/17 5:24 PM
 * @Description:
 */
public class SmartScreenImpl implements SmartScreen {
    private static final String TAG = "SSImpl";
    private Context mContext;

    private LocalChannel mLocalChannel;
    private SSEChannel mSseChannel;

    private LocalIMChannel mLocalIMChannel;
    private CloudIMChannel mCloudIMChannel;
    private IMChannelManager mIMChannelManager;

    private ControllerServer mController;

    private WebServer mWebServer;

    private SessionManagerServer mSessionManager;

    private DeviceManagerServer mDeviceManager;


    private ClientManager mClientManager;
    private ServerInterface mServerInterface;
    private SmartScreenManager mSmartScreenManager;

    private TransmitterBroadcastReceiver mTransmitterBroadcastReceiver;

    private HT mHT = new HT("ss-thread", true);

    private BroadcastReceiver mScreenReceiver;

    /**
     * 屏幕on/off监听
     */
    public class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) {
                return;
            }
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // DO WHATEVER YOU NEED TO DO HERE
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                // AND DO WHATEVER YOU NEED TO DO HERE
                ThreadManager.getInstance().ioThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("yao", "action screen on");

                            Session target = getSessionManager().getConnectedSession();
                            mLocalIMChannel.available(target); //此方法自带重连机制

                            //开屏以后刷新设备列表
                            if (mDeviceManager != null) {
                                mDeviceManager.updateLsid(null, 1);
                            }

                            if (!mSseChannel.available()) {
                                mSseChannel.reOpen(mLsid);
                                Log.d("yao", "mSseChannel reconnect");
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        }

    }


    private String mAccessToken;
    private String mLsid;

    public SmartScreenImpl(Context context, SmartScreenManager manager) {
        mContext = context;

        mAccessToken = ShareUtls.getInstance(context).getString("sp_accessToken", "");
        mLsid = ShareUtls.getInstance(context).getString("sp_sid", "");

        mSmartScreenManager = manager;
        createBase();
    }

    private void createIMChannel() {
        mLocalIMChannel = new LocalIMChannel(this, mLocalChannel);
        mCloudIMChannel = new CloudIMChannel(this, mSseChannel);
        mIMChannelManager = new IMChannelManagerImpl(mContext, this, mLocalIMChannel, mCloudIMChannel);
    }

    private void createBase() {
        mClientManager = new ClientManagerImpl(mContext, this);
        mLocalChannel = new LocalChannelImpl(mContext, this);
        mSseChannel = new SSEChannelImpl(mContext, this);
        createIMChannel();
        mController = new ControllerServerImpl(this, mSseChannel, mLocalIMChannel);
        mWebServer = new WebServerImpl(mContext);
        mSessionManager = new SessionManagerServerImpl(this);
        mDeviceManager = new DeviceManagerServerImpl(this);
        mServerInterface = new ServerInterfaceImpl(this);
        HttpServiceConfig.init(this);
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public Session open(Context context, OpenHandler handler) {
        try {
            mSmartScreenManager.onCreate(context);
        } catch (Exception e) {
            e.printStackTrace();
            handler.onFailed(e.getMessage());
            return null;
        }

        getSmartScreenManager().getLSIDManager().addCallback(mLSIDCallback);
        getDeviceManager().updateLsid(null, 1);

        openBaseChannel();
        openIMChannel();
        mTransmitterBroadcastReceiver = new TransmitterBroadcastReceiver(this);

        try {
            mSessionManager.getMySession().setId(getLSID());
        } catch (Exception e) {
            e.printStackTrace();
        }

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenReceiver = new ScreenReceiver();
        mContext.registerReceiver(mScreenReceiver, filter);

        setDeviceType();

        try {
            Session session = mSessionManager.getMySession();
            handler.onOpened(this);
            return session;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ClientManager getClientManager() {
        return mClientManager;
    }

    @Override
    public ControllerServer getController() {
        return mController;
    }

    @Override
    public SessionManagerServer getSessionManager() {
        return mSessionManager;
    }

    @Override
    public WebServer getWebServer() {
        return mWebServer;
    }

    private void setDeviceType() {
        try {

            DeviceInfo deviceInfo = getDeviceInfo();
            if (deviceInfo instanceof TVDeviceInfo) {
                if (Constants.isDangle()) {
                    UserBehaviorAnalysis.deviceType = "dongle";
                } else {
                    UserBehaviorAnalysis.deviceType = "tv";
                }
                UserBehaviorAnalysis.userId = Constants.getActiveId(getContext());
            } else if (deviceInfo instanceof PhoneDeviceInfo) {
                UserBehaviorAnalysis.deviceType = "mobile-android";
                UserBehaviorAnalysis.userId = mSmartScreenManager.getLSIDManager().getLSIDInfo().userId;
            } else if (deviceInfo instanceof PadDeviceInfo) {
                UserBehaviorAnalysis.deviceType = "panel";
                UserBehaviorAnalysis.userId = Constants.getActiveId(getContext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            UserBehaviorAnalysis.deviceType = "mobile-android";
        }
    }

    private void openBaseChannel() {
        try {
            mWebServer.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mSseChannel.open(getLSID());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mLocalChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mController.open();
        mSessionManager.open();
    }

    private LSIDManager.Callback mLSIDCallback = new LSIDManager.Callback() {
        @Override
        public void onLSIDUpdate() {
            Log.d(TAG, "onLSIDUpdate reset session lsid!");
            mSessionManager.updateMyLSID(true);

            Log.d(TAG, "onLSIDUpdate reopen ssechannel!");
            try {
                mSseChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mSseChannel.open(getLSID());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void openIMChannel() {
        mIMChannelManager.open();
    }

    @Override
    public IMChannelManager getIMChannel() {
        return mIMChannelManager;
    }

    @Override
    public int close() {
        try {
            getSmartScreenManager().getLSIDManager().removeCallback(mLSIDCallback);
            closeIMChannel();
            closeBaseChannel();

            mContext.unregisterReceiver(mScreenReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void closeIMChannel() {
        mIMChannelManager.close();
    }

    private void closeBaseChannel() {
        mController.close();
        try {
            mSseChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mLocalChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mWebServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SmartScreenManager getSmartScreenManager() {
        return mSmartScreenManager;
    }

    @Override
    public DeviceManagerServer getDeviceManager() {
        return mDeviceManager;
    }

    @Override
    public String getLSID() {
        if (TextUtils.isEmpty(mLsid) || mLsid.length() != 32) {
            LSIDInfo lsidInfo = mSmartScreenManager.getLSIDManager().getLSIDInfo();
            if (lsidInfo != null) {
                mLsid = lsidInfo.lsid;
                if (!TextUtils.isEmpty(mLsid)) {
                    if (mLsid.length() == 32) {
                        ShareUtls.getInstance(mContext).putString("sp_sid", mLsid);
                    } else {
                        ShareUtls.getInstance(mContext).putString("sp_sid", "");
                    }
                }
            }
        }
        return mLsid;
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return mSmartScreenManager.getDeviceInfo(mContext);
    }

    @Override
    public String getAccessToken() {
        if (TextUtils.isEmpty(mAccessToken)) {
            LSIDInfo lsidInfo = getSmartScreenManager().getLSIDManager().getLSIDInfo();
            if (lsidInfo != null) {
                mAccessToken = lsidInfo.accessToken;
                if (!TextUtils.isEmpty(mAccessToken)) {
                    ShareUtls.getInstance(mContext).putString("sp_accessToken", mAccessToken);
                }
            }
        }
        return mAccessToken;
    }

    @Override
    public String getTempBindCode() {
        if (getSmartScreenManager().getLSIDManager() != null && getSmartScreenManager().getLSIDManager().getLSIDInfo() != null)
            return getSmartScreenManager().getLSIDManager().getLSIDInfo().tempCode;
        return "";
    }

    @Override
    public ServerInterface getServerInterface() {
        return mServerInterface;
    }

    @Override
    public TransmitterCallBack getTransmitter() {
        return mTransmitterBroadcastReceiver;
    }

    @Override
    public void post(Runnable runnable) {
        mHT.post(runnable);
    }

    @Override
    public void postDelay(Runnable runnable, long delay) {
        mHT.postDelay(runnable, delay);
    }

    @Override
    public void removeCallbacks(Runnable runnable) {
        mHT.removeCallbacks(runnable);
    }

    @Override
    public void reset(String sid, String token) {
        Log.d(TAG, "onLSIDUpdate reset session lsid!");
        if (!TextUtils.isEmpty(sid)) {
            mLsid = sid;
            ShareUtls.getInstance(mContext).putString("sp_sid", sid);
        }

        if (!TextUtils.isEmpty(token)) {
            mAccessToken = token;
            ShareUtls.getInstance(mContext).putString("sp_accessToken", token);
        }

        mSmartScreenManager.getLSIDManager().setSid(sid, token);
        mSessionManager.updateMyLSID(true);
        post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onLSIDUpdate reopen ssechannel!");
                try {
                    mSseChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mSseChannel.reOpen(getLSID());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mDeviceManager.updateLsid(null, 1);
                mSessionManager.clearConnectedSession();
                mController.getDeviceStateManager().reflushDeviceStateOfSid();
            }
        });
    }

    @Override
    public void reset(String sid, String token, String userId) {
        Log.d(TAG, "onLSIDUpdate reset session lsid!");
        AndroidLog.androidLog("-------sid:" + sid + " token:" + token + " userId:" + userId);
        UserBehaviorAnalysis.userId = userId;
        if (!TextUtils.isEmpty(sid)) {
            mLsid = sid;
            ShareUtls.getInstance(mContext).putString("sp_sid", sid);
        }

        if (!TextUtils.isEmpty(token)) {
            mAccessToken = token;
            ShareUtls.getInstance(mContext).putString("sp_accessToken", token);
        }

        mSmartScreenManager.getLSIDManager().setSidAndUserId(sid, token, userId);
        mSessionManager.updateMyLSID(true);
        post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onLSIDUpdate reopen ssechannel!");
                try {
                    mSseChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mSseChannel.reOpen(getLSID());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mDeviceManager.updateLsid(null, 1);
                mSessionManager.clearConnectedSession();
                mController.getDeviceStateManager().reflushDeviceStateOfSid();
            }
        });
    }

    @Override
    public void connectSSETest(String lsid, IConnectResult result) {

    }

    @Override
    public void connectLocalTest(String ip, IConnectResult result) {

    }

}
