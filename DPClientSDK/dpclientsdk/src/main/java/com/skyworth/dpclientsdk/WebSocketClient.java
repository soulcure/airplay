package com.skyworth.dpclientsdk;

import android.util.Log;

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

public class WebSocketClient {
    private static final String TAG = WebSocketClient.class.getSimpleName();

    private String mIP;
    private int mPort;
    private ResponseCallback mDataCallback;

    private WebSocket mWebSocket;

    private AsyncHttpClient.WebSocketConnectCallback mConnectCallBack =
            new AsyncHttpClient.WebSocketConnectCallback() {
                @Override
                public void onCompleted(Exception ex, WebSocket webSocket) {
                    if (ex != null) {
                        ex.printStackTrace();
                        Log.e(TAG, "WebSocket onCompleted: " + ex.getMessage());
                        mDataCallback.onConnectState(ConnectState.ERROR);
                        return;
                    }
                    mWebSocket = webSocket;
                    Log.i(TAG, "WebSocket Client OK");
                    mDataCallback.onConnectState(ConnectState.CONNECT);

                    mWebSocket.setClosedCallback(new CompletedCallback() {
                        @Override
                        public void onCompleted(Exception e) {
                            Log.i(TAG, "WebSocket Client Closed");
                            mWebSocket = null;
                            mDataCallback.onConnectState(ConnectState.DISCONNECT);
                        }
                    });

                    mWebSocket.setEndCallback(new CompletedCallback() {
                        @Override
                        public void onCompleted(Exception e) {
                            Log.i(TAG, "WebSocket Client End");
                            mWebSocket = null;
                            mDataCallback.onConnectState(ConnectState.DISCONNECT);
                        }
                    });

                    mWebSocket.setStringCallback(new WebSocket.StringCallback() {
                        public void onStringAvailable(final String s) {
                            Log.i(TAG, "WebSocket Client Receive String msg:" + s);
                            mDataCallback.onCommand(s);
                        }
                    });

                    mWebSocket.setDataCallback(new DataCallback() {
                        @Override
                        public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                            Log.i(TAG, "WebSocket Client Receive bytes");
                            byte[] data = bb.getAllByteArray();
                            mDataCallback.onCommand(data);
                        }
                    });

                    mWebSocket.setPingCallback(new WebSocket.PingCallback() {
                        @Override
                        public void onPingReceived(String s) {
                            Log.i(TAG, "WebSocket Client Receive ping msg:" + s);
                            mDataCallback.ping(s);
                        }
                    });

                    mWebSocket.setPongCallback(new WebSocket.PongCallback() {
                        @Override
                        public void onPongReceived(String s) {
                            Log.i(TAG, "WebSocket Client Receive pong msg:" + s);
                            mDataCallback.pong(s);
                        }
                    });
                }
            };


    public WebSocketClient(String ip, int port, ResponseCallback callback) {
        this.mIP = ip;
        this.mPort = port;
        this.mDataCallback = callback;
    }

    public void open() {
        Log.d(TAG, "openCommandChannel---" + mIP + ":" + mPort);
        AsyncHttpClient.getDefaultInstance().websocket(
                "ws://" + mIP + ":" + mPort, null, mConnectCallBack);
    }

    public void close() {
        Log.d(TAG, "closeCommandChannel");
        if (null != mWebSocket) {
            try {
                mWebSocket.close();
                mWebSocket = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void send(String s) {
        if (mWebSocket != null) {
            mWebSocket.send(s);
        } else {
            Log.e(TAG, "send string error!");
        }
    }

    public void send(byte[] bytes) {
        if (mWebSocket != null) {
            mWebSocket.send(bytes);
        } else {
            Log.e(TAG, "send bytes error!");
        }
    }

    public void ping(String s) {
        if (mWebSocket != null) {
            mWebSocket.ping(s);
        } else {
            Log.e(TAG, "ping error!");
        }
    }

    public void pong(String s) {
        if (mWebSocket != null) {
            mWebSocket.pong(s);
        } else {
            Log.e(TAG, "pong error!");
        }
    }

}
