package com.coocaa.tvpi;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.clj.fastble.BleManager;
import com.coocaa.publib.PublibHelper;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartmall.data.mobile.http.MobileRequestService;
import com.coocaa.smartmall.data.tv.data.SmartMallRequestConfig;
import com.coocaa.smartscreen.BuildConfig;
import com.coocaa.smartscreen.R;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.constant.BuildInfo;
import com.coocaa.smartscreen.constant.BusinessInfo;
import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.smartscreen.repository.utils.SmartScreenKit;
import com.coocaa.smartsdk.pay.PayManager;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.local.document.DocumentBrowser;
import com.coocaa.tvpi.module.local.document.DocumentResManager;
import com.coocaa.tvpi.module.local.utils.LocalMediaHelper;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.onlineservice.OnlineServiceHelp;
import com.coocaa.tvpi.module.onlineservice.QiyuImageLoader;
import com.coocaa.tvpi.module.pay.PayPresenter;
import com.coocaa.tvpi.module.remote.RemoteVirtualInputManager;
import com.coocaa.tvpi.module.runtime.AppRuntime;
import com.coocaa.tvpi.util.AppProcessUtil;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.example.sanyansdk.SanYanManager;
import com.qiyukf.nimlib.sdk.StatusBarNotificationConfig;
import com.qiyukf.unicorn.api.Unicorn;
import com.qiyukf.unicorn.api.YSFOptions;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

/**
 * @ClassName MyApplication
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020-03-15
 * @Version TODO (write something)
 */
public class MyApplication extends Application implements ViewModelStoreOwner {
    private static final String TAG = "MyApplication";
    private static MyApplication sApp = null;
    private static final String APP_KEY = "6641645939cd49643b71e42c2298cdbb";
    private static final String APP_TOKEN = "5619397646c8b3f43e82f61486e22bde";

    public static final String APPID_WECHAT = "wx51bbf061ea14abb0";
    public static final String APPKEY_WECHAT = "e77668ad6d5b98396a83fbba78045df7";
    public static final String APPID_QQ = "1109849345";
    public static final String APPKEY_QQ = "mtWaIO1DuboTwxKI";
    public static final String APPID_ALIPAY = "2021002114625304";//支付宝

    private ViewModelStore mAppViewModelStore;

    /*
     * 友盟设置
     * */ {
        PlatformConfig.setWeixin(APPID_WECHAT, APPKEY_WECHAT);
        PlatformConfig.setQQZone(APPID_QQ, APPKEY_QQ);
    }

    @Override
    public void onCreate() {
        SmartScreenKit.setContext(this);
        UserInfoCenter.getInstance().init(this);

        super.onCreate();
        sApp = this;

        PublibHelper.init(this);
        SanYanManager.getInstance().init(getApplicationContext());

        //old appkey 5bd13984b465f5812800014c
        /**
         * 注意: 即使您已经在AndroidManifest.xml中配置过appkey和channel值，也需要在App代码中调
         * 用初始化接口（如需要使用AndroidManifest.xml中配置好的appkey和channel值，
         * UMConfigure.init调用中appkey和channel参数请置为null）。
         * UMConfigure.init(Context context, String appkey, String channel, int deviceType, String pushSecret);
         */
//        UMConfigure.init(getApplicationContext(), "5f83c98b80455950e4a75ef6", "umeng", UMConfigure.DEVICE_TYPE_PHONE, null);
        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, null);
        LogSubmit.init(this);
        PlatformConfig.setWeixin(APPID_WECHAT, APPKEY_WECHAT);
        //只有release包上传crash数据
        MobclickAgent.setCatchUncaughtExceptions(!BuildConfig.DEBUG);

        //网易七鱼，在线客服初始化
        OnlineServiceHelp.getInstance().init(this);

