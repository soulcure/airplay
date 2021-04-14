package com.skyworth.dpclientsdk.local;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.skyworth.dpclientsdk.ConnectState;
import com.skyworth.dpclientsdk.StreamSourceCallback;
import com.skyworth.dpclientsdk.TcpClient;

import org.json.JSONException;
import org.json.JSONObject;

public class LocalConnect {
    private static final String TAG = "yao";


    public interface ConnectCallBack {
        void onConnect(int code, String msg);
    }

    public interface SendCallBack {
        void onReceive(String msg);
    }

    private TcpClient tcpClient;
    private ConnectCallBack mConnectCallBack;
    private SendCallBack mSendCallBack;

    private Handler mHandler;

    public LocalConnect() {
        mHandler = new Handler(Looper.getMainLooper());
    }


    private StreamSourceCallback mCallBack = new StreamSourceCallback() {
        @Override
        public void onConnectState(final ConnectState state) {
            Log.d(TAG, "StreamSourceState onConnectState---" + state);
            if (mConnectCallBack != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (state == ConnectState.CONNECT) {
                            mConnectCallBack.onConnect(0, "connect success");
                        } else {
                            mConnectCallBack.onConnect(-1, "connect fail");
                        }
                    }
                });
            }
        }

        @Override
        public void onData(final String data) {
            if (mSendCallBack != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSendCallBack.onReceive(data);
                    }
                });
            }

        }

        @Override
        public void onData(final byte[] data) {
            if (mSendCallBack != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String str = new String(data);
                        mSendCallBack.onReceive(str);
                    }
                });
            }
        }

        @Override
        public void ping(String msg) {
            Log.d(TAG, "ping msg:" + msg);
        }

        @Override
        public void pong(String msg) {
            Log.d(TAG, "pong msg:" + msg);

        }
    };

    public void open(String ip, ConnectCallBack callBack) {
        this.mConnectCallBack = callBack;

        if (tcpClient == null) {
            tcpClient = new TcpClient(ip, 34000, mCallBack);
        } else {
            tcpClient.close();
        }
        tcpClient.open();
    }


    public void reqDeviceInfo(SendCallBack callBack) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("proto", "TVDeviceInfo");
            String json = jsonObject.toString();

            sendMsg(json, callBack);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void sendMsg(String msg, SendCallBack callBack) {
        this.mSendCallBack = callBack;

        if (tcpClient != null && tcpClient.isOpen()) {
            tcpClient.sendData(msg);
        }

    }


    public void sendCommand(String msg, SendCallBack callBack) {
        this.mSendCallBack = callBack;

        if (tcpClient != null && tcpClient.isOpen()) {
            tcpClient.sendData(msg.getBytes());
        }

    }

}
