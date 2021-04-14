package com.skyworth.dpclient;


import android.media.MediaCodec;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.skyworth.dpclientsdk.ConnectState;
import com.skyworth.dpclientsdk.HexUtil;
import com.skyworth.dpclientsdk.StreamSinkCallback;
import com.skyworth.dpclientsdk.TcpServer;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TcpServerActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "server";

    TcpServer server;
    public int sinkIndex = 0;
    private long firstTime;
    private int count = 0;
    private long temp;

    private TextView input;

    StreamSinkCallback mCallback = new StreamSinkCallback() {
        @Override
        public void onData(String data, SocketChannel channel) {
            Log.d(TAG, "StreamSinkCallback onData:" + data);
        }

        @Override
        public void onData(byte[] data, SocketChannel channel) {
            Log.d(TAG, "StreamSinkCallback onData:" + HexUtil.bytes2HexString(data));
        }

        @Override
        public void onAudioFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer data, SocketChannel channel) {
            Log.d(TAG, "StreamSinkCallback onAudioFrame bufferInfo:");
        }

        @Override
        public void onVideoFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer data, SocketChannel channel) {
            Log.d(TAG, "StreamSinkCallback onVideoFrame bufferInfo:");
        }

        @Override
        public void ping(String msg, SocketChannel channel) {
            Log.d(TAG, "ping :" + msg);
        }

        @Override
        public void pong(String msg, SocketChannel channel) {
            Log.d(TAG, "pong :" + msg);
        }

        @Override
        public void onConnectState(ConnectState state) {
            Log.d(TAG, "onConnectState:" + state);
        }
    };

    private void checkData(byte[] data) {
        Log.e(TAG, "onData: lenght --- data --- " + data);
        count++;

        if (count >= 294) {
            Log.e(TAG, "onData: 共用时 --- " + (System.currentTimeMillis() - firstTime));
            count = 0;
            firstTime = 0;
            return;
        }

        if (firstTime == 0) {
            firstTime = System.currentTimeMillis();
            temp = firstTime;
            Log.e(TAG, "onData: 第" + count + " 帧数据收到时间 --- " + firstTime);
        } else {
            Log.e(TAG, "onData: 第" + count + "帧数据收到时间 --- " + System.currentTimeMillis());
            Log.e(TAG, "onData: 第 " + count + "帧数据传输用时 ---" + (System.currentTimeMillis() - temp));
            temp = System.currentTimeMillis();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_server);

        input = findViewById(R.id.ip_edit);
        input.setText(DeviceUtil.getLocalIPAddress(this));
        //input.setText("192.168.50.28");

        findViewById(R.id.btn_server_open).setOnClickListener(this);
        findViewById(R.id.btn_server_close).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_server_open:
                server = new TcpServer(34000, TcpServer.BUFFER_SIZE_DEFAULT, mCallback);
                server.open();
                break;
            case R.id.btn_server_close:
                if (server != null) {
                    server.close();
                    server = null;
                }
                break;
        }
    }

}
