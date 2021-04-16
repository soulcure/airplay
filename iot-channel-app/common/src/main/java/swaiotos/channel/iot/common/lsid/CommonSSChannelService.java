package swaiotos.channel.iot.common.lsid;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import swaiotos.channel.iot.common.http.Inject;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.common.utils.PublicParametersUtils;
import swaiotos.channel.iot.ss.SSChannelService;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.manager.lsid.LSIDManager;
import swaiotos.channel.iot.utils.NetUtils;

public abstract class CommonSSChannelService<T extends DeviceInfo> extends SSChannelService {
    private static final String TAG = CommonSSChannelService.class.getSimpleName();
    private Manager mManager;

    public abstract T getTYPEDeviceInfo(Context context);

    private  class Manager extends SSChannelServiceManager<T> {

        private LSIDInfoManager mLSIDInfoManager;
        private ILSIDManagerService mLSIDManagerService;

        @Override
        public void performCreate(Context context) {
            Intent intent = new Intent(Constants.COOCAA_LSID_ACTION);
            intent.setPackage(context.getPackageName());
            NetUtils.NetworkReceiver.register(getApplicationContext(),mNetworkReceiver);
            //判断激活id是否为空
            if (TextUtils.isEmpty(PublicParametersUtils.getcUDID(getApplicationContext()))) {
                Log.d(TAG,"-----PublicParametersUtils.getcUDID(getApplicationContext())-----");
                //为空注册广播
                IntentFilter filter = new IntentFilter();
                filter.addAction(Constants.COOCAA_CUDID_ACTION);

                CUDIDReceiver cudidReceiver = new CUDIDReceiver();
                context.registerReceiver(cudidReceiver, filter, "swaiot.permission.RECEIVE_ACTIVATION_ID", null);
            }

            //兼容TV端第一次刷机以后出现激活id生成与通道自动启动时序问题
            String type = swaiotos.channel.iot.ss.server.utils.Constants.getIOTChannel(context);
            if (!TextUtils.isEmpty(type) && type.equals("TV")) {
                while (TextUtils.isEmpty(PublicParametersUtils.getcUDID(getApplicationContext()))) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG,"type:"+type +" ---:");
                }
                Log.d(TAG,"type:"+type +" ---:"+PublicParametersUtils.getcUDID(getApplicationContext()));
            }

            final AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            context.bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mLSIDManagerService = ILSIDManagerService.Stub.asInterface(service);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mLSIDInfoManager = new LSIDInfoManager(getApplicationContext(),mLSIDManagerService);
                            mLSIDInfoManager.queryLSID();
                            atomicBoolean.compareAndSet(true,false);
                        }
                    }).start();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            }, Context.BIND_AUTO_CREATE);

            while (atomicBoolean.get()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public T getDeviceInfo(Context context) {
            return getTYPEDeviceInfo(context);
        }


        @Override
        public boolean performClientVerify(Context context, ComponentName cn, String id, String key) {
            return true;
        }

        @Override
        public LSIDManager getLSIDManager() {
            return mLSIDInfoManager;
        }

        @Override
        public void onSSChannelServiceStarted(Context context) {
            Log.d(TAG, "onSSChannelServiceStarted");
        }

        @Override
        public Intent getClientServiceIntent(Context context) {
            return new Intent("swaiotos.intent.action.channel.iot.SSCLIENT");
        }

        private NetUtils.NetworkReceiver mNetworkReceiver = new NetUtils.NetworkReceiver() {
            @Override
            public void onConnected() {
                if (mLSIDInfoManager != null) {
                    mLSIDInfoManager.checkLSID();
                }
            }

            @Override
            public void onDisconnected() {
            }
        };
        public class CUDIDReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG,"----CUDIDReceiver-intent-----");
                try {
                    if (intent != null && intent.getAction().equals(Constants.COOCAA_CUDID_ACTION)) {
                        Inject.getGoLiveRestApi(getApplicationContext()).reflush();
                        if (mLSIDInfoManager != null) {
                            mLSIDInfoManager.checkLSID();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    protected synchronized SSChannelServiceManager getManager() {
        if (mManager == null) {
            mManager = new Manager();
        }
        return mManager;
    }

}
