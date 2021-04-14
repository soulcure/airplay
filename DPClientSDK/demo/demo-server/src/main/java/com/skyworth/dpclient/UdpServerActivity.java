package com.skyworth.dpclient;


import android.media.MediaCodec;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.skyworth.dpclientsdk.ConnectState;
import com.skyworth.dpclientsdk.StreamSinkCallback;
import com.skyworth.dpclientsdk.UdpServer;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class UdpServerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "server";

    private UdpServer udpServer;
    private TextView tv_msg;

    private StreamSinkCallback mCallback = new StreamSinkCallback() {
        @Override
        public void onData(byte[] data, SocketChannel channel) {
            Log.d(TAG, "onData:" + data.length);
        }

        @Override
        public void onData(String data, SocketChannel channel) {
            Log.d(TAG, "onData:" + data);
            showUI(data);
        }

        @Override
        public void onAudioFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer data, SocketChannel channel) {

        }

        @Override
        public void onVideoFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer data, SocketChannel channel) {

        }

        @Override
        public void ping(String msg, SocketChannel channel) {

        }

        @Override
        public void pong(String msg, SocketChannel channel) {

        }

        @Override
        public void onConnectState(ConnectState state) {
            Log.d(TAG, "onConnectState:" + state);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udp_server);

        tv_msg = findViewById(R.id.tv_msg);
        TextView tv_ip = findViewById(R.id.tv_ip);
        tv_ip.setText(DeviceUtil.getLocalIPAddress(this));

        findViewById(R.id.btn_open).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
    }


    private void showUI(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_msg.setText(msg);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_open:
                udpServer = new UdpServer(39999, mCallback);
                udpServer.open();
                break;
            case R.id.btn_close:
                if (udpServer != null) {
                    udpServer.close();
                    udpServer = null;
                }
                break;
        }
    }

}
