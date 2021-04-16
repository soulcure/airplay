package com.coocaa.tvpi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.views.SDialog;
import com.coocaa.smartscreen.R;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.connect.callback.BindCallback;
import com.coocaa.smartscreen.connect.callback.ConnectCallbackImpl;
import com.coocaa.smartscreen.data.channel.events.ConnectEvent;
import com.coocaa.smartscreen.data.upgrade.UpgradeData;
import com.coocaa.smartscreen.utils.NetworkUtils;
import com.coocaa.smartscreen.utils.SpUtil;
import com.coocaa.tvpi.adapter.MainAdapter;
import com.coocaa.tvpi.base.mvvm.BaseViewModelActivity;
import com.coocaa.tvpi.broadcast.NetWorkStateReceiver;
import com.coocaa.tvpi.broadcast.WiFiApStateReceiver;
import com.coocaa.tvpi.connect.FastConnectDevice;
import com.coocaa.tvpi.event.ScanFastFinishEvent;
import com.coocaa.tvpi.module.base.Navigatgorable;
import com.coocaa.tvpi.module.connection.NoNetwortDialogActivity;
import com.coocaa.tvpi.module.homepager.UserAgreementDialog;
import com.coocaa.tvpi.module.homepager.main.vy21m4.SmartScreenFragment;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.location.SmartLocationManager;
import com.coocaa.tvpi.module.log.LoginEvent;
import com.coocaa.tvpi.module.login.LoginActivity;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.mine.DiscoverFragment;
import com.coocaa.tvpi.module.upgrade.UpgradeManager;
import com.coocaa.tvpi.module.web.SmartBrowserActivity2;
import com.coocaa.tvpi.module.web.SmartBrowserClipboardDialogActivity;
import com.coocaa.tvpi.util.ClipboardUtil;
import com.coocaa.tvpi.util.PackageUtils;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpi.viewmodel.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.device.Device;

import static com.coocaa.tvpi.common.UMengEventId.MAIN_PAGE_TAB;
import static com.coocaa.tvpi.util.ClipboardUtil.KEY_WORD;

public class MainActivity extends BaseViewModelActivity<MainViewModel> implements Navigatgorable {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String KEY_IS_AGREE_AGREEMENT = "isAgreeAgreement";
    private static final String KEY_HAS_SHOWN_NOTIFICATION = "isFirstStartApp";
    private static final int REQUEST_CODE_NOTIFICATION_SETTING = 1;
    private static final long DAY = 24 * 60 * 60 * 1000;
    private static final String SMART_HOME_PACKAGE_NAME = "com.skyworth.smartsystem.vhome";

    private long exitTime;
    private volatile boolean isScanFastFinish;
    private volatile long scanFastFinishTime;
    private UserAgreementDialog userAgreementDialog;
    private SDialog notificationDialog;
    private SDialog appConflictDialog;
    private final FastConnectDevice fastConnectDevice = new FastConnectDevice(this);

    private NetWorkStateReceiver netWorkStateReceiver;
    private WiFiApStateReceiver wifiApStateReceiver;

