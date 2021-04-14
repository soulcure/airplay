package com.skyworth.dpclient;


import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.skyworth.dpclientsdk.ble.BlePdu;
//import com.skyworth.dpclientsdk.ble.BluetoothServer;
import com.skyworth.dpclientsdk.ble.BluetoothServer;
import com.skyworth.dpclientsdk.ble.BluetoothServerCallBack;
import com.skyworth.dpclientsdk.MACUtils;
import com.skyworth.dpclientsdk.ble.BluetoothServerImp;
import com.skyworth.dpclientsdk.ble.IBluetoothServer;

public class FastBleServerActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "server";
    //    private BluetoothServer bleServer;
    private IBluetoothServer bleServer;
    private static final String PERFIX_SERVICE_UUID = "9ee1cf33-8819-1305-";//"9ee1cf33-a561-4c73-8522-b3029986eb3f";
//    private static final String MAC = "-b3029986eb3f";//"9ee1cf33-a561-4c73-8522-b3029986eb3f";

    private TextView tv_msg;
    private TextView tv_info;
    private TextView input;
    private String mac;
    private BluetoothDevice mBluetoothDevice;
    enum DongleStatus {
        NotConfig,
        DoConfig,
        ConfigOk,
        ConfigFail,
        Unbind,
        Binded,

    };
    private DongleStatus status = DongleStatus.NotConfig;

    BluetoothServerCallBack callBack = new BluetoothServerCallBack() {

        @Override
        public void onMessageShow(BlePdu blePdu, BluetoothDevice device) {
            mBluetoothDevice = device;
            tv_msg.setText(new String(blePdu.body));
        }

        @Override
        public void onStartSuccess(String deviceName) {
            tv_info.setText("开启ble广播成功:" + mac);
            input.setText(deviceName);
        }

        @Override
        public void onStartFail(String info) {
            tv_info.setText(info + "&mac:" + mac);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fast_ble_server);
        mac = MACUtils.getMac(this).replace(":", "").toLowerCase();

        input = findViewById(R.id.ip_edit);
        input.setText(mac);

        tv_msg = findViewById(R.id.tv_msg);
        tv_info = findViewById(R.id.tv_info);

        findViewById(R.id.btn_server_open).setOnClickListener(this);
        findViewById(R.id.btn_server_close).setOnClickListener(this);
        findViewById(R.id.btn_response).setOnClickListener(this);
        findViewById(R.id.btn_update).setOnClickListener(this);
        bleServer = new BluetoothServerImp(this, callBack);
//        bleServer = new BluetoothServer(this, callBack);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        String str = "";
        switch (view.getId()) {
            case R.id.btn_server_open:
                str = status.toString();
                bleServer.openBle();
                break;
            case R.id.btn_server_close:
                if (bleServer != null) {
                    bleServer.removeService();
                }
                break;
            case R.id.btn_update:
                int next = status.ordinal() + 1;
                if (DongleStatus.values().length <= next) {
                    next = 0;
                }
                status = DongleStatus.values()[next];
                str = status.toString();
                Log.d(TAG, "onClick() called with: status = [" + str + "]");
                tv_info.setText(str);
                bleServer.setCustomData("FFFFFF0" + next);
                break;
            case R.id.btn_response:
                responseMsg();
                break;
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void responseMsg() {
        String msg = "{\n" +
                "    \"id\":\"EFC91A36-F545-4053-A91C-C367C0155D57\",\n" +
                "    \"client-source\":\"ss-clientID-mobile-iphone\",\n" +
                "    \"client-target\":\"ss-clientID-appstore_12345\",\n" +
                "    \"type\":\"TEXT\",\n" +
                "    \"source\":\"{\"id\":\"47377d7249a042889545d8421249f308\",\"extra\":{\"im-cloud\":\"47377d7249a042889545d8421249f308\",\"stream-local\":\"10.136.108.230\",\"address-local\":\"10.136.108.230\",\"im-local\":\"10.136.108.230:34000\"}}\",\n" +
                "    \"extra\":{\n" +
                "        \"force-sse\":\"true\"\n" +
                "    },\n" +
                "    \"content\":\"{\"cmd\":\"\",\"type\":\"SCREEN_SHOT\",\"param\":\"\"}\",\n" +
                "    \"reply\":false,\n" +
                "    \"target\":\"{\"id\":\"de17ec2f155f4687b78052ea65943004\",\"extra\":{}}\"\n" +
                "}";
        if (mBluetoothDevice != null)
            bleServer.sendMessage(msg, BlePdu.TEMP_CMD, mBluetoothDevice);
    }


}