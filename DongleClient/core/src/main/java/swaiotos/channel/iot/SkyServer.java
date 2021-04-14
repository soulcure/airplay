package swaiotos.channel.iot;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.coocaa.sdk.IReceiveMessage;
import com.coocaa.sdk.entity.IMMessage;
import com.coocaa.sdk.entity.Session;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import swaiotos.channel.iot.aidl.BinderPoolImpl;
import swaiotos.channel.iot.callback.BindResult;
import swaiotos.channel.iot.callback.ConnectStatusListener;
import swaiotos.channel.iot.callback.DeviceCallback;
import swaiotos.channel.iot.callback.LoginCallback;
import swaiotos.channel.iot.callback.NotifyListener;
import swaiotos.channel.iot.callback.ResultListener;
import swaiotos.channel.iot.callback.UnBindResult;
import swaiotos.channel.iot.client.Client;
import swaiotos.channel.iot.client.ClientIDHandleModel;
import swaiotos.channel.iot.client.Clients;
import swaiotos.channel.iot.db.bean.Device;
import swaiotos.channel.iot.db.helper.DeviceHelper;
import swaiotos.channel.iot.im.IMChannel;
import swaiotos.channel.iot.sse.SSEPushModel;
import swaiotos.channel.iot.utils.AppUtils;
import swaiotos.channel.iot.utils.DeviceUtil;
import swaiotos.channel.iot.webserver.WebServer;


public class SkyServer extends Service {

    public static final String TAG = "yao";

    private static final int HANDLER_THREAD_DEFAULT = 1;

    //todo  //for demo test
    public static final String TEST_SID = "547c08a5f84b411fa945ca8f73c426f6";
    public static final String TEST_ACCESS_TOKEN = "2.5f000c6de7ec467d978e28dda282ed09";

    private ProcessHandler mProcessHandler;  //子线程 handler

    private Context mContext;
    private WebServer mWebServer;
    private String mSid;
    private String mAccessToken;
    private HttpApi httpApi;

    private IMChannel imChannel;

    private NetWorkChangeReceiver mNetWorkReceiver;
    private BroadcastReceiver mScreenReceiver;


    private final RemoteCallbackList<IReceiveMessage> mCallBack = new RemoteCallbackList<>();
    private ClientIDHandleModel clientIDHandleModel;


    /**
     * 消息监听器
     */
    private final NotifyListener mNotifyListener = new NotifyListener() {
        @Override
        public void OnRec(String targetClient, IMMessage msg) {
            boolean res = callback(targetClient, msg);
            if (!res) { //未回调动态监听
                String clientID = msg.getClientTarget();
                Map<String, Client> clients = Clients.getInstance().getClients();
                Client client = clients.get(clientID);
                if (client != null) {
                    //派发消息到应用
                    Intent intent = new Intent();
                    intent.setComponent(client.cn);
                    intent.putExtra("message", msg);
                    mContext.startService(intent);
                    Log.d(TAG, "startService" + " send " + msg + " to " + client);
                }
            }
        }
    };