    private final Runnable clipboardRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "clipboardRunnable run");
            checkClipboard();
            judgeClipboardWebPage();
        }
    };

    private final Runnable connectHistoryRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "connectHistoryRunnable run");
            if (NetworkUtils.isAvailable(MainActivity.this)) {
                SSConnectManager.getInstance().connectHistory();
                HomeIOThread.execute(10 * 1000, this);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarHelper.translucent(this);
        initView();
        netWorkStateReceiver = new NetWorkStateReceiver();
        wifiApStateReceiver = new WiFiApStateReceiver();
        registerReceiver(netWorkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(wifiApStateReceiver, new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED"));
        EventBus.getDefault().register(this);
        UserInfoCenter.getInstance().registerAccountReceiver();
        SSConnectManager.getInstance().addConnectCallback(connectCallback);

        viewModel.updateUserInfo();
        //不管用户是否登录，都上报startup事件
        LoginEvent.submitLogin("startup");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        fastConnectDevice.setBind(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showDialogs();
        viewModel.getTpToken();
        checkScanFastFinish();
        checkSmartHomeConflict();
        HomeUIThread.execute(500, clipboardRunnable);
        HomeIOThread.execute(1000, connectHistoryRunnable);
        if (getIntent().getData() != null) {
            if (!fastConnectDevice.isBind()) {
                Log.d(TAG, "onResume: " + getIntent().getData().toString());
                fastConnectDevice.setBindCode(getIntent().getData().getQueryParameter("bc"), getIntent().getData().getQueryParameter("m"));
                fastConnectDevice.setSpaceId(getIntent().getData().getQueryParameter("sp"), getIntent().getData().getQueryParameter("m"));
                fastConnectDevice.start();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        HomeUIThread.removeTask(clipboardRunnable);
        HomeIOThread.removeTask(connectHistoryRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fastConnectDevice.stop();
        unregisterReceiver(netWorkStateReceiver);
        unregisterReceiver(wifiApStateReceiver);
        EventBus.getDefault().unregister(this);
        UserInfoCenter.getInstance().unRegisterAccountReceiver();
        SSConnectManager.getInstance().removeConnectCallback(connectCallback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                ToastUtils.getInstance().showGlobalShort("再按一次退出");
                exitTime = System.currentTimeMillis();
            } else {
//                finish();
//                System.exit(0);
                try {
                    Intent home = new Intent(Intent.ACTION_MAIN);
                    home.addCategory(Intent.CATEGORY_HOME);
                    home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(home);
                } catch (Exception e) {
                    e.printStackTrace();
                    finish();
                    System.exit(0);
                }
            }
            return true;
        }

        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ScanFastFinishEvent event) {
        Log.d("SSS", "on ScanFastFinishEvent");
        isScanFastFinish = true;
        scanFastFinishTime = SystemClock.uptimeMillis();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_NOTIFICATION_SETTING) {
            showDialogs();
        }
    }

    private final ConnectCallbackImpl connectCallback = new ConnectCallbackImpl() {
        @Override
        public void onSuccess(ConnectEvent connectEvent) {
            Log.d("SSS", "connectCallback onSuccess: " + connectEvent);
            checkScanFastFinish();
            getLocation();
        }

        @Override
        public void sseLoginSuccess() {
            Log.d(TAG, "sseLoginSuccess: ");
            HomeIOThread.removeTask(connectHistoryRunnable);
            HomeIOThread.execute(connectHistoryRunnable);
        }
    };

    private void initView() {
        Fragment smartScreenFragment = new SmartScreenFragment();
        Fragment mineFragment = new DiscoverFragment();
        List<Fragment> fragmentsList = new ArrayList<>();
        fragmentsList.add(smartScreenFragment);
        fragmentsList.add(mineFragment);
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        MainAdapter mainAdapter = new MainAdapter(getSupportFragmentManager(), getLifecycle(), fragmentsList);
        viewPager.setAdapter(mainAdapter);
        viewPager.setUserInputEnabled(false);
        BottomNavigationView bottomNavigationView = findViewById(R.id.navView);
        bottomNavigationView.setItemIconTintList(null);//除去自带效果
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d(TAG, "onNavigationItemSelected: " + item);
                viewPager.setCurrentItem(item.getOrder(), false);
                submitTabClick(item.getOrder(), String.valueOf(item.getTitle()));
                return true;
            }
        });
    }


    private void showDialogs() {
        if (!showAgreementDialog()) {
            if (!showNotificationDialog()) {
                showUpdateDialog();
            }
        }
    }


    //用户协议弹窗
    private boolean showAgreementDialog() {
        Log.d(TAG, "showAgreementDialog");
        boolean isAgreedAgreement = SpUtil.getBoolean(MainActivity.this, KEY_IS_AGREE_AGREEMENT, false);
        if (!isAgreedAgreement) {
            if (userAgreementDialog == null) {
                userAgreementDialog = new UserAgreementDialog();
                userAgreementDialog.setUserAgreementListener(new UserAgreementDialog.UserAgreementListener() {
                    @Override
                    public void onAgreeClick() {
                        Log.d(TAG, "onAgreeClick");
                        SpUtil.putBoolean(MainActivity.this, KEY_IS_AGREE_AGREEMENT, true);
                        showDialogs();
                        checkClipboard();
                        judgeClipboardWebPage();
                    }
                });
            }
            if (!userAgreementDialog.isAdded()) {
                userAgreementDialog.show(getSupportFragmentManager(), "UserAgreementDialog");
            }
            return true;
        } else {
            return false;
        }
    }

    //通知权限弹窗
    private boolean showNotificationDialog() {
        Log.d(TAG, "showNotificationDialog");
        boolean hasShowNotification = SpUtil.getBoolean(MainActivity.this, KEY_HAS_SHOWN_NOTIFICATION, false);
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        boolean enabled = manager.areNotificationsEnabled();
        if (!hasShowNotification && !enabled) {
            if (notificationDialog == null) {
                notificationDialog = new SDialog(MainActivity.this, "\"共享屏\"想给您发送通知", "\"通知\"可能包括提醒、声音和图标标记。这些可在\"设置\"中配置。",
                        com.coocaa.tvpilib.R.string.not_allowed, com.coocaa.tvpilib.R.string.allowed, new SDialog.SDialog2Listener() {
                    @Override
                    public void onClick(boolean l, View view) {
                        if (!l) {
                            startActivityForResult(createNotificationSettingsIntent(), REQUEST_CODE_NOTIFICATION_SETTING);
                        }
                    }
                });
                notificationDialog.setCanceledOnTouchOutside(false);
                notificationDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        SpUtil.putBoolean(MainActivity.this, KEY_HAS_SHOWN_NOTIFICATION, true);
                    }
                });
            }

            if (!notificationDialog.isShowing()) {
                notificationDialog.show();
            }
            return true;
        } else {
            return false;
        }
    }


    //升级弹窗
    private void showUpdateDialog() {
        Log.d(TAG, "showUpdateDialog");
        UpgradeManager.getInstance().upgradeLatest(new UpgradeManager.UpgradeCallback() {
            @Override
            public void onSuccess(UpgradeData upgradeData) {
                if (null != upgradeData) {
                    Log.d(TAG, "onSuccess: " + upgradeData.toJson());
                    long versionCode = UpgradeManager.getInstance().getAppVersionCode(MainActivity.this);
                    if (versionCode < upgradeData.version_code) {
                        UpgradeManager.getInstance().downloadLatestAPK(true);
                        /*long lastUpdateTime = SpUtil.getLong(MainActivity.this, UpgradeManager.KEY_LAST_SHOW_UPDATE_DIALOG_TIME, 0L);
                        if (System.currentTimeMillis() - lastUpdateTime < 7 * DAY) {
                            Log.d(TAG, "System.currentTimeMillis() - lastUpdateTime < 7 day"
                                    + "\ncurrentTimeMillis:" + System.currentTimeMillis() + "\nlastUpdateTime:" + lastUpdateTime);
                            boolean lastAgreeUpdate = SpUtil.getBoolean(MainActivity.this, UpgradeManager.KEY_LAST_AGREE_UPDATE, false);
                            Log.d(TAG, "lastAgreeUpdate:" + lastAgreeUpdate);
                            if (lastAgreeUpdate) {
                                UpgradeManager.getInstance().downloadLatestAPK(true);
                            }
                        } else {
                            Log.d(TAG, "System.currentTimeMillis() - lastUpdateTime > 7 day"
                                    + "\ncurrentTimeMillis:" + System.currentTimeMillis() + "\nlastUpdateTime:" + lastUpdateTime);
                            UpgradeManager.getInstance().downloadLatestAPK(true);
                        }*/
                    } else {
                        Log.d(TAG, "onSuccess: 没有更新版本" + versionCode);
                    }
                }
            }

            @Override
            public void onFailed(Throwable e) {
                Log.d(TAG, "onFailed: " + e.toString());
            }
        });
    }


    //修复二维码扫描快速推出后，不提示已连接的问题
    private void checkScanFastFinish() {
        if (!isScanFastFinish) return;
//        if (SystemClock.uptimeMillis() - scanFastFinishTime > 3000) {//间隔太久，或者快速进入到其他页面后再返回就不再提示
//            return;
//        }
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (MainActivity.class) {
                    if (isScanFastFinish) {
                        if (SSConnectManager.getInstance().isConnected()) {
                            ToastUtils.getInstance().showGlobalShort("已连接");
                            isScanFastFinish = false;
                        }
                    }
                }
            }
        });
    }

    private void checkSmartHomeConflict() {
        if (PackageUtils.isInstalledApp(this, SMART_HOME_PACKAGE_NAME)
                && PackageUtils.getVersionCode(this, SMART_HOME_PACKAGE_NAME) < 10122
                && !PackageUtils.getAppName(this, SMART_HOME_PACKAGE_NAME).equals("小维智联")) {
            if(appConflictDialog == null) {
                appConflictDialog = new SDialog(this, "当前酷开智家版本与共享屏冲突，可能导致共享屏无法正常使用，请升级最新版酷开智家APP",
                        "去下载", new SDialog.SDialogListener() {
                    @Override
                    public void onOK() {
                        appConflictDialog.dismiss();
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri apk_url = Uri.parse("https://tvpi.coocaa.com/smarthome/index.html");
                        intent.setData(apk_url);
                        startActivity(intent);
                    }
                });
                appConflictDialog.setCanceledOnTouchOutside(false);
                appConflictDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        return keyCode == KeyEvent.KEYCODE_BACK;
                    }
                });
            }
            appConflictDialog.show();
        }
    }

    private void getLocation() {
        if (SpUtil.getBoolean(this, "smart_location_permission_denied", false)) {
            //用户拒绝过权限
            Log.d("SSS", "user has denied location permission before.");
            return;
        }
        Log.d(TAG, "getLocation: ");
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                SpUtil.putBoolean(MainActivity.this, "smart_location_permission_denied", false);
                Log.d(TAG, "permissionGranted: 有获取位置权限");
                SmartLocationManager.INSTANCE.initAndSubmitLocation(MainActivity.this);
            }

            @Override
            public void permissionDenied(String[] permission) {
                SpUtil.putBoolean(MainActivity.this, "smart_location_permission_denied", true);
                Log.d(TAG, "permissionDenied: 未获取位置权限");
            }
        }, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    //构造跳转开启通知权限界面Intent
    private Intent createNotificationSettingsIntent() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= 26) {
            // android 8.0引导
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
        } else if (Build.VERSION.SDK_INT >= 21) {
            // android 5.0-7.0
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", getPackageName());
            intent.putExtra("app_uid", getApplicationInfo().uid);
        } else {
            // 其他
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", getPackageName(), null));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }


    //初始值 0， 去登陆1，
    private int clipBoardStep = 0;

    public void checkClipboard() {
        if (!SpUtil.getBoolean(this, KEY_IS_AGREE_AGREEMENT, false)) {
            Log.d(TAG, "checkClipboard: 用户未同意用户协议，取消弹窗");
            return;
        }

        if (SSConnectManager.getInstance().isConnected()) {
            Log.d(TAG, "checkClipboard: 已经连上设备");
            return;
        }

        String s = ClipboardUtil.getCopy(this);
        Log.d(TAG, "checkClipboard: s = " + s);

        if (!TextUtils.isEmpty(s) && s.contains(KEY_WORD)) {
            if (!UserInfoCenter.getInstance().isLogin()) {
                if (clipBoardStep == 0) {
                    Log.d(TAG, "checkClipboard: 去登录");
                    LoginActivity.start(this);
                    clipBoardStep = 1;
                } else if (clipBoardStep == 1) {
                    Log.d(TAG, "checkClipboard: 从登录回来，清空数据");
                    clipBoardStep = 0;
                    //从登录回来，清除粘贴版
                    ClipboardUtil.copy(this, null);
                }
                return;
            }

            Log.d(TAG, "checkClipboard: " + s);
            s = s.replace("COOC@@", "");

            Map<String, String> map = ClipboardUtil.getURLRequest(s);
            try {
                String timestampStr = map.get("t");
                if (TextUtils.isEmpty(timestampStr) || !ClipboardUtil.isAvaliableTime(Long.parseLong(timestampStr))) {
                    Log.d(TAG, "checkClipboard: 时间非法");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "checkClipboard: 时间非法 = " + e.getMessage());
                return;
            }

            final String NEW_HOST = "https://ccss.tv/";
            if (s.contains(NEW_HOST)) {
                handleNewClipboard(map);
                //识别完成后，清除粘贴版
                ClipboardUtil.copy(this, null);
                return;
            }

            String action = map.get("action");
            if (!TextUtils.isEmpty(action)) {
                if (action.equals("smart_screen")) {
                    String mode = map.get("mode");
                    handleClipboard(mode, map);
                    //识别完成后，清除粘贴版
                    ClipboardUtil.copy(this, null);
                }
            }

        }
    }

    //检测粘贴板有其他url地址，跳转到链接打开
    private void judgeClipboardWebPage() {
        if (!SpUtil.getBoolean(this, KEY_IS_AGREE_AGREEMENT, false)) {
            Log.d(TAG, "judgeClipboardWebPage: 用户未同意用户协议，取消弹窗");
            return;
        }
        String s = ClipboardUtil.getCopy(this);
        Log.d(TAG, "judgeClipboardWebPage, s=" + s);
        if(s == null)
            return ;
        boolean isSmartScreen = s.contains("tvpi") || s.contains("smart_screen") || s.contains("ccss.tv");
        if(!isSmartScreen && s != null && (s.startsWith("http://") || s.startsWith("https://"))) {
            //复制的链接
            if(!SmartBrowserActivity2.isDuplicateUrl(this, s)) {
                SmartBrowserActivity2.setShowDialogUrl(this, s);
                SmartBrowserClipboardDialogActivity.start(MainActivity.this, s);
            }
        }
    }

    private void handleNewClipboard(Map<String, String> map) {
        Log.d(TAG, "handleNewClipboard: " + map);

        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                String m = map.get("m");
                if (!TextUtils.isEmpty(m)) {
                    if (m.equals("sm")) {
                        //已联网，直接绑定
                        String name = "";
                        String mn = map.get("mn");
                        if (!TextUtils.isEmpty(mn)) {
                            name += mn + " ";
                        }
                        String sn = map.get("sn");
                        if (!TextUtils.isEmpty(sn)) {
                            name += sn;
                        }
                        String tips = "";
                        if (!TextUtils.isEmpty(name)) {
                            tips = "发现【" + name + "】共享屏，是否立即连接？";
                        } else {
                            tips = "发现共享屏，是否立即连接？";
                        }
                        if (!TextUtils.isEmpty(map.get("bc"))) {//绑定码
                            new SDialog(MainActivity.this, tips, "暂不连接", "立即连接",
                                    new SDialog.SDialog2Listener() {
                                        @Override
                                        public void onClick(boolean left, View view) {
                                            if (!left) {
                                                handleBind(map.get("bc"));
                                            }
                                        }
                                    }).show();
                        } else if (!TextUtils.isEmpty(map.get("sp"))) {//空间id连接
                            new SDialog(MainActivity.this, tips, "暂不连接", "立即连接",
                                    new SDialog.SDialog2Listener() {
                                        @Override
                                        public void onClick(boolean left, View view) {
                                            if (!left) {
                                                handleTempBind(map.get("sp"), 1);
                                            }
                                        }
                                    }).show();
                        }
                    } else if (m.equals("pw")) {//配网
                        //未联网，需要去配网
                        new SDialog(MainActivity.this, "是否给共享屏配网", "", "立即配网",
                                new SDialog.SDialog2Listener() {
                                    @Override
                                    public void onClick(boolean left, View view) {
                                        if (!left) {
                                            NoNetwortDialogActivity.start(MainActivity.this, map.get("mac"));
                                        }
                                    }
                                }).show();
                    }
                }
            }

            @Override
            public void permissionDenied(String[] permission) {
                ToastUtils.getInstance().showGlobalShort("需要获取位置信息权限才能读取Wi-Fi");
            }
        }, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public void handleClipboard(String mode, Map<String, String> map) {
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                if ("2".equals(mode)) {
                    //已联网，直接绑定
                    String name = "";
                    String mn = map.get("mn");
                    if (!TextUtils.isEmpty(mn)) {
                        name += mn + " ";
                    }
                    String sn = map.get("sn");
                    if (!TextUtils.isEmpty(sn)) {
                        name += sn;
                    }
                    String tips = "";
                    if (!TextUtils.isEmpty(name)) {
                        tips = "发现【" + name + "】共享屏，是否立即连接？";
                    } else {
                        tips = "发现共享屏，是否立即连接？";
                    }

                    new SDialog(MainActivity.this, tips, "暂不连接", "立即连接",
                            new SDialog.SDialog2Listener() {
                                @Override
                                public void onClick(boolean left, View view) {
                                    if (!left) {
                                        handleBind(map.get("bindCode"));
                                    }
                                }
                            }).show();
                } else if ("3".equals(mode)) {
                    //未联网，需要去配网
                    new SDialog(MainActivity.this, "是否给共享屏配网", "", "立即配网",
                            new SDialog.SDialog2Listener() {
                                @Override
                                public void onClick(boolean left, View view) {
                                    if (!left) {
                                        NoNetwortDialogActivity.start(MainActivity.this, map.get("mac"));
                                    }
                                }
                            }).show();
                }
            }

            @Override
            public void permissionDenied(String[] permission) {
                ToastUtils.getInstance().showGlobalShort("需要获取位置信息权限才能读取Wi-Fi");
            }
        }, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void handleBind(String bindCode) {
        if (!UserInfoCenter.getInstance().isLogin()) {
            LoginActivity.start(this);
            return;
        }
        if (SSConnectManager.getInstance().isConnectedChannel()) {
            SSConnectManager.getInstance().bind(bindCode, new BindCallback() {
                @Override
                public void onSuccess(String bindCode, Device device) {
                    Log.d(TAG, "onSuccess: bindCode = " + bindCode + "   device = " + device);
                    ToastUtils.getInstance().showGlobalShort("正在连接");
                }

                @Override
                public void onFail(String bindCode, String errorType, String msg) {
                    Log.d(TAG, "onFail: bindCode = " + bindCode + " errorType = " + errorType + " msg = " + msg);
                    ToastUtils.getInstance().showGlobalShort("绑定失败：" + msg);
                }
            });
        } else {
            Log.d(TAG, "handleBind: channel 初始化失败");
        }
    }

    private void handleTempBind(String uniqueId, int type) {
        if (!UserInfoCenter.getInstance().isLogin()) {
            LoginActivity.start(this);
            return;
        }
        if (SSConnectManager.getInstance().isConnectedChannel()) {
            SSConnectManager.getInstance().tempBind(uniqueId, type, new BindCallback() {
                @Override
                public void onSuccess(String bindCode, Device device) {
                    Log.d(TAG, "onSuccess: bindCode = " + bindCode + "   device = " + device);
                    ToastUtils.getInstance().showGlobalShort("正在连接");
                }

                @Override
                public void onFail(String bindCode, String errorType, String msg) {
                    Log.d(TAG, "onFail: bindCode = " + bindCode + " errorType = " + errorType + " msg = " + msg);
                    ToastUtils.getInstance().showGlobalShort("绑定失败：" + msg);
                }
            });
        } else {
            Log.d(TAG, "handleBind: channel 初始化失败");
        }
    }


    private void submitTabClick(int tabIndex, String tabName) {
        Map<String, String> map = new HashMap<>();
        map.put("index", tabIndex + "");
        map.put("tab_name", tabName);
        MobclickAgent.onEvent(MainActivity.this, MAIN_PAGE_TAB, map);
    }

    @Override
    public int navigatorHeight() {
        return DimensUtils.dp2Px(this, 50);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
//        super.onSaveInstanceState(outState, outPersistentState);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
    }
}
