package swaiotos.channel.iot.common;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.skyworth.dpclientsdk.MACUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.ccenter.CCenterMangerImpl;
import swaiotos.channel.iot.common.entity.QrCode;
import swaiotos.channel.iot.common.http.exception.UniteThrowable;
import swaiotos.channel.iot.common.usecase.QRCodeUseCase;
import swaiotos.channel.iot.common.usecase.TempCodeUseCase;
import swaiotos.channel.iot.common.utils.BindCodeUtil;
import swaiotos.channel.iot.common.utils.NetChangeUtils;
import swaiotos.channel.iot.common.utils.PublicParametersUtils;
import swaiotos.channel.iot.common.utils.TYPE;
import swaiotos.channel.iot.ss.SSAdminChannel;
import swaiotos.channel.iot.ss.client.event.BindCodeEvent;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.NetUtils;
import swaiotos.channel.iot.utils.SpaceAccountManager;
import swaiotos.channel.iot.utils.ThreadManager;
import swaiotos.channel.iot.utils.WifiAccount;

public class SendLetterServiceImpl extends Service {

    private static final int TYPE_TEMP_CONNECTION = 1; //临时连接
    private static final int TYPE_MODE_NET = 3;//配网模式
    private static final int TYPE_MODE_TEMP = 2;//临时连接
    private static final int TYPE_MODE_BIND = 1;//绑定连接

    private RemoteCallbackList<BindCodeCallback> mCbs = new RemoteCallbackList<>();
    private Map<Integer, RemoteCallbackList<TypeInfoCallback>> mCallbacks = new HashMap<>();
    private MyBinder myBinder;
    private WifiBroadcastReceiver mWifiBroadcastReceiver;
    private boolean isOpenSuccess = false;
    private ScheduledExecutorService mBindExecutorService;
    private ScheduledExecutorService mTimeRefreshExecutorService;
//    private IntentFilter intentFilter;
//    private NetworkChangeReceiver networkChangeReceiver;
    private int mTryCount = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        myBinder = new MyBinder();
        return myBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 注意notification也要适配Android 8 哦
            try {
                startForeground(android.os.Process.myPid(), getNotification(this));// 通知栏标识符 前台进程对象唯一ID
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mTryCount = 0;
        AndroidLog.androidLog("---SendLetterServiceImpl onCreate:" + Thread.currentThread().getId());
        isOpenSuccess = false;
        IOTAdminChannel.mananger.open(getApplicationContext(), getPackageName(), new IOTAdminChannel.OpenCallback() {
            @Override
            public void onConntected(SSAdminChannel channel) {
                AndroidLog.androidLog("SendLetterServiceImpl bind success!");
                isOpenSuccess = true;
                //绑定成功以后：轮询接口
                if (mTimeRefreshExecutorService == null) {
                    mTimeRefreshExecutorService = Executors.newScheduledThreadPool(1);
                    mTimeRefreshExecutorService.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            AndroidLog.androidLog("---SendLetterServiceImpl-:" + Thread.currentThread().getId());
                            onRefreshTempCode();
                        }
                    },1,60*60, TimeUnit.SECONDS);
                }
            }