    /**
     * wifi监测广播.
     */
    private class NetWorkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (imChannel != null) {
                    if (AppUtils.isNetworkConnected(context)) {
                        imChannel.reConnectSSE();
                    }
                    if (AppUtils.isWifi(context)) {
                        imChannel.reConnectLocal();

                        String ip = DeviceUtil.getLocalIPAddress(context);
                        imChannel.updateMySession(ip);
                    }
                }
            }
        }
    }


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

                if (!imChannel.isLocalConnect()) {
                    imChannel.reConnectLocal();
                }

                if (!imChannel.isSSEConnected()) {
                    imChannel.reConnectSSE();
                }
            }
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onBind action=" + action);
        if (action != null) {
            if (action.equals("com.coocaa.service.client.AIDL.BIND")) {
                return new BinderPoolImpl(this);
            } else {
                return new SkyServiceBinder(this);
            }
        }

        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        initHandler();
        initAccount();

        post(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }


    private void init() {
        IntentFilter netFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetWorkReceiver = new NetWorkChangeReceiver();
        registerReceiver(mNetWorkReceiver, netFilter);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenReceiver = new ScreenReceiver();
        registerReceiver(mScreenReceiver, filter);

        mWebServer = new WebServer(this);
        mWebServer.open();

        imChannel = new IMChannel(this);
        httpApi = new HttpApi(this);

        if (!TextUtils.isEmpty(mSid)) {
            SSEPushModel.LoginCallback callback = new SSEPushModel.LoginCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "SkyServer loginSSE onSuccess...");
                }

                @Override
                public void onFail() {
                    Log.e(TAG, "SkyServer loginSSE onFail...");
                }
            };
            imChannel.loginSSE(mSid, callback);
            httpApi.reqBindDevice(null);
        }


        clientIDHandleModel = new ClientIDHandleModel(mContext);
        clientIDHandleModel.registerAppStatusReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mNetWorkReceiver);
        unregisterReceiver(mScreenReceiver);

        clientIDHandleModel.unRegisterAppStatusReceiver();
    }

    public void setAccessToken(String accessToken) {
        mAccessToken = accessToken;
        AppUtils.setStringSharedPreferences(mContext, "sp_accessToken", accessToken);
    }


    public String getAccessToken() {
        if (TextUtils.isEmpty(mAccessToken)) {
            mAccessToken = AppUtils.getStringSharedPreferences(this, "sp_accessToken", "");
        }
        return mAccessToken;
    }


    public void setSid(String sid) {
        mSid = sid;
        AppUtils.setStringSharedPreferences(mContext, "sp_sid", sid);
    }


    public String getSid() {
        if (TextUtils.isEmpty(mSid)) {
            mSid = AppUtils.getStringSharedPreferences(this, "sp_sid", "");
        }
        return mSid;
    }

    public String fileServer(String path) {
        File file = new File(path);
        if (file.exists()) {
            return mWebServer.uploadFile(file);
        }

        Log.e(TAG, "fileServer file path not exists");
        return null;

    }

    public void loginOut() {
        imChannel.close(mSid);
        setAccessToken("");
        setSid("");
    }


    public void sendMessage(IMMessage msg, ResultListener listener) {
        imChannel.sendMessage(msg, listener);
    }

    public NotifyListener getNotifyDefaultListener() {
        return mNotifyListener;
    }


    public void addNotifyListener(String targetClient, NotifyListener listener) {
        imChannel.addNotifyListener(targetClient, listener);
    }

    public void removeNotifyListener(String targetClient) {
        imChannel.removeNotifyListener(targetClient);
    }

    public void setConnectListener(ConnectStatusListener listener) {
        imChannel.setConnectListener(listener);
        httpApi.setConnectListener(listener);
    }

    public Session getMySession() {
        return imChannel.getMySession();
    }

    public Session getTargetSession() {
        return imChannel.getTargetSession();
    }


    public Session connect(String sid, long timeout) throws TimeoutException {
        return imChannel.connect(sid, timeout);
    }

    public Device getCurrentDevice() {
        Session targetSession = imChannel.getTargetSession();
        if (targetSession != null) {
            String targetSid = targetSession.getId();
            List<Device> list = DeviceHelper.instance().toQueryDeviceList(mContext);
            if (list != null && list.size() > 0) {
                for (Device item : list) {
                    if (item.getZpLsid().equals(targetSid)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    public void post(Runnable r) {
        mProcessHandler.post(r);
    }

    public void postDelayed(Runnable r, long delay) {
        mProcessHandler.postDelayed(r, delay);
    }


    public void loginSSE(final String sid, final String token, final LoginCallback callback) {
        imChannel.loginSSE(sid, new SSEPushModel.LoginCallback() {
            @Override
            public void onSuccess() {
                setAccessToken(token);
                setSid(sid);
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onFail() {
                setAccessToken("");
                setSid("");
                imChannel.setSid("");

                if (callback != null) {
                    callback.onFail();
                }
            }
        });
    }


    public boolean isSSEConnected() {
        return imChannel.isSSEConnected();
    }

    public boolean isLocalConnect() {
        return imChannel.isLocalConnect();
    }


    public void sendBroadCastByHttp(IMMessage msg, ResultListener listener) {
        httpApi.sendBroadCastByHttp(msg, listener);
    }

    public void bindDevice(String bindCode, BindResult callback) {
        httpApi.bindDevice(bindCode, callback);
    }

    public void reqBindDevice(final DeviceCallback callback) {
        httpApi.reqBindDevice(callback);
    }


    public void unbindDevice(String targetSid, int deleteType, UnBindResult callback) {
        httpApi.unbindDevice(targetSid, deleteType, callback);
    }


    public void refreshOnlineStatus(final DeviceCallback callback) {
        mProcessHandler.post(new Runnable() {
            @Override
            public void run() {
                httpApi.refreshOnlineStatus(callback);
            }
        });
    }


    public void registerCallback(String targetClient, IReceiveMessage cb) throws RemoteException {
        if (TextUtils.isEmpty(targetClient) || cb == null) {
            return;
        }
        mCallBack.register(cb, targetClient);
    }

    public void unregisterCallback(String targetClient, IReceiveMessage cb) throws RemoteException {
        if (TextUtils.isEmpty(targetClient) || cb == null) {
            return;
        }
        mCallBack.unregister(cb);
    }


    //-----------private method-----------------//

    private boolean callback(String targetClient, IMMessage msg) {
        Log.d(TAG, "接收消息动态回调callback targetClient=" + targetClient + " IMMessage=" + msg);
        boolean result = false;
        final int n = mCallBack.beginBroadcast();
        for (int i = 0; i < n; i++) {
            IReceiveMessage callback = mCallBack.getBroadcastItem(i);
            String key = (String) mCallBack.getBroadcastCookie(i);
            if (!TextUtils.isEmpty(key) && key.equals(targetClient)) {
                try {
                    if (callback != null) {
                        callback.OnRec(targetClient, msg);
                        result = true;
                        break;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mCallBack.finishBroadcast();

        return result;
    }


    private void initAccount() {
        mSid = AppUtils.getStringSharedPreferences(this, "sp_sid", TEST_SID);
        mAccessToken = AppUtils.getStringSharedPreferences(this, "sp_accessToken", TEST_ACCESS_TOKEN);
    }


    /**
     * 线程初始化
     */
    private void initHandler() {
        if (mProcessHandler == null) {
            HandlerThread handlerThread = new HandlerThread("SkyServer-Thread");
            handlerThread.start();
            mProcessHandler = new ProcessHandler(handlerThread.getLooper());
        }
    }


    /**
     * 子线程handler,looper
     *
     * @author Administrator
     */
    private static class ProcessHandler extends Handler {

        public ProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_THREAD_DEFAULT:
                    //do something
                    break;
                default:
                    break;
            }

        }

    }
}