        String processName = getProcessName(this, Process.myPid());
        Log.d(TAG, "onCreate: processName: " + processName);
        boolean defaultProcess = true;
        if (processName != null) {
            defaultProcess = processName.equals(getApplicationContext().getPackageName());
            //判断当前的进程是我们自己app
            if (defaultProcess) {
                //当前应用的初始化
//                checkOverLayPermission();

                //ss init
                SSConnectManager.getInstance().init(this);
                //文档浏览器加载内核初始化
                HomeIOThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        DocumentBrowser.init(getApplicationContext(), "MyApplication");
                    }
                });

                BleManager.getInstance().init(this);
                BleManager.getInstance()
                        .enableLog(true)
                        .setReConnectCount(1, 5000)
                        .setSplitWriteNum(20)
                        .setConnectOverTime(10000)
                        .setOperateTimeout(5000);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    //修复一个crash Using WebView from more than one process at once with the same data directory is not supported
                    try {
                        WebView.setDataDirectorySuffix(processName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        final boolean isDefaultProcess = defaultProcess;

        //涉及到需要获取公共参数的http请求，需要在这个初始化方法之后
        initBuildInfo();
        PermissionsUtil.init(MyApplication.this);
        MobileRequestService.init(MyApplication.this);
        RemoteVirtualInputManager.INSTANCE.init(MyApplication.this, isDefaultProcess);

        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                mAppViewModelStore = new ViewModelStore();

                //商城网络请求设置
                SmartMallRequestConfig.setDebugMode(false);

                AppRuntime.init(MyApplication.this);

                ProcessLifecycleOwner.get().getLifecycle().addObserver(new ApplicationObserver());
            }
        });

        PayManager.setiPay(PayPresenter.getInstance());

        if (isDefaultProcess) {
            //初始化教程视频资源
            DocumentResManager.getInstance().init(this);
        }
        Log.d(TAG, "flavor name = " + getChannelName(this));
    }

    public static Context getContext() {
        return sApp;
    }

    public String getProcessName(Context cxt, int pid) {
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

    private void checkOverLayPermission() {
        //权限判断：VIVO手机放在MainActivity里判断提示语出不来，小米就可以
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                Log.d(TAG, "checkOverLayPermission: ....");
                ToastUtils.getInstance().showGlobalLong(getResources().getString(R.string.app_name) +
                        " 需要权限以使用语音悬浮窗");
            }
        }
    }


    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        return mAppViewModelStore;
    }

    private void initBuildInfo() {
         SmartConstans.setAppContext(this);
        SmartConstans.setBuildInfo(BuildInfo.BuildInfoBuilder.builder()
                .setVersionCode(BuildConfig.VERSION_CODE).setVersionname(BuildConfig.VERSION_NAME)
                .setBuildTimestamp(BuildConfig.BUILD_TIMESTAMP).setBuildDate(BuildConfig.BUILD_DATE)
                .setBuildChannel(BuildConfig.BUILD_CHANNEL)
                .setDebugMode(BuildConfig.DEBUG)
                .setPublishMode(BuildConfig.PUBLISH_MODE)
                .build());
        SmartConstans.setBusinessInfo(BusinessInfo.BusinessInfoBuilder.builder()
                .setWeChatId(APPID_WECHAT)
                .setQQId(APPID_QQ)
                .setAliId(APPID_ALIPAY)
                .setAppKey("81dbba5e74da4fcd8e42fe70f68295a6")
                .setSecret("50c08407916141aa878e65564321af5f")
                .build());
    }

    private static class ApplicationObserver implements LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        private void onAppBackground() {
            AppProcessUtil.isAppBackground = true;
            Log.w(TAG, "[LifecycleChecker]: app moved to background");
            //LocalMediaHelper.getInstance().clear();
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        private void onAppForeground() {
            AppProcessUtil.isAppBackground = false;
            preLoadAlbumData();
            Log.w(TAG, "[LifecycleChecker]: app moved to foreground");
        }

    }

    //启动与回到前台就开始提前缓存照片数据
    private static void preLoadAlbumData() {
        PermissionsUtil.getInstance().requestPermission(getContext(), new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                LocalMediaHelper.getInstance().getReLocalAlbumData(getContext());
            }

            @Override
            public void permissionDenied(String[] permission) {

            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    // 获取渠道工具函数
    public static String getChannelName(Context ctx) {
        if (ctx == null) {
            return null;
        }
        String channelName = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                //注意此处为ApplicationInfo 而不是 ActivityInfo,因为友盟设置的meta-data是在application标签中，而不是activity标签中，所以用ApplicationInfo
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        channelName = applicationInfo.metaData.get("UMENG_CHANNEL") + "";
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(channelName)) {
            channelName = "Unknown";
        }
        return channelName;
    }
}
