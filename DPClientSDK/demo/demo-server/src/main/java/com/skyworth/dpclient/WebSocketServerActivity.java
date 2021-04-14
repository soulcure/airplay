package com.skyworth.dpclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.skyworth.dpclientsdk.ConnectState;
import com.skyworth.dpclientsdk.RequestCallback;
import com.skyworth.dpclientsdk.WebSocketServer;

public class WebSocketServerActivity extends AppCompatActivity implements View.OnClickListener {
    WebSocketServer test;
    RequestCallback callback1;
    int sockeId;
    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_websocket_server);

        input = findViewById(R.id.ip_edit);
        input.setText(DeviceUtil.getLocalIPAddress(this));
        //input.setText("192.168.137.227");

        //命令接收回调
        callback1 = new RequestCallback() {
            @Override
            public void onRead(int i, String s) {
                Log.d("server", "收到设备 onRead " + i + " 发来的命令: " + s);
                //命令返回
                test.sendData(i, "bbbbbbbbbbbb");

            }

            @Override
            public void onRead(int socketId, byte[] data) {

            }

            @Override
            public void onConnectState(int socketId, ConnectState state) {
                Log.d("server", "id: " + socketId + " 连接状态：" + state);
            }

            @Override
            public void ping(int socketId, String cmd) {
                sockeId = socketId;
                Log.d("server", "收到设备 ping" + socketId + " 发来的命令: " + cmd);
            }

            @Override
            public void pong(int socketId, String cmd) {
                sockeId = socketId;
                Log.d("server", "收到设备 pong" + socketId + " 发来的命令: " + cmd);
            }
        };

        Button button = findViewById(R.id.button_server_start);
        button.setOnClickListener(this);

        Button button2 = findViewById(R.id.button_server_stop);
        button2.setOnClickListener(this);

        Button button3 = findViewById(R.id.button_ping);
        button3.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_server_start:
                //实例化一个命令接收端，端口号暂时写死
                test = new WebSocketServer(21095, callback1);
                //打开通道等待连接
                test.open();
                break;
            case R.id.button_server_stop:
                test.close();
                test = null;
                break;
            case R.id.button_ping:
                test.ping(sockeId, "server ping");
                break;

        }
    }
}
