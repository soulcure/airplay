package com.skyworth.dpclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.skyworth.dpclientsdk.ConnectState;
import com.skyworth.dpclientsdk.ResponseCallback;
import com.skyworth.dpclientsdk.WebSocketClient;

public class WebSocketClientActivity extends AppCompatActivity implements View.OnClickListener {
    WebSocketClient test2;
    ResponseCallback callback2;
    EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_websocket_client);

        input = findViewById(R.id.editText);
        //input.setText(DeviceUtil.getLocalIPAddress(this));
        input.setText("192.168.31.221");

        Button button = findViewById(R.id.button_client_start);
        Button button2 = findViewById(R.id.button_client_send);
        button.setOnClickListener(this);
        button2.setOnClickListener(this);

        //命令发送端回调，用于接收命令的返回消息
        callback2 = new ResponseCallback(){
            @Override
            public void onCommand(String cmd) {
                Log.d("client", "命令发送成功，收到返回：" + cmd);
            }
            @Override
            public void onCommand(byte[] data) {
                //TODO
            }
            @Override
            public void onConnectState(ConnectState state) {
                Log.d("client", "连接状态：" + state);
            }

            @Override
            public void ping(String cmd) {
                Log.d("client", "命令发送成功，ping：" + cmd);
            }

            @Override
            public void pong(String cmd) {
                Log.d("client", "命令发送成功，pong：" + cmd);
            }


        };
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_client_start:
                String ip = input.getText().toString();
                //发送命令
                //实力化一个命令发送端，参数为接受端的IP地址和端口号。这个会和发现服务（云端or局域网）对接，由发现服务提供
                test2 = new WebSocketClient(ip, 21095, callback2);
                //打开发送通道
                test2.open();
                break;
            case R.id.button_client_send:
                test2.ping("this ping message");
                //test2.pong("this pong message");
                break;
        }
    }
}
