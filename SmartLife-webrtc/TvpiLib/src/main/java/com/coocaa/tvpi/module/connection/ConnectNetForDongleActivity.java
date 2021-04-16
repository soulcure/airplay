package com.coocaa.tvpi.module.connection;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.BleClientManager;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.connect.callback.BindCallback;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.util.GpsUtil;
import com.coocaa.tvpi.util.WifiUtil;
import com.coocaa.tvpi.module.connection.constant.ConnNetForDongleUIState;
import com.coocaa.tvpi.module.connection.view.ConnectNetForDongleView;
import com.coocaa.tvpi.util.IntentUtils;
import com.coocaa.tvpi.util.NetworkUtil;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.google.gson.Gson;
import com.skyworth.bleclient.BluetoothClientCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import swaiotos.channel.iot.ss.device.Device;

/**
 * 给dongle配网界面
 */
public class ConnectNetForDongleActivity extends BaseActivity implements UnVirtualInputable {
    private static final String TAG = ConnectNetForDongleActivity.class.getSimpleName();
    private static final String KEY_WIFI_SSID = "KEY_WIFI_SSID";
    private static final String KEY_IS_NEED_PASSWORD = "KEY_HAS_PASSWORD";
    private static final String KEY_IS_USER_WIFI = "KEY_IS_USER_WIFI";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_SETTINGS_CODE = 4;

    private ConnectNetForDongleView connNetForDongleView;
    private static String macAddress;
    private Handler handler;
    private BleClientManager bleClientManager;

