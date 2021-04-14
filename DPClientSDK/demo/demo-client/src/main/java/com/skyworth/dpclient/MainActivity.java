package com.skyworth.dpclient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_PERMISSION_LOCATION = 2;
    public static final int REQUEST_PERMISSION_WRITE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_websocket).setOnClickListener(this);
        findViewById(R.id.btn_tcp).setOnClickListener(this);
        findViewById(R.id.btn_udp).setOnClickListener(this);
        findViewById(R.id.btn_ble).setOnClickListener(this);
        findViewById(R.id.btn_new_ble).setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_websocket:
                startActivity(new Intent(this, WebSocketClientActivity.class));
                break;
            case R.id.btn_tcp:
                startActivity(new Intent(this, TcpClientActivity.class));
                break;
            case R.id.btn_udp:
                startActivity(new Intent(this, UdpClientActivity.class));
                break;
            case R.id.btn_ble:
                startActivity(new Intent(this, BleClientActivity.class));
                break;
            case R.id.btn_new_ble:
                startActivity(new Intent(this, BleClientNewActivity.class));

        }
    }
}