            @Override
            public void onError(String s) {

            }
        });

        //网络监听
        //NetUtils.NetworkReceiver.register(getApplicationContext(),mNetworkReceiver);
        //新网络监听
        //register();
        NetChangeUtils.NetworkChangeReceiver.register(getApplicationContext(), mNetworkChangeReceiver);
        //wifi端口监听
        if (mWifiBroadcastReceiver == null)
            mWifiBroadcastReceiver = new WifiBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiBroadcastReceiver, filter);

    }

    private void onRefreshTempCode() {
        AndroidLog.androidLog("--------onRefreshTempCode-");
        //采用Iterator遍历HashMap
        for (Integer integer : mCallbacks.keySet()) {
            if (myBinder != null) {
                try {
                    AndroidLog.androidLog("SendLetterServiceImpl flush page!");
                    //type = 1 临时连接方案
                    myBinder.loadInfo(integer);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class MyBinder extends SendLetterInterface.Stub {

        @Override
        public void registerCallback(final BindCodeCallback bindCodeListener) throws RemoteException {
            AndroidLog.androidLog("registerCallback---");
            mCbs.register(bindCodeListener);
        }

        @Override
        public void unregisterCallback(BindCodeCallback bindCodeListener) throws RemoteException {
            AndroidLog.androidLog("unregisterCallback---");
            mCbs.unregister(bindCodeListener);
        }

        @Override
        public void loadBindCodeStart() throws RemoteException {
            BindCodeUtil.getInstance().startHeartBeat(getApplicationContext(), TYPE.TV, new BindCodeUtil.BindCodeCall() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                @Override
                public void onBindBitmapShow(String bindCode, String url, String expiresIn) throws RemoteException {
                    synchronized (SendLetterServiceImpl.class) {
                        mCbs.beginBroadcast();
                        //遍历所有注册的Listener，逐个调用它们的实现方法，也就是通知所有的注册者
                        for (int i = 0; i < mCbs.getRegisteredCallbackCount(); i++) {
                            BindCodeCallback cb = mCbs.getBroadcastItem(i);
                            cb.show(bindCode);
                        }
                        mCbs.finishBroadcast();
                    }
                }
            });

        }

        @Override
        public void loadBindCodeStop() throws RemoteException {
            BindCodeUtil.getInstance().stopHeartBeat();
        }

        @Override
        public void registerTypeCallback(TypeInfoCallback typeInfoCallback, int type) throws RemoteException {
            AndroidLog.androidLog("registerTypeCallback---");
            if (!mCallbacks.containsKey(type)) {
                AndroidLog.androidLog("registerTypeCallback-not exit--");
                RemoteCallbackList<TypeInfoCallback> callback = new RemoteCallbackList<>();
                callback.register(typeInfoCallback);
                mCallbacks.put(type, callback);
            } else {
                AndroidLog.androidLog("registerTypeCallback-exit--");
                RemoteCallbackList<TypeInfoCallback> callback = mCallbacks.get(type);
                if (callback != null) {
                    AndroidLog.androidLog("registerTypeCallback-exit and register-");
                    callback.register(typeInfoCallback);

                }
            }
        }

        @Override
        public void unregisterTypeCallback(TypeInfoCallback typeInfoCallback, int type) throws RemoteException {
            AndroidLog.androidLog("unregisterTypeCallback--");
            if (mCallbacks.containsKey(type)) {
                AndroidLog.androidLog("unregisterTypeCallback containsKey--");
                RemoteCallbackList<TypeInfoCallback> callback = mCallbacks.get(type);
                if (callback != null) {
                    callback.unregister(typeInfoCallback);
                }
            }
        }

        @Override
        public void loadInfo(final int type) throws RemoteException {
            AndroidLog.androidLog("isDANGLE:" + Constants.isDangle());
            if (mCallbacks.containsKey(type) && mCallbacks.get(type) != null) {
                final RemoteCallbackList<TypeInfoCallback> callbacks = mCallbacks.get(type);
                ThreadManager.getInstance().ioThread(new Runnable() {
                    @Override
                    public void run() {
                        if (NetUtils.isConnected(getApplicationContext()) && swaiotos.channel.iot.common.utils.Constants.outerNetState()) {
                            if (!isOpenSuccess) {
                                return;
                            }
                            if (mBindExecutorService != null && !mBindExecutorService.isShutdown()) {
                                mBindExecutorService.shutdownNow();
                                mBindExecutorService = null;
                            }
                            onTempBind(type, callbacks);
                        } else {
                            try {
                                //遍历所有注册的Listener，逐个调用它们的实现方法，也就是通知所有的注册者
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                    try {
                                        String mac = MACUtils.getMac(getApplicationContext()).replace(":", "");

                                        //dongle获取不到Mac异常情况：当dongle未联网且无法获取到mac时，首页二维码和数字码不显示，直到获取之后再正常显示
                                        if (TextUtils.isEmpty(mac) || mac.equals("020000000000")) {
                                            AndroidLog.androidLog("-----mac:"+mac);
                                            return;
                                        }

                                        try {
                                            //G22需求：是否支持蓝牙，不支持就mac为空
                                            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                            AndroidLog.androidLog("---mBluetoothAdapter---isEnabled:"+mBluetoothAdapter.isEnabled());
                                            if (!mBluetoothAdapter.isEnabled()) {
                                                mac = "";
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        String macUrl = PublicParametersUtils.QRCODE_BASE_URL + "mac=" + mac + "&m=pw";

                                        QrCode qrCode = new QrCode();
                                        qrCode.setShowCode(mac);
                                        qrCode.setQrCode(macUrl);
                                        qrCode.setType(type);
                                        String qrCodeJson = JSONObject.toJSONString(qrCode);
                                        AndroidLog.androidLog("qrCodeJson:" + qrCodeJson);
                                        EventBus.getDefault().postSticky(new BindCodeEvent("",macUrl));

                                        synchronized (SendLetterServiceImpl.class) {
                                            int n = callbacks.beginBroadcast();
                                            for (int i = 0; i < n; i++) {
                                                TypeInfoCallback cb = callbacks.getBroadcastItem(i);
                                                try {
                                                    cb.getTypeInfo(qrCodeJson);
                                                } catch (RemoteException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            callbacks.finishBroadcast();
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

            }
        }

        private void onTempBind(final int type, final RemoteCallbackList<TypeInfoCallback> callbacks) {
            BindCodeUtil.getInstance().getTempBindCode(getApplicationContext(), TYPE.TV, new BindCodeUtil.TokenCodeCall() {
                @Override
                public void getToken(String token) {
                    //有网的临时连接
                    QRCodeUseCase.getInstance(getApplicationContext()).run(new QRCodeUseCase.RequestValues(token, TYPE.TV,
                            true), new QRCodeUseCase.QRCodeCallBackListener() {
                        @Override
                        public void onError(String errType, String msg) {
                            AndroidLog.androidLog("QRCodeUseCase errType:" + errType + " msg:" + msg);
                            if (mTryCount ++ < 2) {
                                //重试机制
                                onTempBind(type,callbacks);
                            }
                        }

                        @Override
                        public void onSuccess(final String bindCode, String url, String expiresIn, String typeLoopTime) {
                            AndroidLog.androidLog("---bindCode:" + bindCode + " url:" + url + " expiresIn:" + expiresIn);

                            mTryCount = 0;
                            if (mBindExecutorService == null) {
                                mBindExecutorService = Executors.newScheduledThreadPool(1);
                                mBindExecutorService.scheduleAtFixedRate(new Runnable() {
                                    @Override
                                    public void run() {
                                        onTempBind(type, callbacks);
                                    }
                                }, Integer.parseInt(expiresIn), Integer.parseInt(expiresIn), TimeUnit.SECONDS);
                            }

                            QrCode qrCode = new QrCode();
                            qrCode.setShowCode(bindCode);

                            //临时码由于太长导致二维码太密，去除wifi信息以及商户信息以及去除mode模式等于2的i情况，bindcode可以区别临时码以及永久码
                            String qrCodeStr = PublicParametersUtils.QRCODE_BASE_URL + "bc=" + bindCode + "&m=sm";
                            AndroidLog.androidLog("TYPE_MODE_TEMP qrCodeStr:" + qrCodeStr);
                            EventBus.getDefault().postSticky(new BindCodeEvent(bindCode,qrCodeStr));

                            qrCode.setQrCode(qrCodeStr);
                            qrCode.setType(type);
                            String qrCodeJson = JSONObject.toJSONString(qrCode);
                            AndroidLog.androidLog("TYPE_MODE_TEMP qrCodeJson:" + qrCodeJson);

                            //遍历所有注册的Listener，逐个调用它们的实现方法，也就是通知所有的注册者
                            synchronized (SendLetterServiceImpl.class) {
                                int n = callbacks.beginBroadcast();
                                for (int i = 0; i < n; i++) {
                                    TypeInfoCallback cb = callbacks.getBroadcastItem(i);
                                    try {
                                        cb.getTypeInfo(qrCodeJson);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                                callbacks.finishBroadcast();
                            }

                        }

                    });
                }

                @Override
                public void getTokenError(String error, String msg) {

                }
            });
        }
    }

    private NetChangeUtils.NetworkChangeReceiver mNetworkChangeReceiver = new NetChangeUtils.NetworkChangeReceiver() {
        @Override
        public void onConnected() {
            if (myBinder != null && isOpenSuccess) {
                try {
                    myBinder.loadInfo(TYPE_TEMP_CONNECTION);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onDisconnected() {
            if (myBinder != null) {
                try {
                    myBinder.loadInfo(TYPE_TEMP_CONNECTION);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            AndroidLog.androidLog("------------:" + MACUtils.getMac(getApplicationContext()));
            if (intent != null && !TextUtils.isEmpty(intent.getAction()) &&
                    WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                //获取当前的wifi状态int类型数据
                int mWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                AndroidLog.androidLog("------------mWifiState:" + mWifiState);
                if (mWifiState == WifiManager.WIFI_STATE_ENABLED) {
                    AndroidLog.androidLog("------------:" + MACUtils.getMac(getApplicationContext()));
                    //已打开
                    if (myBinder != null) {
                        try {
                            myBinder.loadInfo(TYPE_TEMP_CONNECTION);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                if (intent == null ) return;
                Bundle bundle = intent.getBundleExtra(CCenterMangerImpl.CCENTER_INTENT_BUNDLE);
                if (bundle == null) return;
                final Messenger messenger = bundle.getParcelable(CCenterMangerImpl.CCENTER_START_MESSENGER);
                if (messenger == null) return;
                final String urlMapStr = bundle.getString(CCenterMangerImpl.CCENTER_EXTRA_MESSASGE);

                if (NetUtils.isConnected(getApplicationContext())) {
                    if (!isOpenSuccess) {
                        return;
                    }

                    BindCodeUtil.getInstance().getTempBindCode(getApplicationContext(), TYPE.TV, new BindCodeUtil.TokenCodeCall() {
                        @Override
                        public void getToken(String token) {
                            //有网的临时连接
                            QRCodeUseCase.getInstance(getApplicationContext()).run(new QRCodeUseCase.RequestValues(token, TYPE.TV,
                                    true), new QRCodeUseCase.QRCodeCallBackListener() {
                                @Override
                                public void onError(String errType, String msg) {
                                    AndroidLog.androidLog("QRCodeUseCase errType:" + errType + " msg:" + msg);
                                }

                                @Override
                                public void onSuccess(final String bindCode, String url, String expiresIn, String typeLoopTime) {
                                    AndroidLog.androidLog("---bindCode:" + bindCode + " url:" + url + " expiresIn:" + expiresIn);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                        try {
                                            final QrCode qrCode = new QrCode();
                                            qrCode.setShowCode(bindCode);

                                            String qrCodeStr = PublicParametersUtils.QRCODE_BASE_URL + "bc=" + bindCode + "&m=sm";

                                            /*SpaceAccountManager mSpaceAccountManager = new SpaceAccountManager();
                                            String spaceAccount = mSpaceAccountManager.getSpaceAccount(getApplicationContext());
                                            AndroidLog.androidLog("onTempBind---spaceAccount:"+spaceAccount);
                                            String sceneType = null;
                                            if (!TextUtils.isEmpty(spaceAccount)) {
                                                try {
                                                    org.json.JSONObject jsonObject = new org.json.JSONObject(spaceAccount);
                                                    sceneType = jsonObject.getString("scene_type");

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            //场景类型
                                            if (!TextUtils.isEmpty(sceneType)) {
                                                qrCodeStr = qrCodeStr + "&s=" + sceneType;
                                            }*/

                                            if (!TextUtils.isEmpty(urlMapStr)) {
                                                qrCodeStr = qrCodeStr + "&" + urlMapStr;
                                            }

                                            AndroidLog.androidLog("urlMapStr-----------------qrCodeStr:"+qrCodeStr);
                                            //请求断码url
                                            TempCodeUseCase.getInstance(getApplicationContext()).run(new TempCodeUseCase.RequestValues(qrCodeStr), new TempCodeUseCase.TempCodeCallBackListener() {
                                                @Override
                                                public void onError(int errType, String msg) {
                                                    AndroidLog.androidLog("TempCodeUseCase-----------------onError--errType:"+errType + "--msg:"+msg);
                                                }

                                                @Override
                                                public void onSuccess(String data) {
                                                    AndroidLog.androidLog("urlMapStr-----------------data:"+data);
                                                    qrCode.setQrCode(data);

                                                    String qrCodeJson = JSONObject.toJSONString(qrCode);
                                                    if (messenger != null) {

                                                        Message msg = Message.obtain();
                                                        Bundle bundle = new Bundle();
                                                        bundle.putString(CCenterMangerImpl.CCENTER_CODE,qrCodeJson);
                                                        msg.setData(bundle);

                                                        try {
                                                            messenger.send(msg);
                                                        } catch (RemoteException e) {
                                                            e.printStackTrace();
                                                        }

                                                    }
                                                }
                                            });


                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            });
                        }

                        @Override
                        public void getTokenError(String error, String msg) {

                        }
                    });
                }

            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTryCount = 0;
        NetChangeUtils.NetworkChangeReceiver.unregister(getApplicationContext(), mNetworkChangeReceiver);
        if (mWifiBroadcastReceiver != null)
            unregisterReceiver(mWifiBroadcastReceiver);

        if (mBindExecutorService != null && !mBindExecutorService.isShutdown()) {
            mBindExecutorService.shutdownNow();
            mBindExecutorService = null;
        }
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private static Notification getNotification(Service service) {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) service.getSystemService(NOTIFICATION_SERVICE);
            //数字是随便写的“40”，
            nm.createNotificationChannel(new NotificationChannel("23", "App Service", NotificationManager.IMPORTANCE_NONE));
            builder = new Notification.Builder(service, "23");
        } else {
            builder = new Notification.Builder(service);
        }
        builder.setContentTitle("服务运行于前台")
                .setContentText("service被设为前台进程")
                .setTicker("service正在后台运行...")
                .setPriority(Notification.PRIORITY_MAX)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        return notification;
    }
}
