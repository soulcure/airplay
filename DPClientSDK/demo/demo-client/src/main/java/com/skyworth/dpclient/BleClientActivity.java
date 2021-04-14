package com.skyworth.dpclient;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.skyworth.bleclient.BLEClient;
import com.skyworth.bleclient.BleDeviceInfo;
import com.skyworth.bleclient.BlePdu;
import com.skyworth.bleclient.BluetoothClientCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class BleClientActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "client";

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_SETTINGS_CODE = 3;

    private BLEClient bleClient;
    private TextView tv_info;

    //private String mac = "B00247B9A1BF";
    //private String mac = "B00247B99EF5";
    private String mac = "102C6B6828D4";

    private ProcessHandler mProcessHandler;


    private void reqLocationPermission() {
        if (Build.VERSION.SDK_INT >= 29) {
            String[] permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION};

            List<String> list = new ArrayList<>();

            for (String permission : permissions) {          //逐个判断是否还有未通过的权限
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    list.add(permission);
                }
            }
            if (list.size() > 0) {                           //有权限没有通过，需要申请
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_COARSE_LOCATION);
            } else {
                Log.e("getPermissions() >>>", "已经授权");     //权限已经都通过了
            }

        } else if (Build.VERSION.SDK_INT >= 23) {
            // Android M Permission check
            String[] permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION};

            List<String> list = new ArrayList<>();

            for (String permission : permissions) {          //逐个判断是否还有未通过的权限
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    list.add(permission);
                }
            }
            if (list.size() > 0) {                           //有权限没有通过，需要申请
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_COARSE_LOCATION);
            } else {
                Log.e("getPermissions() >>>", "已经授权");     //权限已经都通过了
            }
        }
    }


    private void reqOpenBle() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    BluetoothClientCallback callback = new BluetoothClientCallback() {
        @Override
        public void onMessageShow(BlePdu blePdu) {
            try {
                String msg = new String(blePdu.body);
                Log.e("bleClient", "onMessageShow---" + msg);

                JSONObject jsonObject = new JSONObject(msg);
                int code = jsonObject.optInt("code");
                String message = jsonObject.optString("msg");
                String proto = jsonObject.optString("proto");
                String device = jsonObject.optString("device");
                String session = jsonObject.optString("session");

                if (code == 0
                        && !TextUtils.isEmpty(proto)
                        && !TextUtils.isEmpty(device)
                        && proto.equals("TVDeviceInfo")) {

                    Gson gson = new Gson();
                    TVDeviceInfo t = gson.fromJson(device, TVDeviceInfo.class);
                    Log.e("bleClient", "reqDeviceInfo---" + t.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStateChange(DeviceState res) {
            tv_info.setText(mac + "蓝牙连接状态:" + res.name());

            if (res == DeviceState.BLE_DISABLE) {
                reqOpenBle();
            } else if (res == DeviceState.LOCATION_PERMISSION) {
                reqLocationPermission();
            } else if (res == DeviceState.NOTSUPPORT) {
                Toast.makeText(BleClientActivity.this, "不支持此设备", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onScanResult(String mac) {
            Log.e("bleClient", "onScanResult---" + mac);
        }

        @Override
        public void onScanList(Collection<BleDeviceInfo> bleDeviceInfoList) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_client);
        initHandler();

        tv_info = findViewById(R.id.tv_info);
        findViewById(R.id.btn_open).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_stop_scan).setOnClickListener(this);
        findViewById(R.id.btn_key_down).setOnClickListener(this);
        findViewById(R.id.btn_info).setOnClickListener(this);
        findViewById(R.id.btn_wifi).setOnClickListener(this);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_open:

                bleClient = new BLEClient(this, callback, mProcessHandler);
                bleClient.startScan(mac);
                break;
            case R.id.btn_close:
                if (bleClient != null) {
                    bleClient.disConnect();
                }
                break;
            case R.id.btn_stop_scan:
                if (bleClient != null) {
                    bleClient.stopScan();
                    bleClient = null;
                }
                break;
            case R.id.btn_key_down:
                String down = "{\"id\":\"a21239e1-1fa8-4dbb-8c5a-4959cd65eb5e\",\"source\":\"{\\\"id\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.130.135:34000\\\",\\\"address-local\\\":\\\"172.20.130.135\\\",\\\"stream-local\\\":\\\"172.20.130.135\\\",\\\"im-cloud\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\"}}\",\"target\":\"{\\\"id\\\":\\\"d0e69442ec094a918ba1d06699b537e5\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.146.231:34000\\\",\\\"address-local\\\":\\\"172.20.146.231\\\",\\\"stream-local\\\":\\\"172.20.146.231\\\",\\\"im-cloud\\\":\\\"d0e69442ec094a918ba1d06699b537e5\\\"}}\",\"client-source\":\"ss-clientID-SmartScreen\",\"client-target\":\"ss-clientID-appstore_12345\",\"type\":\"TEXT\",\"content\":\"{\\\"cmd\\\":\\\"25\\\",\\\"param\\\":\\\"\\\",\\\"type\\\":\\\"KEY_EVENT\\\"}\",\"extra\":{},\"reply\":false}";
                bleClient.sendMsg(down, BlePdu.TEMP_CMD);

                break;
            case R.id.btn_info:
                /*{
                    "proto": "TVDeviceInfo",   //内容固定
                    "deviceInfo": "",  //内容为空
                    "session": ""  //设置手机本机的session
                }*/
                //String info = "{\"proto\": \"TVDeviceInfo\",\"deviceInfo\": \"\",\"session\": \"{\\\"id\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.130.135:34000\\\",\\\"address-local\\\":\\\"172.20.130.135\\\",\\\"stream-local\\\":\\\"172.20.130.135\\\",\\\"im-cloud\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\"}}\"}";
                //bleClient.sendMsg(info, BlePdu.TEMP_PROTO);

                WifiConnectManager.connectWifi(this, "soulcure", "", "OPEN");

                break;
            case R.id.btn_wifi:
                //String wifi = "{\"proto\": \"ConfigureWiFi\", \"ssid\": \"Coocaatest04\", \"password\": \"23757007\"}";
                String wifi = "{\"proto\": \"ConfigureWiFi\", \"ssid\": \"soulcure\", \"password\": \"\"}";
                bleClient.sendMsg(wifi, BlePdu.TEMP_PROTO);
                break;

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                bleClient.startScan(mac);
            }
        } else if (requestCode == REQUEST_SETTINGS_CODE) {
            bleClient.startScan(mac);   //允许权限
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                boolean hasPermissionDismiss = false;//有权限没有通过
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) { //拒绝权限
                        hasPermissionDismiss = true;
                    }
                }
                //如果有权限没有被允许
                if (hasPermissionDismiss) {
                    //无权限，且被选择"不再提醒"：提醒客户到APP应用设置中打开权限
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        showPermissionRationale("权限提示", "必须打开位置权限 “始终允许”，才能使用蓝牙配网");
                    }
                } else {
                    bleClient.startScan(mac);   //允许权限
                }
                break;
        }
    }


    // 跳出 提示界面；
    private void showPermissionRationale(@Nullable String title, @NonNull String content) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //finish();
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleClient != null) {
            bleClient.disConnect();

            bleClient = null;
        }


    }


    /**
     * 线程初始化
     */
    private void initHandler() {
        if (mProcessHandler == null) {
            HandlerThread handlerThread = new HandlerThread(
                    "handler looper Thread");
            handlerThread.start();
            mProcessHandler = new ProcessHandler(handlerThread.getLooper());
        }
    }

    /**
     * 子线程handler,looper
     *
     * @author Administrator
     */
    private class ProcessHandler extends Handler {

        public ProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    break;
                default:
                    break;
            }

        }

    }

}
