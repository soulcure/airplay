package swaiotos.channel.iot.ss;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;

import swaiotos.channel.iot.ss.analysis.UserBehaviorAnalysis;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.manager.SmartScreenManager;
import swaiotos.channel.iot.ss.server.utils.Constants;

/**
 * The type Ss channel service.
 */
public abstract class SSChannelService<T extends DeviceInfo> extends Service {
    protected static abstract class SSChannelServiceManager<T extends DeviceInfo> extends SmartScreenManager {
        /**
         * SSChannelService实例创建完成后会调用
         *
         * @param context the context
         */
        public abstract void onSSChannelServiceStarted(Context context);
    }


    private static Context context;

    public static final Context getContext() {
        return context;
    }


    private static SSChannelServiceImpl impl;

    @Override
    public final void onCreate() {
        super.onCreate();

        if (new File("/vendor/TianciVersion").exists()//for tv
                || Constants.getIOTChannel(this).equals("PAD")) { //for pad
            KeepAliveHelperService.keep(this);
        }
        SSChannelService.context = getApplicationContext();
        UserBehaviorAnalysis.init();
        SSChannelServiceManager manager = getManager();
        synchronized (SSChannelService.class) {
            if (impl == null) {
                impl = new SSChannelServiceImpl(getContext());
            }
        }
        impl.start(manager);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        impl.close();
        UserBehaviorAnalysis.unInit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return impl.getMainStub();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SS", "onStartCommand:" + startId);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Gets manager.
     *
     * @return the manager
     */
    protected abstract SSChannelServiceManager<T> getManager();
}
