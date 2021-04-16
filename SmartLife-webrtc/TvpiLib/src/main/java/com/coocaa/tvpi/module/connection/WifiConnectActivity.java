package com.coocaa.tvpi.module.connection;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.views.SDialog;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.connect.callback.ConnectCallbackImpl;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.tvpi.broadcast.WiFiApStateReceiver;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.util.GpsUtil;
import com.coocaa.tvpi.util.WifiUtil;
import com.coocaa.tvpi.module.connection.wifi.WifiConnectCallback;
import com.coocaa.tvpi.module.connection.wifi.WifiConnectErrorCode;
import com.coocaa.tvpi.module.connection.wifi.WifiConnector;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.module.login.LoginActivity;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpilib.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.Map;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.session.Session;


public class WifiConnectActivity extends BaseActivity implements UnVirtualInputable {
    private static final String TAG = "WifiConnect";
    private static final String KEY_WIFI_SSID = "ssid";
    private static final String KEY_WIFI_PASSWORD = "password";
    private static final String KEY_DEVICE_NET_TYPE = "net";
    private static final int REQUEST_CODE_OPEN_GPS = 1;

    private enum UIState {
        TvUnSupportConnect,      //电视不支持连wifi(电视连接的有线)
        PhoneUnSupportConnect,   //手机不支持连wifi(android8.0 8.1无法通过代码连接wifi，直接让去设置连接)

        UnableOpenWifi,     //无法打开wifi(通过代码无法打开)
        UnConnectWifi,      //wifi未连接
        ConnectingWifi,     //wifi连接中
        ConnectWifiFailed,  //wifi连接失败
        ConnectWifiSuccess, //wifi连接成功

        ReconnectingServer,       //重连通道
        ConnectedServerSuccess, //重连通道成功
        ConnectedServerFailed//重连通道失败 连接了wifi但通道不通
    }

    private Context context;
    private TextView tvTitle;
    private TextView tvSubTitle;
    private ImageView ivConnectState;
    private ProgressBar progressBar;
    private Button btConnect;
    private String wifiSsid;
    private String wifiPassword;
    private String deviceNetType; //设备连接的网络类型
    private UIState uiState;
    private int connectFailedCount;
    private long timeToStartConnect;
    //这个变量是为了解决部分手机通过WifiUtil.openWifi()返回true后，
    //WifiHelp.setWifiEnable()仍然返回false的问题
    private volatile boolean isOpenedWifi = false; //是否开启了wifi


    public static void start(Context context) {
        Intent starter = new Intent(context, WifiConnectActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        if (!UserInfoCenter.getInstance().isLogin()) {
            ToastUtils.getInstance().showGlobalShort("未登录");
            LoginActivity.start(context);
            finish();
            return;
        }

        if (!SSConnectManager.getInstance().isConnected()) {
            ToastUtils.getInstance().showGlobalShort("请先连接设备");
            finish();
            return;
        }

        StatusBarHelper.translucent(this);
        overridePendingTransition(R.anim.push_bottom_in, 0);
        setContentView(R.layout.activity_wifi_connectw52);

        SSConnectManager.getInstance().addConnectCallback(serverConnectCallback);
        initWifiInfo();
        initView();
        checkPermissionAndInitStatus();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.push_bottom_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SSConnectManager.getInstance().removeConnectCallback(serverConnectCallback);
    }


    private void initWifiInfo() {
        Session connectSession = SSConnectManager.getInstance().getTarget();
        if (connectSession == null) {
            Log.d(TAG, "initWifiInfo: connectSession is null");
            return;
        }
        Map<String, String> extras = connectSession.getExtras();
        if (extras != null) {
            String ssid = extras.get(KEY_WIFI_SSID);
            if (!TextUtils.isEmpty(ssid)) {
                wifiSsid = URLDecoder.decode(ssid);
            }
            wifiPassword = extras.get(KEY_WIFI_PASSWORD);
            deviceNetType = extras.get(KEY_DEVICE_NET_TYPE);
            Log.d(TAG, "initWifiInfo: wifi ssid:" + wifiSsid);
            Log.d(TAG, "initWifiInfo: wifi password:" + wifiPassword);
            Log.d(TAG, "initWifiInfo: net type:" + deviceNetType);
        }
    }

    private void initView() {
        View root = findViewById(R.id.root);
        ImageView btClose = findViewById(R.id.btClose);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        ivConnectState = findViewById(R.id.ivConnectState);
        progressBar = findViewById(R.id.progressBar);
        btConnect = findViewById(R.id.btConnect);

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        BottomSheetBehavior<RelativeLayout> behavior = BottomSheetBehavior.from(findViewById(R.id.contentLayout));
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    finish();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });


        btConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uiState == null) {
                    //防止刚进界面还没状态时点击报错
                    return;
                }
                switch (uiState) {
                    case TvUnSupportConnect:
                    case UnableOpenWifi:
                        gotoWifiSettings();
                        finish();
                        break;
                    case PhoneUnSupportConnect:
                        clipWifiPassword();
                        gotoWifiSettings();
                        finish();
                        break;
                    case UnConnectWifi:
                    case ConnectWifiFailed:
                        connectWifi();
                        break;
                    case ConnectWifiSuccess:
                    case ConnectedServerSuccess:
                        finish();
                        break;
                    case ConnectedServerFailed:
                        reconnectServer();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void checkPermissionAndInitStatus() {
        Log.d(TAG, "checkPermissionAndInitStatus");

        if (!WifiUtil.setWifiEnable(context) && !isOpenedWifi) {
            openWifi();
            return;
        }
        Log.w(TAG, "wifi is opened");

        if (!GpsUtil.isOpen(context)) {
            openGps();
            return;
        }
        Log.d(TAG, "gps is opened");

        if (!PermissionsUtil.getInstance().hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            openLocationPermission();
            return;
        }
        Log.d(TAG, "location is opened");

        if (SSConnectManager.getInstance().isSameWifi()) {
            updateUIByState(UIState.ConnectedServerSuccess);
        } else {
            //连接有线或者wifi名称和密码都为空的情况下不支持连接wifi
            if ((TextUtils.isEmpty(wifiSsid) && TextUtils.isEmpty(wifiPassword))
                    || "ETHERNET".equals(deviceNetType)) {
                updateUIByState(UIState.TvUnSupportConnect);
            } else {
                if (wifiSsid.equals(WifiUtil.getConnectWifiSsid(this))) {
                    //连接同一wifi但本地通道不通
                    updateUIByState(UIState.ConnectedServerFailed);
                } else {
                    if (WifiUtil.isSupportConnectWifi()) {
                        updateUIByState(UIState.UnConnectWifi);
                    } else {
                        updateUIByState(UIState.PhoneUnSupportConnect);
                    }
                }
            }
        }
    }

    private void openWifi() {
        Log.w(TAG, "openWifi");
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                //这个方法需要在子线程中执行，部分手机是阻塞等待结果,部分手机是立马失败
                isOpenedWifi = WifiUtil.setWifiEnable(context);
                HomeUIThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isOpenedWifi) {
                            Log.w(TAG, "open wifi success");
                            checkPermissionAndInitStatus();
                        } else {
                            if (WiFiApStateReceiver.isWifiApOpen) {
                                Log.w(TAG, "open wifi failed: isOpenedWifiAp true");
                                //开启热点的情况下通过wifiManager.setWifiEnabled(true)无法开启wifi
                                //手动引导用户开启wifi
                                updateUIByState(UIState.UnableOpenWifi);
                                //1.只开启wifi(普遍情况，直接连接dongle配置的wifi)
                                //2.只开启热点 （代码开启WiFi会失败，用弹窗提示开启wifi，开启wifi后变到情况3）
                                //3.同时开启wifi和热点
                                //3.1如果dongle连接了自己开启的热点 (提示已连接该热点 难点如何获取自己开启的热点名字和密码用来判断是否已连接)
                                //3.2如果dongle未连接自己开启的热点 (连接dongle配置的wifi)
                            } else {
                                Log.w(TAG, "open wifi failed: finish");
                                finish();
                            }
                        }
                    }
                });
            }
        });
    }

    private void openGps() {
        Log.d(TAG, "openGps");
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
        ToastUtils.getInstance().showGlobalShort("请开启访问位置信息");
    }

    private void openLocationPermission() {
        Log.d(TAG, "openLocationPermission");
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                Log.d(TAG, "location permission granted");
                checkPermissionAndInitStatus();
            }

            @Override
            public void permissionDenied(String[] permission) {
                Log.d(TAG, "location permission denied");
                ToastUtils.getInstance().showGlobalShort("需开启访问位置信息，才能连接共享屏");
                finish();
            }
        }, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (GpsUtil.isOpen(WifiConnectActivity.this)) {
                checkPermissionAndInitStatus();
            } else {
                ToastUtils.getInstance().showGlobalShort("需开启定位服务，才能连接共享屏");
                finish();
            }
        }
    }

    private void connectWifi() {
        timeToStartConnect = System.currentTimeMillis();
        updateUIByState(UIState.ConnectingWifi);
        WifiConnector.withContext(this)
                .connect(wifiSsid, wifiPassword, new WifiConnectCallback() {
                    @Override
                    public void onConnectSuccess() {
                        updateUIByState(UIState.ConnectWifiSuccess);
                        submitConnectResult(true);
                    }

                    @Override
                    public void onConnectFail(WifiConnectErrorCode errorCode) {
                        switch (errorCode) {
                            case NO_OPEN_WIFI:
                                openWifi();
                                break;
                            case NO_GPS_PERMISSION:
                                openGps();
                                break;
                            case NO_LOCATION_PERMISSION:
                                openLocationPermission();
                                break;
                            case CONNECT_TIMEOUT:
                                updateUIByState(UIState.ConnectWifiFailed);
                                submitConnectResult(false);
                                showConnectFailedDialogIfNeed();
                                break;
                            default:
                                break;
                        }
                    }
                }).start();

    }

    private void reconnectServer() {
        Device device = SSConnectManager.getInstance().getHistoryDevice();
        if (null != device) {
            updateUIByState(UIState.ReconnectingServer);
            SSConnectManager.getInstance().connect(device);
        }
    }

    private final ConnectCallbackImpl serverConnectCallback = new ConnectCallbackImpl() {
        @Override
        public void loginState(int code, String info) {
            super.loginState(code, info);
            Log.d(TAG, "loginState: code = " + code + "  info = " + info);

            if (uiState == UIState.ReconnectingServer) {
                if (SSConnectManager.getInstance().isSameWifi()) {
                    updateUIByState(UIState.ConnectedServerSuccess);
                } else {
                    updateUIByState(UIState.ConnectedServerFailed);
                }
            } else {
                Log.d(TAG, "loginState: ui state is not UIState.ReconnectingServer. " + uiState);
            }
        }
    };

    private void updateUIByState(UIState state) {
        Log.d(TAG, "updateUIByState: state" + state);
        if (this.uiState == state) {
            return;
        }
        this.uiState = state;
        Log.d(TAG, "updateUIByState: uiState" + uiState);
        switch (uiState) {
            case TvUnSupportConnect:
                tvTitle.setText("连 WiFi 使用共享屏");
                tvSubTitle.setText("请保证手机与电视在同一网络");
                ivConnectState.setBackground(getResources().getDrawable(R.drawable.connect_wifi_blue));
                progressBar.setVisibility(View.INVISIBLE);
                btConnect.setText("去「设置」连接");
                btConnect.setBackground(getResources().getDrawable(R.drawable.bg_blue_round_28));
                break;
            case PhoneUnSupportConnect:
                tvTitle.setText("手机无法自动连接 WiFi");
                tvSubTitle.setText("WiFi 名称 ：" + wifiSsid);
                ivConnectState.setBackground(getResources().getDrawable(R.drawable.connect_wifi_failed_gray));
                progressBar.setVisibility(View.INVISIBLE);
                btConnect.setText("密码已复制，去「设置」连接");
                btConnect.setBackground(getResources().getDrawable(R.drawable.bg_gray_round_28));
                break;
            case UnConnectWifi:
                tvTitle.setText("连 WiFi 使用共享屏");
                tvSubTitle.setText("WiFi 名称 ：" + wifiSsid);
                ivConnectState.setBackground(getResources().getDrawable(R.drawable.connect_wifi_blue));
                progressBar.setVisibility(View.INVISIBLE);
                btConnect.setText("一键连接");
                btConnect.setBackground(getResources().getDrawable(R.drawable.bg_blue_round_28));
                break;
            case ConnectingWifi:
                tvTitle.setText("连 WiFi 使用共享屏");
                tvSubTitle.setText("WiFi 名称 ：" + wifiSsid);
                ivConnectState.setBackground(getResources().getDrawable(R.drawable.connect_wifi_blue));
                progressBar.setVisibility(View.VISIBLE);
                btConnect.setText("连接中...");
                btConnect.setBackground(getResources().getDrawable(R.drawable.bg_blue_unable_round_28));
                break;
            case UnableOpenWifi:
                tvTitle.setText("WiFi打开失败");
                tvSubTitle.setText("WiFi 名称 ：" + wifiSsid);
                ivConnectState.setBackground(getResources().getDrawable(R.drawable.connect_wifi_blue));
                progressBar.setVisibility(View.INVISIBLE);
                btConnect.setText("去设置开启手机 WiFi");
                btConnect.setBackground(getResources().getDrawable(R.drawable.bg_blue_round_28));
                break;
            case ConnectWifiSuccess:
                tvTitle.setText("连接成功");
                tvSubTitle.setText("欢迎继续使用共享屏");
                ivConnectState.setBackground(getResources().getDrawable(R.drawable.connect_wifi_success_green));
                progressBar.setVisibility(View.INVISIBLE);
                btConnect.setText("完成");
                btConnect.setBackground(getResources().getDrawable(R.drawable.bg_green_round_28));
                break;
            case ConnectWifiFailed:
                tvTitle.setText("连接失败");
                tvSubTitle.setText("点击按钮尝试重新连接共享屏 WiFi");
                ivConnectState.setBackground(getResources().getDrawable(R.drawable.connect_wifi_failed_gray));
                progressBar.setVisibility(View.INVISIBLE);
                btConnect.setText("再试一次");
                btConnect.setBackground(getResources().getDrawable(R.drawable.bg_gray_round_28));
                break;
            case ReconnectingServer:
                tvTitle.setText("连 WiFi 使用共享屏");
                tvSubTitle.setText("WiFi 名称 ：" + wifiSsid);
                ivConnectState.setBackground(getResources().getDrawable(R.drawable.connect_wifi_blue));
                progressBar.setVisibility(View.VISIBLE);
                btConnect.setText("连接中...");
                btConnect.setBackground(getResources().getDrawable(R.drawable.bg_blue_unable_round_28));
                break;
            case ConnectedServerSuccess:
                tvTitle.setText("连接成功");
                tvSubTitle.setText("欢迎继续使用共享屏");
                ivConnectState.setBackground(getResources().getDrawable(R.drawable.connect_wifi_success_green));
                progressBar.setVisibility(View.INVISIBLE);
                btConnect.setText("完成");
                btConnect.setBackground(getResources().getDrawable(R.drawable.bg_green_round_28));
                break;
            case ConnectedServerFailed:
                tvTitle.setText("局域网无法连接");
                tvSubTitle.setText("该网络无法进行局域网共享");
                ivConnectState.setBackground(getResources().getDrawable(R.drawable.connect_wifi_failed_gray));
                progressBar.setVisibility(View.INVISIBLE);
                btConnect.setText("再试一次");
                btConnect.setBackground(getResources().getDrawable(R.drawable.bg_gray_round_28));
                break;
            default:
                break;
        }
    }

    private void showConnectFailedDialogIfNeed() {
        connectFailedCount++;
        if (connectFailedCount >= 2) {
            connectFailedCount = 0;
            SDialog sDialog = new SDialog(this, "Wi-Fi连接失败", "密码已复制，去设置里连接",
                    R.string.cancel, R.string.bt_connect_wifi,
                    new SDialog.SDialog2Listener() {
                        @Override
                        public void onClick(boolean left, View view) {
                            if (!left) {
                                clipWifiPassword();
                                gotoWifiSettings();
                                finish();
                            }
                        }
                    });
            if (!WifiConnectActivity.this.isFinishing() && !sDialog.isShowing()) {
                sDialog.show();
            }
        }
    }

    private void gotoWifiSettings() {
        Intent wifiSettingsIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivity(wifiSettingsIntent);
    }

    private void clipWifiPassword() {
        if (!TextUtils.isEmpty(wifiPassword)) {
            ClipboardManager clipboard = (ClipboardManager) WifiConnectActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("password", wifiPassword);
            clipboard.setPrimaryClip(clip);
        }
    }

    private void submitConnectResult(boolean success) {
        Device device = SSConnectManager.getInstance().getDevice();
        CoocaaUserInfo coocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
        LogParams params = LogParams.newParams();
        params.append("connect_result", success ? "success" : "fail")
                .append("ss_device_id", device == null ? "disconnected" : device.getLsid())
                .append("ss_device_type", device == null ? "disconnected" : device.getZpRegisterType())
                .append("account", coocaaUserInfo == null ? "not_login" : coocaaUserInfo.getOpen_id());
        LogSubmit.event("connect_same_wifi_result", params.getParams());
        if (success) {
            submitConnectTime();
        }
    }

    private void submitConnectTime() {
        Device device = SSConnectManager.getInstance().getDevice();
        CoocaaUserInfo coocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
        LogParams params = LogParams.newParams();
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        long durationLong = System.currentTimeMillis() - timeToStartConnect;
        String duration = decimalFormat.format((float) durationLong / 1000);
        params.append("duration", duration)
                .append("ss_device_id", device == null ? "disconnected" : device.getLsid())
                .append("ss_device_type", device == null ? "disconnected" : device.getZpRegisterType())
                .append("account", coocaaUserInfo == null ? "not_login" : coocaaUserInfo.getOpen_id());
        LogSubmit.event("connect_same_wifi_load_time", params.getParams());
    }
}
