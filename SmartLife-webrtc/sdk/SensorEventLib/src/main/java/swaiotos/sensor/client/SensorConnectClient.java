package swaiotos.sensor.client;

import android.hardware.SensorEvent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.alibaba.fastjson.JSON;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import swaiotos.sensor.connect.IConnectCallback;
import swaiotos.sensor.data.ClientCmdInfo;
import swaiotos.sensor.mgr.InfoManager;
import swaiotos.sensor.tm.TM;
import swaiotos.sensor.utils.MotionEventUtil;
import swaiotos.sensor.utils.SensorEventUtil;

/**
 * @Author: yuzhan
 */
public class SensorConnectClient implements IConnectClient {

    private OkHttpClient client;
    private Request request;
    private IConnectCallback callback;
    private WebSocket socket;
    private volatile boolean isConnect;
    private int reConnectCount = 0;
    private final int RE_CONNECT_LIMIT_COUNT = 5;
    private String clientId;
    private InfoManager infoManager;
    private ISmartApi smartApi;

    private final String TAG = "SSCClient";

    public SensorConnectClient(InfoManager infoManager) {
        this.clientId = infoManager.getBusinessInfo().clientSSId;
        client = new OkHttpClient.Builder()
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .pingInterval(10, TimeUnit.SECONDS)
                .build();

        this.infoManager = infoManager;
    }

    @Override
    public void setSmartApi(ISmartApi smartApi) {
        this.smartApi = smartApi;
    }

    @Override
    public boolean isConnected() {
        Log.d(TAG, "isConnected()=======" + (socket != null && isConnect));
        return socket != null && isConnect;
    }

    @Override
    public void connect(final String url, final IConnectCallback callback) {
        Log.d(TAG, "client call connect url=" + url);
        TM.Companion.ioSingle(new Runnable() {
            @Override
            public void run() {
                if (isConnected()) {
                    Log.d(TAG, "already connected.");
                    return;
                }
                Log.d(TAG, "real start connect : " + url);
                reConnectCount = 0;
                SensorConnectClient.this.callback = callback;

                request = new Request.Builder().url(url).addHeader("clientId", clientId).build();
                connect();
            }
        });
    }


    @Override
    public void disconnect() {
        Log.d(TAG, "client call disconnect, client=" + client);
        TM.Companion.ioSingle(new Runnable() {
            @Override
            public void run() {
                if (socket != null) {
                    try {
                        socket.close(1000, "client-stop");
                    } catch (Exception e) {
                        Log.d(TAG, "client call disconnect, error=" + e.toString());
                    }
                }
            }
        });
//        if(client != null) {
//            Log.d(TAG, "client.cache()=" + client.cache());
//            if(client.cache() != null) {
//                try {
//                    client.cache().close();
//                } catch (IOException e) {
//                    Log.d(TAG, "client close error : " + e.toString());
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    @Override
    public void send(final String text) {
        TM.Companion.ioSingle(new Runnable() {
            @Override
            public void run() {
                if (isConnected()) {
                    Log.d(TAG, "send : " + text);
                    socket.send(text);
                }
            }
        });
    }

    @Override
    public void sendMotionEvent(MotionEvent event, View v) {
        Log.d(TAG, "sendMotionEvent()=======getAction:" + event.getAction() + "---getActionMasked:" + event.getActionMasked());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                TM.Companion.ioSingle(new Runnable() {
                    @Override
                    public void run() {
                        if (smartApi != null && !smartApi.isSameWifi()) {
                            smartApi.startConnectSameWifi();
                        }
                    }
                });
                innerSendMotionEvent(event, v);
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                innerSendMotionEvent(event, v);
                break;
        }
    }

    private void innerSendMotionEvent(MotionEvent event, View v) {
        ClientCmdInfo info = ClientCmdInfo.build(infoManager, ClientCmdInfo.CMD_CLIENT_MOTION_EVENT, v);
        info.content = MotionEventUtil.formatTouchEvent(event);
        send(JSON.toJSONString(info));
    }

    @Override
    public void sendSensorEvent(SensorEvent event, View v) {
        ClientCmdInfo info = ClientCmdInfo.build(infoManager, ClientCmdInfo.CMD_CLIENT_SENSOR_EVENT, v);
        info.content = SensorEventUtil.formatSensorEvent(event);
        send(JSON.toJSONString(info));
    }

    private void connect() {
        TM.Companion.ioSingle(new Runnable() {
            @Override
            public void run() {
                if (isConnected()) {
                    Log.d(TAG, "already connected2.");
                    return;
                }
                WebSocket socket = client.newWebSocket(request, listener);
                Log.d(TAG, "client newWebSocket : " + socket);
            }
        });
    }

    private boolean reconnect() {
        if (reConnectCount > RE_CONNECT_LIMIT_COUNT) {
            Log.i(TAG, "reconnect over limit : " + RE_CONNECT_LIMIT_COUNT + ", please check url or network");
            return false;
        } else {
            TM.Companion.removeIO(connectRunnable);
            TM.Companion.io(connectRunnable, 1000);
            return true;
        }
    }

    private Runnable connectRunnable = new Runnable() {
        @Override
        public void run() {
            connect();
            reConnectCount++;
        }
    };

    private void onSocketConnect() {
        callback.onSuccess();
    }

    private void onSocketDisconnect() {
        callback.onClose();
    }

    private WebSocketListener listener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            Log.d(TAG, "connect, onOpen : " + response.toString() + ", webSocket=" + webSocket);
            socket = webSocket;
            isConnect = response.code() == 101;
            if (!isConnect) {
                reconnect();
            } else {
                onSocketConnect();
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d(TAG, "connect, onMessage : " + text);
            super.onMessage(webSocket, text);
            callback.onMessage(text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            super.onMessage(webSocket, bytes);
            onMessage(webSocket, bytes.base64());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            Log.d(TAG, "connect, onClosing : code=" + code + ", reason=" + reason);
            super.onClosing(webSocket, code, reason);
            socket = null;
            isConnect = false;
            onSocketDisconnect();
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            Log.d(TAG, "connect, onClosed : code=" + code + ", reason=" + reason);
            super.onClosed(webSocket, code, reason);
            socket = null;
            isConnect = false;
            onSocketDisconnect();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.d(TAG, "connect onFailure：" + response);
            super.onFailure(webSocket, t, response);
            isConnect = false;

            if (t != null) {
                t.printStackTrace();
            }
            callback.onFailOnce(t.toString());
            if (!reconnect()) {
                callback.onFail(t.toString());
            }
        }
    };
}
