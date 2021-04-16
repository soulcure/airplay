package com.coocaa.tvpi.module.connection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.coocaa.tvpilib.R;
import com.skyworth.bleclient.BluetoothClient;

import java.util.List;

import com.coocaa.smartscreen.BleClientManager;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;


public class BleClientActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "client";

    private BluetoothClient bleClient;

    private TextView tv_info;

    //private String mac = "B00247B9A1BF";
    private String mac = "B00247B99AB1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_client);

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

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {

        int id = view.getId();
        if (id == R.id.btn_open) {
            BleClientManager.instance(this).startScan(mac, new BleClientManager.ScanCallBack() {

                @Override
                public void onUpdateDevices(List<Device> list) {
                    tv_info.setText("扫描成功---" + list.size());
                }

                @Override
                public void onStateChange(com.skyworth.bleclient.BluetoothClientCallback.DeviceState res) {

                }
            });
        } else if (id == R.id.btn_close) {
            BleClientManager.instance(this).disConnect();
        } else if (id == R.id.btn_key_up) {
            String up = "{\"id\":\"a21239e1-1fa8-4dbb-8c5a-4959cd65eb5e\",\"source\":\"{\\\"id\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.130.135:34000\\\",\\\"address-local\\\":\\\"172.20.130.135\\\",\\\"stream-local\\\":\\\"172.20.130.135\\\",\\\"im-cloud\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\"}}\",\"target\":\"{\\\"id\\\":\\\"d0e69442ec094a918ba1d06699b537e5\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.146.231:34000\\\",\\\"address-local\\\":\\\"172.20.146.231\\\",\\\"stream-local\\\":\\\"172.20.146.231\\\",\\\"im-cloud\\\":\\\"d0e69442ec094a918ba1d06699b537e5\\\"}}\",\"client-source\":\"ss-clientID-SmartScreen\",\"client-target\":\"ss-clientID-appstore_12345\",\"type\":\"TEXT\",\"content\":\"{\\\"cmd\\\":\\\"24\\\",\\\"param\\\":\\\"\\\",\\\"type\\\":\\\"KEY_EVENT\\\"}\",\"extra\":{},\"reply\":false}{\"id\":\"a21239e1-1fa8-4dbb-8c5a-4959cd65eb5e\",\"source\":\"{\\\"id\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.130.135:34000\\\",\\\"address-local\\\":\\\"172.20.130.135\\\",\\\"stream-local\\\":\\\"172.20.130.135\\\",\\\"im-cloud\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\"}}\",\"target\":\"{\\\"id\\\":\\\"d0e69442ec094a918ba1d06699b537e5\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.146.231:34000\\\",\\\"address-local\\\":\\\"172.20.146.231\\\",\\\"stream-local\\\":\\\"172.20.146.231\\\",\\\"im-cloud\\\":\\\"d0e69442ec094a918ba1d06699b537e5\\\"}}\",\"client-source\":\"ss-clientID-SmartScreen\",\"client-target\":\"ss-clientID-appstore_12345\",\"type\":\"TEXT\",\"content\":\"{\\\"cmd\\\":\\\"25\\\",\\\"param\\\":\\\"\\\",\\\"type\\\":\\\"KEY_EVENT\\\"}\",\"extra\":{},\"reply\":false}";
            BleClientManager.instance(this).sendCmd(up);
        } else if (id == R.id.btn_key_down) {
            String down = "{\"id\":\"a21239e1-1fa8-4dbb-8c5a-4959cd65eb5e\",\"source\":\"{\\\"id\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.130.135:34000\\\",\\\"address-local\\\":\\\"172.20.130.135\\\",\\\"stream-local\\\":\\\"172.20.130.135\\\",\\\"im-cloud\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\"}}\",\"target\":\"{\\\"id\\\":\\\"d0e69442ec094a918ba1d06699b537e5\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.146.231:34000\\\",\\\"address-local\\\":\\\"172.20.146.231\\\",\\\"stream-local\\\":\\\"172.20.146.231\\\",\\\"im-cloud\\\":\\\"d0e69442ec094a918ba1d06699b537e5\\\"}}\",\"client-source\":\"ss-clientID-SmartScreen\",\"client-target\":\"ss-clientID-appstore_12345\",\"type\":\"TEXT\",\"content\":\"{\\\"cmd\\\":\\\"25\\\",\\\"param\\\":\\\"\\\",\\\"type\\\":\\\"KEY_EVENT\\\"}\",\"extra\":{},\"reply\":false}";
            BleClientManager.instance(this).sendCmd(down);
        } else if (id == R.id.btn_info) {/*{
                    "proto": "TVDeviceInfo",   //内容固定
                    "deviceInfo": "",  //内容为空
                    "session": ""  //设置手机本机的session
                }*/
            String info = "{\"proto\": \"TVDeviceInfo\",\"deviceInfo\": \"\",\"session\": \"{\\\"id\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.130.135:34000\\\",\\\"address-local\\\":\\\"172.20.130.135\\\",\\\"stream-local\\\":\\\"172.20.130.135\\\",\\\"im-cloud\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\"}}\"}";
            //String info = "{\"proto\": \"TVDeviceInfo\",\"deviceInfo\": \"{\\\"activeId\\\":\\\"86350099\\\",\\\"deviceName\\\":\\\"tb8788p1_64_bsp\\\",\\\"mChip\\\":\\\"3C02\\\",\\\"mModel\\\":\\\"AIP10A\\\",\\\"mSize\\\":\\\"10\\\",\\\"mUserId\\\":\\\"60f9f3c97e084a258d966ded3ae5f31b\\\",\\\"mNickName\\\":\\\"Mister丶蓝\\\",\\\"clazzName\\\":\\\"swaiotos.channel.iot.ss.device.PadDeviceInfo\\\"}\",\"session\": \"{\\\"id\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.130.135:34000\\\",\\\"address-local\\\":\\\"172.20.130.135\\\",\\\"stream-local\\\":\\\"172.20.130.135\\\",\\\"im-cloud\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\"}}\"}";
            BleClientManager.instance(this).reqSession(info, new BleClientManager.SessionCallBack() {
                @Override
                public void onSession(Device<TVDeviceInfo> device) {
                    tv_info.setText("请求session成功---" + device.getLsid());
                }
            });
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

}
