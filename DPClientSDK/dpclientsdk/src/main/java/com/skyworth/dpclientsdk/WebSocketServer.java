package com.skyworth.dpclientsdk;

import android.util.Log;

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;

import java.net.BindException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebSocketServer {
    private static final String TAG = WebSocketServer.class.getSimpleName();

    private AsyncHttpServer mHttpServer;

    private int socketInc;
    private List<SocketPool> mConnects;

    private int mPort;
    private RequestCallback mDataCallback;


    /**
     * 交互回调
     */
    private AsyncHttpServer.WebSocketRequestCallback mRequestCallback =
            new AsyncHttpServer.WebSocketRequestCallback() {
                @Override
                public void onConnected(final WebSocket webSocket, AsyncHttpServerRequest request) {

                    final int socketIndex = addSocket(webSocket);

                    Log.d(TAG, "client onConnected ");

                    mDataCallback.onConnectState(socketIndex, ConnectState.CONNECT);

                    webSocket.setClosedCallback(new CompletedCallback() {
                        @Override
                        public void onCompleted(Exception ex) {
                            if (ex != null) {
                                ex.printStackTrace();
                            }
                            Log.e(TAG, "webSocket Close:" + socketIndex);
                            freeSocket(socketIndex);
                        }
                    });

                    webSocket.setEndCallback(new CompletedCallback() {
                        @Override
                        public void onCompleted(Exception ex) {
                            if (ex != null) {
                                ex.printStackTrace();
                            }
                            Log.e(TAG, "webSocket End:" + webSocket);
                            freeSocket(socketIndex);
                        }
                    });

                    webSocket.setDataCallback(new DataCallback() {
                        @Override
                        public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                            byte[] data = bb.getAllByteArray();
                            for (SocketPool connection : mConnects) {
                                if (connection.webSocket == webSocket) {
                                    mDataCallback.onRead(connection.socketId, data);
                                    break;
                                }
                            }
                        }
                    });

                    webSocket.setStringCallback(new WebSocket.StringCallback() {
                        @Override
                        public void onStringAvailable(String s) {
                            Log.d(TAG, "onStringAvailable:" + s);
                            for (SocketPool connection : mConnects) {
                                if (connection.webSocket == webSocket) {
                                    mDataCallback.onRead(connection.socketId, s);
                                    break;
                                }
                            }
                        }
                    });

                    webSocket.setPingCallback(new WebSocket.PingCallback() {
                        @Override
                        public void onPingReceived(String s) {
                            Log.d(TAG, "ping onStringAvailable:" + s);
                            for (SocketPool connection : mConnects) {
                                if (connection.webSocket == webSocket) {
                                    mDataCallback.ping(connection.socketId, s);
                                    break;
                                }
                            }
                        }
                    });

                    webSocket.setPongCallback(new WebSocket.PongCallback() {
                        @Override
                        public void onPongReceived(String s) {
                            Log.d(TAG, "pong onStringAvailable:" + s);
                            for (SocketPool connection : mConnects) {
                                if (connection.webSocket == webSocket) {
                                    mDataCallback.pong(connection.socketId, s);
                                    break;
                                }
                            }
                        }
                    });
                }
            };

    /**
     * 错误回调
     */
    private CompletedCallback mErrorCallback = new CompletedCallback() {
        public void onCompleted(Exception ex) {
            if (ex instanceof BindException) {
                Log.d(TAG, "WebSocketServer bind Address already in use");
                mDataCallback.onConnectState(-1, ConnectState.CONNECT);
            } else {
                ex.printStackTrace();
                Log.e(TAG, "WebSocketServer bind error:" + ex.getMessage());
                mDataCallback.onConnectState(-1, ConnectState.ERROR);
            }
        }
    };


    public WebSocketServer(int port, RequestCallback dataCallback) {
        mConnects = new CopyOnWriteArrayList<>();
        socketInc = 0;

        mPort = port;
        mDataCallback = dataCallback;
    }


    /**
     * 开启web socket server port监听
     */
    public void open() {
        Log.d(TAG, "open WebSocketServer");

        mHttpServer = new AsyncHttpServer();
        mHttpServer.websocket("/", null, mRequestCallback);
        mHttpServer.setErrorCallback(mErrorCallback);
        mHttpServer.listen(mPort);
    }


    /**
     * 关闭web socket
     */
    public void close() {
        Log.d(TAG, "close WebSocketServer");

        socketInc = 0;
        for (SocketPool item : mConnects) {
            item.clear();
        }
        mConnects.clear();

        if (mHttpServer != null) {
            mHttpServer.stop();
        }

        mHttpServer = null;
        mDataCallback = null;
    }


    /**
     * web socket server send string to client
     *
     * @param socketId 客户端连接句柄
     * @param data     发送字符串
     */
    public void sendData(int socketId, String data) {
        for (SocketPool connection : mConnects) {
            if (connection.socketId == socketId && connection.webSocket != null) {
                connection.webSocket.send(data);
                break;
            }
        }
    }


    /**
     * web socket server send bytes to client
     *
     * @param socketId 客户端连接句柄
     * @param data     发送bytes
     */
    public void sendData(int socketId, byte[] data) {
        for (SocketPool connection : mConnects) {
            if (connection.socketId == socketId && connection.webSocket != null) {
                connection.webSocket.send(data);
                break;
            }
        }
    }


    public void ping(int socketId, String data) {
        for (SocketPool connection : mConnects) {
            if (connection.socketId == socketId && connection.webSocket != null) {
                connection.webSocket.ping(data);
                break;
            }
        }
    }


    public void pong(int socketId, String data) {
        for (SocketPool connection : mConnects) {
            if (connection.socketId == socketId && connection.webSocket != null) {
                connection.webSocket.pong(data);
                break;
            }
        }
    }


    private int addSocket(final WebSocket webSocket) {
        Log.d(TAG, "addWebSocket start");

        SocketPool connection = new SocketPool();
        connection.socketId = socketInc++;
        connection.webSocket = webSocket;

        mConnects.add(connection);

        Log.d(TAG, "addWebSocket end");
        return connection.socketId;
    }

    private void freeSocket(int socketIndex) {
        Log.d(TAG, "freeSocket start:" + socketIndex);

        int delIndex = -1;
        for (int i = 0; i < mConnects.size(); i++) {
            SocketPool item = mConnects.get(i);

            if (item.socketId == socketIndex) {
                item.clear();
                mDataCallback.onConnectState(socketIndex, ConnectState.DISCONNECT);
                delIndex = i;
                break;
            }
        }

        if (delIndex != -1) {
            mConnects.remove(delIndex);
        }

        Log.d(TAG, "freeSocket end:" + socketIndex);
    }

    public static class SocketPool {
        int socketId;
        WebSocket webSocket;

        public void clear() {
            webSocket.close();
            webSocket = null;
            socketId = 0;
        }
    }
}
