package com.skyworth.dpclient;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.skyworth.bleclient.BleDeviceInfo;
import com.skyworth.bleclient.BlePdu;
import com.skyworth.bleclient.BluetoothClientCallback;
import com.skyworth.bleclient.BluetoothNewClient;

import org.json.JSONObject;

import java.util.Collection;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.utils.Utils;


public class BleClientNewActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "client";

    private BluetoothNewClient bleClient;

    private TextView tv_info;
    public static final int REQUEST_GPS = 4;

    //private String mac = "B00247B9A1BF";
    //private String mac = "B00247B99EF5";
    private String mac = "B00247B99F49";

    private ProcessHandler mProcessHandler;

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
        }

        @Override
        public void onScanResult(String mac) {
            Log.e("bleClient", "onScanResult---" + mac);
        }

        @Override
        public void onScanList(Collection<BleDeviceInfo> bleDeviceInfoList) {
            Log.d(TAG, "onScanList() called with: bleDeviceInfoList = [" + bleDeviceInfoList + "]");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_client_new);
        initHandler();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        tv_info = findViewById(R.id.tv_info);
        findViewById(R.id.btn_open).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_key_up).setOnClickListener(this);
        findViewById(R.id.btn_key_down).setOnClickListener(this);
        findViewById(R.id.btn_info).setOnClickListener(this);
        findViewById(R.id.btn_wifi).setOnClickListener(this);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_open:

                bleClient = new BluetoothNewClient(this, callback, mProcessHandler, false);
                bleClient.startScan(mac);
                break;
            case R.id.btn_close:
                if (bleClient != null) {
                    bleClient.disConnect();
                    bleClient = null;
                }
                break;
            case R.id.btn_key_up:
                String up = "{\"id\":\"a21239e1-1fa8-4dbb-8c5a-4959cd65eb5e\",\"source\":\"{\\\"id\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.130.135:34000\\\",\\\"address-local\\\":\\\"172.20.130.135\\\",\\\"stream-local\\\":\\\"172.20.130.135\\\",\\\"im-cloud\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\"}}\",\"target\":\"{\\\"id\\\":\\\"d0e69442ec094a918ba1d06699b537e5\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.146.231:34000\\\",\\\"address-local\\\":\\\"172.20.146.231\\\",\\\"stream-local\\\":\\\"172.20.146.231\\\",\\\"im-cloud\\\":\\\"d0e69442ec094a918ba1d06699b537e5\\\"}}\",\"client-source\":\"ss-clientID-SmartScreen\",\"client-target\":\"ss-clientID-appstore_12345\",\"type\":\"TEXT\",\"content\":\"{\\\"cmd\\\":\\\"24\\\",\\\"param\\\":\\\"\\\",\\\"type\\\":\\\"KEY_EVENT\\\"}\",\"extra\":{},\"reply\":false}";
                bleClient.sendMsg(up, BlePdu.TEMP_CMD);

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
                String info = "{\"proto\": \"TVDeviceInfo\",\"deviceInfo\": \"\",\"session\": \"{\\\"id\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.130.135:34000\\\",\\\"address-local\\\":\\\"172.20.130.135\\\",\\\"stream-local\\\":\\\"172.20.130.135\\\",\\\"im-cloud\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\"}}\"}";
                bleClient.sendMsg(info, BlePdu.TEMP_PROTO);

                break;
            case R.id.btn_wifi:
            /*{
                "proto": "ConfigureWiFi",
                 "ssid": "Coocaatest04",
                 "password": "23757007"
            }*/
                //String wifi = "{\"proto\": \"ConfigureWiFi\", \"ssid\": \"Coocaatest04\", \"password\": \"23757007\"}";
                String wifi = "{\"proto\": \"ConfigureWiFi\", \"ssid\": \"colin\", \"password\": \"12345678\"}";
                bleClient.sendMsg(wifi, BlePdu.TEMP_PROTO);

                //String targetSid = "Coocaatest04";
                ///String targetPsd = "27357007";

                //String targetSid = "colin";
                //String targetPsd = "12345678";

                //String enc = "WPA";
                //WifiConnectManager.connectWifi(this, targetSid, targetPsd, enc);

                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO request success
                }
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleClient != null) {
            bleClient.disConnect();

            Ble.getInstance().released();
            bleClient = null;
        }
    }


    //检查蓝牙是否支持及打开
    private void checkBlueStatus() {
        if (!Ble.getInstance().isSupportBle(this)) {
            finish();
        }
        if (!Ble.getInstance().isBleEnable()) {
            Toast.makeText(getApplicationContext(), "蓝牙不支持", Toast.LENGTH_LONG).show();
        } else {
            checkGpsStatus();
        }
    }

    private void checkGpsStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Utils.isGpsOpen(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("为了更精确的扫描到Bluetooth LE设备,请打开GPS定位")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            BleClientNewActivity.this.startActivityForResult(intent, REQUEST_GPS);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .create()
                    .show();
        } else {

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
