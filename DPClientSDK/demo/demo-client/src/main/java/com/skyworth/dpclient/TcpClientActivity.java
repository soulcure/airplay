package com.skyworth.dpclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.skyworth.dpclientsdk.ConnectState;
import com.skyworth.dpclientsdk.TcpClient;
import com.skyworth.dpclientsdk.StreamSourceCallback;
import com.skyworth.dpclientsdk.local.LocalConnect;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;


public class TcpClientActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "client";

    private TcpClient client;

    private LocalConnect localConnect;

    private boolean mStop;
    private static final int HANDLER_THREAD_SEND_DATA = 100;

    private EditText input;
    private ProcessHandler mProcessHandler;
    private AtomicInteger atomicInteger = new AtomicInteger(0);
    private boolean mStutasBoolean = false;
    private int mCount = 0;

    private StreamSourceCallback mCallBack = new StreamSourceCallback() {
        @Override
        public void onConnectState(ConnectState state) {
            Log.d(TAG, "StreamSourceState onConnectState---" + state);
            showToast("StreamSourceState onConnectState---" + state);
        }

        @Override
        public void onData(String data) {

        }

        @Override
        public void onData(byte[] data) {

        }

        @Override
        public void ping(String msg) {
            Log.d(TAG, "ping msg:" + msg);
        }

        @Override
        public void pong(String msg) {
            atomicInteger.getAndDecrement();
            Log.d(TAG, "pong msg:" + msg + " atomicInteger:" + atomicInteger.get() + " mCount:" + mCount);

        }
    };

    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TcpClientActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_client);
        initHandler();

        input = findViewById(R.id.ip_edit);
        //input.setText(DeviceUtil.getLocalIPAddress(this));
        input.setText("172.20.146.157");
        //input.setText("192.168.50.202");
        //input.setText("192.168.3.22");

        findViewById(R.id.btn_open).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_send_bytes).setOnClickListener(this);
        findViewById(R.id.btn_send_str).setOnClickListener(this);
        findViewById(R.id.btn_send_ping).setOnClickListener(this);
        findViewById(R.id.btn_key_up).setOnClickListener(this);
        findViewById(R.id.btn_key_down).setOnClickListener(this);
        findViewById(R.id.btn_info).setOnClickListener(this);


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


    private void runTest() {

        InputStream inputStream = TcpClientActivity.this.getResources().openRawResource(R.raw.video_test);

        try {
            //ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[(1460 - 8) * 10];
            int n = 0;
            while ((n = inputStream.read(buffer)) != -1) {
                Log.e("lfzzz", "run:  length --- " + buffer.length);
                client.sendData(buffer);
            }

                /*byte[] b = new byte[]{9,9,9,9,9,9,9,9,9};
                client.sendData(b);*/

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (true) {
            return;
        }

        byte[] bbb = new byte[2000 * 1024];
        int frameIndex = 0;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (!mStop) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 2000 * 1024; i++) {
                bbb[i] = (byte) ((i + frameIndex) & 0xff);
//                    bbb[i] =(byte)( i&0xff);
            }
            String aaa = "aaaaaa";
            //byte [] bbb = aaa.getBytes();
            Log.d("cuixiyuan", "send data...");
            client.sendData(bbb);
            frameIndex++;
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_open:
                String ip = input.getText().toString();

                /*if (client == null) {
                    client = new TcpClient(ip, 34000, mCallBack);
                } else {
                    client.close();
                }

                client.open();
                mStutasBoolean = true;*/

                localConnect = new LocalConnect();
                localConnect.open(ip, new LocalConnect.ConnectCallBack() {
                    @Override
                    public void onConnect(int code, String msg) {
                        Log.e("yao", "LocalConnect ---" + code + "---" + msg);
                    }
                });

                break;
            case R.id.btn_close:
                if (client != null) {
                    client.close();
                    client = null;
                }

                mStutasBoolean = false;
                break;
            case R.id.btn_send_bytes:
                byte[] b = new byte[]{9, 9, 9, 9, 9, 9, 9, 9, 9};
                client.sendData(b);

                break;
            case R.id.btn_send_str:
                String str = "幼儿园";
                client.sendData(str);

                break;
            case R.id.btn_send_ping:
                final String str1 = "小朋友";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (mStutasBoolean) {

                            mCount++;
                            atomicInteger.getAndIncrement();

                            Log.d(TAG, "atomicInteger:" + atomicInteger.get() + " mCount:" + mCount);

                            client.ping(str1);

                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }


                        }

                    }
                }).start();
                break;
            case R.id.btn_key_up:
                String up = "{\"id\":\"a21239e1-1fa8-4dbb-8c5a-4959cd65eb5e\",\"source\":\"{\\\"id\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.130.135:34000\\\",\\\"address-local\\\":\\\"172.20.130.135\\\",\\\"stream-local\\\":\\\"172.20.130.135\\\",\\\"im-cloud\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\"}}\",\"target\":\"{\\\"id\\\":\\\"d0e69442ec094a918ba1d06699b537e5\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.146.231:34000\\\",\\\"address-local\\\":\\\"172.20.146.231\\\",\\\"stream-local\\\":\\\"172.20.146.231\\\",\\\"im-cloud\\\":\\\"d0e69442ec094a918ba1d06699b537e5\\\"}}\",\"client-source\":\"ss-clientID-SmartScreen\",\"client-target\":\"ss-clientID-appstore_12345\",\"type\":\"TEXT\",\"content\":\"{\\\"cmd\\\":\\\"24\\\",\\\"param\\\":\\\"\\\",\\\"type\\\":\\\"KEY_EVENT\\\"}\",\"extra\":{},\"reply\":false}";
                //client.sendData(up.getBytes());

                localConnect.sendCommand(up, new LocalConnect.SendCallBack() {
                    @Override
                    public void onReceive(String msg) {
                        Log.e("yao", "localConnect sendMsg onReceive---" + msg);
                    }
                });

                break;
            case R.id.btn_key_down:
                String down = "{\"id\":\"a21239e1-1fa8-4dbb-8c5a-4959cd65eb5e\",\"source\":\"{\\\"id\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.130.135:34000\\\",\\\"address-local\\\":\\\"172.20.130.135\\\",\\\"stream-local\\\":\\\"172.20.130.135\\\",\\\"im-cloud\\\":\\\"baba946e63f7404cbb976abcbbb145d1\\\"}}\",\"target\":\"{\\\"id\\\":\\\"d0e69442ec094a918ba1d06699b537e5\\\",\\\"extra\\\":{\\\"im-local\\\":\\\"172.20.146.231:34000\\\",\\\"address-local\\\":\\\"172.20.146.231\\\",\\\"stream-local\\\":\\\"172.20.146.231\\\",\\\"im-cloud\\\":\\\"d0e69442ec094a918ba1d06699b537e5\\\"}}\",\"client-source\":\"ss-clientID-SmartScreen\",\"client-target\":\"ss-clientID-appstore_12345\",\"type\":\"TEXT\",\"content\":\"{\\\"cmd\\\":\\\"25\\\",\\\"param\\\":\\\"\\\",\\\"type\\\":\\\"KEY_EVENT\\\"}\",\"extra\":{},\"reply\":false}";
                //client.sendData(down.getBytes());

                localConnect.sendCommand(down, new LocalConnect.SendCallBack() {
                    @Override
                    public void onReceive(String msg) {
                        Log.e("yao", "localConnect sendMsg onReceive---" + msg);
                    }
                });
                break;

            case R.id.btn_info:
                localConnect.reqDeviceInfo(new LocalConnect.SendCallBack() {
                    @Override
                    public void onReceive(String msg) {

                        try {
                            JSONObject jsonObject = new JSONObject(msg);
                            int code = jsonObject.optInt("code");
                            String message = jsonObject.optString("msg");
                            String proto = jsonObject.optString("proto");
                            String device = jsonObject.optString("device");

                            if (code == 0
                                    && !TextUtils.isEmpty(proto)
                                    && !TextUtils.isEmpty(device)
                                    && proto.equals("TVDeviceInfo")) {

                                Gson gson = new Gson();
                                TVDeviceInfo t = gson.fromJson(device, TVDeviceInfo.class);
                                Log.e("yao", "reqDeviceInfo---" + t.toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;

        }
    }


    private byte[] InputStream2ByteArray(String filePath) throws IOException {

        InputStream in = new FileInputStream(filePath);
        byte[] data = toByteArray(in);
        in.close();

        return data;
    }

    private byte[] toByteArray(InputStream in) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[(1460 - 8) * 10];
        int n = 0;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
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
                case HANDLER_THREAD_SEND_DATA:
                    runTest();
                    break;
                default:
                    break;
            }

        }

    }
}