    public static void start(Context context, String mac) {
        macAddress = mac;
        Log.d(TAG, "start" + macAddress);
        if (GpsUtil.isOpen(context)) {
            Log.d(TAG, "gps is open");
            PermissionsUtil.getInstance().requestPermission(context, new PermissionListener() {
                @Override
                public void permissionGranted(String[] permission) {
                    Log.d(TAG, "permission ranted: ACCESS_FINE_LOCATION");
                    if (NetworkUtil.isWifiConnected(context)) {
                        //已经连接wifi的情况下显示配网界面
                        Log.d(TAG, "wifi connected");
                        String connectedWifiSsid = WifiUtil.getConnectWifiSsid(context);
                        boolean isConnectedWifiHasPsd = WifiUtil.isWifiHasPassword(context, connectedWifiSsid);
                        Log.d(TAG, "wifi connected: ssid:" + connectedWifiSsid);
                        Log.d(TAG, "wifi connected: has password:" + isConnectedWifiHasPsd);
                        startUseWifi(context, connectedWifiSsid, isConnectedWifiHasPsd);
                    } else {
                        //未连接wifi的情况下显示wifi列表
                        Log.d(TAG, "wifi unconnected");
                        Intent intent = new Intent(context, WifiListActivity.class);
                        intent.putExtra(WifiListActivity.KEY_START_MODE, WifiListActivity.START_MODE_NOT_CONNECTED_WIFI);
                        context.startActivity(intent);
                    }
                }

                @Override
                public void permissionDenied(String[] permission) {
                    Log.d(TAG, "permission denied：ACCESS_FINE_LOCATION");
                    ToastUtils.getInstance().showGlobalLong("需开启访问位置信息，才能连接酷开共享屏");
                }
            }, Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            Log.d(TAG, "gps not open");
            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            ToastUtils.getInstance().showGlobalLong("需开启定位服务，才能连接酷开共享屏");
        }
    }

    //使用wifi配网
    public static void startUseWifi(Context context, String wifiSSid, boolean isNeedPassword) {
        Intent starter = new Intent(context, ConnectNetForDongleActivity.class);
        starter.putExtra(KEY_WIFI_SSID, wifiSSid);
        starter.putExtra(KEY_IS_NEED_PASSWORD, isNeedPassword);
        starter.putExtra(KEY_IS_USER_WIFI, true);
        context.startActivity(starter);
    }

    //使用热点配网
    public static void startUserHotpots(Context context) {
        Intent starter = new Intent(context, ConnectNetForDongleActivity.class);
        starter.putExtra(KEY_WIFI_SSID, "");
        starter.putExtra(KEY_IS_NEED_PASSWORD, false);
        starter.putExtra(KEY_IS_USER_WIFI, false);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connNetForDongleView = new ConnectNetForDongleView(this);
        setContentView(connNetForDongleView);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        handler = new Handler(Looper.getMainLooper());
        parseIntent();
        setListener();
        startScan();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeTimeoutRunnable();
        if (bleClientManager != null) {
            bleClientManager.removeScanCallBack(scanCallBack);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + requestCode + requestCode + data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startScan();
            }
        } else if (requestCode == REQUEST_SETTINGS_CODE) {
            startScan();
        } else {
            connNetForDongleView.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void parseIntent() {
        if (getIntent() != null) {
            String wifiSSid = IntentUtils.INSTANCE.getStringExtra(getIntent(), KEY_WIFI_SSID);
            boolean isNeedPassword = IntentUtils.INSTANCE.getBooleanExtra(getIntent(), KEY_IS_NEED_PASSWORD);
            boolean isUserWifiConnNet = IntentUtils.INSTANCE.getBooleanExtra(getIntent(), KEY_IS_USER_WIFI);
            Log.d(TAG, "parseIntent: wifiSSid:" + wifiSSid);
            Log.d(TAG, "parseIntent: isNeedPassword:" + isNeedPassword);
            Log.d(TAG, "parseIntent: isUserWifiConnNet:" + isUserWifiConnNet);
            connNetForDongleView.initConnNetInfo(wifiSSid, isNeedPassword, isUserWifiConnNet);
        }
    }

    private void setListener() {
        connNetForDongleView.setConnNetListener(new ConnectNetForDongleView.ConnNetListener() {
            @Override
            public void onConnectClick() {
                doConnectNetForDongle();
            }
        });
    }

    private void startScan() {
        Log.d(TAG, "startScan: " + macAddress);
        if (bleClientManager == null) {
            bleClientManager = BleClientManager.instance(this);
        }
        bleClientManager.startScan(macAddress, scanCallBack);
    }

    private void doConnectNetForDongle() {
        postTimeoutRunnable();
        if (bleClientManager.isSupport()) {
            if (bleClientManager.isConnected(macAddress)) {
                Log.d(TAG, "已经连接蓝牙，直接发送wifi info: ");
                sendWifiCmd();
            } else {
                startScan();
                Log.w(TAG, "doConnectBle() called bleClientManager.isConnected false " + macAddress);
            }
        }
    }

    private final BleClientManager.ScanCallBack scanCallBack = new BleClientManager.ScanCallBack() {

        @Override
        public void onUpdateDevices(List<Device> list) {
            Log.d(TAG, "onScanSuccess: 扫描成功---" + new Gson().toJson(list));
        }

        @Override
        public void onStateChange(BluetoothClientCallback.DeviceState res) {
            Log.d(TAG, "onStateChange: 蓝牙连接状态:" + res.name());
            switch (res) {
                case CONNECTED:
                    Log.d(TAG, "onStateChange: 已连接蓝牙");
                    if (isConnecting()) {
                        sendWifiCmd();
                    } else {
                        Log.w(TAG, "onStateChange UIState != Connecting :" + connNetForDongleView.getUiState());
                    }
                    // requestSession();
                    break;
                case FAILED:
                    Log.d(TAG, "onStateChange: 连接蓝牙失败");
                    setUiState(ConnNetForDongleUIState.ConnectError);
                    break;
                case BLE_DISABLE:
                    reqOpenBle();
                    break;
                case LOCATION_PERMISSION:
                    reqLocationPermission();
                    break;
                case SCANING:
                case DISCONNECT:
                case CONNECTING:
                default:
                    break;
            }
        }
    };

    private void sendWifiCmd() {
        Log.d(TAG, "sendWifiCmd: " + System.currentTimeMillis());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("proto", "ConfigureWiFi");
            jsonObject.put("ssid", getWifiSSid());
            jsonObject.put("password", getWifiPassword());
            String info = jsonObject.toString();
            Log.d(TAG, "sendWifiCmd: " + info);
            bleClientManager.setConfigWifi(info, new BleClientManager.WifiCallBack() {
                @Override
                public void onProgress(int errCode, int status, String sid, String bindCode) {
                    Log.d(TAG, "onProgress: " + errCode + "/" + status + "/" + sid + "/" + bindCode);

                    if (isCanceledConnect()) {
                        Log.d(TAG, "onProgress: connect wifi is canceled ");
                        return;
                    }

                    switch (status) {
                        case 1:
                            setUiState(ConnNetForDongleUIState.Connecting);
                            break;
                        case -1:
                            removeTimeoutRunnable();
                            setUiState(ConnNetForDongleUIState.ConnectError);
                            break;
                        case 2:
                            removeTimeoutRunnable();
                            setUiState(ConnNetForDongleUIState.ConnectSuccess);
                            if (!TextUtils.isEmpty(bindCode)) {
                                SSConnectManager.getInstance().bind(bindCode, new BindCallback() {
                                    @Override
                                    public void onSuccess(String bindCode, Device device) {
                                        ToastUtils.getInstance().showGlobalShort("正在连接");
                                        //bind成功后会调用connect，在connect保存历史设备
                                       /* if (null != device) {
                                           SSConnectManager.getInstance().setHistoryLsid(device.getLsid());
                                            SSConnectManager.getInstance().saveHistoryDevice(device);
                                        }*/
                                    }

                                    @Override
                                    public void onFail(String bindCode, String errorType, String msg) {
                                        ToastUtils.getInstance().showGlobalShort("绑定失败");
                                    }
                                });
                            }
                            break;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setUiState(ConnNetForDongleUIState uiState) {
        if(connNetForDongleView != null) {
            connNetForDongleView.setUiState(uiState);
        }
    }

    private boolean isConnecting() {
        if(connNetForDongleView != null) {
            return connNetForDongleView.getUiState() == ConnNetForDongleUIState.Connecting;
        }else {
            return false;
        }
    }

    private boolean isCanceledConnect() {
        if(connNetForDongleView != null) {
            return connNetForDongleView.isCanceledConnect();
        }else {
            return true;
        }
    }

    private String getWifiSSid() {
        if(connNetForDongleView != null) {
            return connNetForDongleView.getSSidEditText().getText().toString();
        }else {
            return "null";
        }
    }

    private String getWifiPassword() {
        if(connNetForDongleView != null) {
            return connNetForDongleView.getPasswordEditText().getText().toString();
        }else {
            return "null";
        }
    }

    private void reqOpenBle() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void reqLocationPermission() {
        if (Build.VERSION.SDK_INT >= 29) {
            String[] permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION};

            List<String> list = new ArrayList<>();

            for (String permission : permissions) {          //逐个判断是否还有未通过的权限
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    list.add(permission);
                }
            }

            if (list.size() > 0) {                           //有权限没有通过，需要申请
                //ActivityCompat.requestPermissions(getActivity(), permissions, PERMISSION_REQUEST_COARSE_LOCATION);
                requestPermissions(permissions, PERMISSION_REQUEST_COARSE_LOCATION);
            } else {
                Log.e("getPermissions() >>>", "已经授权");     //权限已经都通过了
            }

        } else if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION};

            List<String> list = new ArrayList<>();

            for (String permission : permissions) {          //逐个判断是否还有未通过的权限
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    list.add(permission);
                }
            }
            if (list.size() > 0) {                           //有权限没有通过，需要申请
                //ActivityCompat.requestPermissions(getActivity(), permissions, PERMISSION_REQUEST_COARSE_LOCATION);
                requestPermissions(permissions, PERMISSION_REQUEST_COARSE_LOCATION);
            } else {
                Log.e("getPermissions() >>>", "已经授权");     //权限已经都通过了
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            boolean hasPermissionDismiss = false;//有权限没有通过
            for (int grantResult : grantResults)
                if (grantResult == PackageManager.PERMISSION_DENIED) { //拒绝权限
                    hasPermissionDismiss = true;
                    break;
                }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                //无权限，且被选择"不再提醒"：提醒客户到APP应用设置中打开权限
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    showPermissionRationale();
                }
            } else {
                startScan();   //允许权限
            }
        }
    }

    // 跳出 提示界面；
    private void showPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("权限提示")
                .setMessage("必须打开位置权限 “始终允许”，才能使用蓝牙配网")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到APP设置页面
                        gotoSetting();
                    }
                })
                .create().show();
    }

    // 跳转到设置界面；
    private void gotoSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivityForResult(intent, REQUEST_SETTINGS_CODE);
    }

    private void postTimeoutRunnable() {
        if (handler != null) {
            handler.removeCallbacks(connTimeoutRunnable);
            handler.postDelayed(connTimeoutRunnable, 30 * 1000);
        }
    }

    private void removeTimeoutRunnable() {
        if (handler != null) {
            handler.removeCallbacks(connTimeoutRunnable);
        }
    }

    private final Runnable connTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "connTimeoutRunnable: 连接超时" + System.currentTimeMillis());
            if (isCanceledConnect()) {
                Log.d(TAG, " connTimeoutRunnable :connect wifi is canceled ");
                return;
            }

            setUiState(ConnNetForDongleUIState.ConnectTimeout);
            ToastUtils.getInstance().showGlobalShort("连接超时");
            BleClientManager.instance(ConnectNetForDongleActivity.this).disConnect();
        }
    };
}
