package swaiotos.channel.iot.tv;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Process;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.statemanager.StateManager;
import com.coocaa.statemanager.view.UiUtil;
import com.google.gson.Gson;
import com.skyworth.framework.skysdk.ipc.SkyApplication;
import com.swaiotos.universalmediaplayer.datasubmit.DataSubmit;
import com.swaiotos.universalmediaplayer.datasubmit.IDataSubmit;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.client.event.BindCodeEvent;
import swaiotos.channel.iot.ss.client.event.StartClientEvent;
import swaiotos.channel.iot.ss.server.data.log.ReportData;
import swaiotos.channel.iot.ss.server.data.log.ReportDataUtils;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.tv.base.DeviceChangeListener;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.ThreadManager;
import swaiotos.sal.SAL;
import swaiotos.sal.SalModule;
import swaiotos.sal.system.ISystem;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv
 * @ClassName: AiotApplication
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/9 16:56
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/9 16:56
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TVChannelApplication extends MultiDexApplication {

    private Thread.UncaughtExceptionHandler defaultHandler;

    private static Context mContext;
    private List<DeviceChangeListener> listeners = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });

    }

    private void init() {
        mContext = this;
        SkyApplication.init(this);//for SAL

        String cur_process = getProcessName(this, android.os.Process.myPid());
        Log.e("yao", "onCreate cur_process :" + cur_process);

        if (!TextUtils.isEmpty(cur_process)) {
            if (cur_process.equals("swaiotos.channel.iot:core")) {
                if (!EventBus.getDefault().isRegistered(this)) {
                    EventBus.getDefault().register(this);
                }
                UiUtil.init(this);
                StateManager.INSTANCE.init(this);

                String action = "swaiotos.intent.action.channel.iot.service.SS";
                ISystem iSystem = SAL.getModule(this, SalModule.SYSTEM);
                iSystem.registerKeepAliveServiceByAction(getPackageName(), action, true);

            } else if (cur_process.equals("swaiotos.channel.iot")) {
                ZXingLibrary.initDisplayOpinion(this);

                if (Constants.isDangle()) {
                    AndroidLog.androidLog("---------UMConfigure init---------");
                    UMConfigure.setLogEnabled(true);
                    UMConfigure.init(this, "6073e6bbde41b946ab47360b", "dongle", UMConfigure.DEVICE_TYPE_BOX, null);
                    MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
                }
            } else if (cur_process.equals("swaiotos.channel.iot:uplayer")) {
                initUniversalMediaPlayer();
            }
        }

    }


    private void initUniversalMediaPlayer() {
        DataSubmit.setImpl(new IDataSubmit() {
            @Override
            public void submit(final String eventName, final Map<String, String> data) {
                ReportData.PayLoadData<Map<String, String>> pData = new ReportData.PayLoadData<>();
                ReportData.EventData<Map<String, String>> eData = new ReportData.EventData<>();
                eData.data = data;
                eData.eventName = eventName;
                eData.eventTime = System.currentTimeMillis();
                pData.events = new ArrayList<>();
                pData.events.add(eData);
                ReportData reportData = ReportDataUtils.getReportData(mContext, "dongle.reserved_events", pData);
                Log.d("log", "submitAppCast reportData:" + new Gson().toJson(reportData));
                Intent intent = new Intent("coocaa.intent.action.IotlogSubmit");
                intent.setPackage(getPackageName());
                intent.putExtra("logdata", reportData);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mContext.startForegroundService(intent);
                } else {
                    mContext.startService(intent);
                }
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(t, e);
                }
                Process.killProcess(Process.myPid());
                Log.e("yao", "setDefaultUncaughtExceptionHandler...");
            }
        });
    }

    public List<DeviceChangeListener> getListeners() {
        return listeners;
    }

    public static Context getContext() {
        return mContext;
    }

    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEvent(final StartClientEvent event) {
        try {
            String clientId = event.clientID;
            String pkgName = event.pkgName;
            String message = event.message;
            Log.d("state", "StartClientEvent clientId:" + clientId + " pkgName:" + pkgName);
            Log.d("state", "StartClientEvent message:" + message);
            if (!TextUtils.isEmpty(clientId) && !TextUtils.isEmpty(pkgName)) {
                StateManager.INSTANCE.startClient(clientId, pkgName, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEvent(BindCodeEvent event) {
        String bindCode = event.bindCode;
        String screenQR = event.tempUrl;
        Log.d("state", "BindCodeEvent bindCode:" + bindCode + " screenQR:" + screenQR);
        StateManager.INSTANCE.updateScreenCode(bindCode, screenQR);
    }
}
