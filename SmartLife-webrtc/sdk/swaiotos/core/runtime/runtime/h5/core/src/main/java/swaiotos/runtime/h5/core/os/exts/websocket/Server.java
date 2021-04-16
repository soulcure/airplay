package swaiotos.runtime.h5.core.os.exts.websocket;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSONObject;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import swaiotos.runtime.h5.H5CoreExt;

/**
 * @ClassName: Server
 * @Author: lu
 * @CreateDate: 11/30/20 6:50 PM
 * @Description:
 */

public class Server extends H5CoreExt {
    private WebSocketServer mWebSocketServer;
    private Context mContext;
    private String mId;

    private final Map<String, Socket> mSockets = new HashMap<>();

    Server(Context context) {
        Log.d("h5ext", "Server " + this);
        mContext = context;
    }

    @JavascriptInterface
    public void setCallback(String id) {
        Log.d("h5ext", "setCallback " + id);
        mId = id;
    }

    @JavascriptInterface
    public synchronized void listen() {
        Log.d("h5ext", " listen");
        listen(getRandomPort());
    }

    @JavascriptInterface
    public synchronized void listen(int port) {
        Log.d("h5ext", "listen " + port);
        close();
        mWebSocketServer = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                synchronized (mSockets) {
                    String address = Socket.getRemoteAddress(mWebSocketServer, conn);
                    Socket socket = mSockets.get(address);
                    if (socket != null) {
                        mSockets.remove(address);
                    }
                    if (socket == null) {
                        socket = new Socket(mWebSocketServer, conn);
                        socket.setWebView(getWebView());
                        mSockets.put(address, socket);
                    }

                    JSONObject object = new JSONObject();
                    object.put("socket", address);
                    native2js(mId, "onOpen", object.toJSONString());
                }
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                synchronized (mSockets) {
                    String address = Socket.getRemoteAddress(mWebSocketServer, conn);
                    Socket socket = mSockets.get(address);
                    if (socket != null) {
                        mSockets.remove(address);
                    }

                    JSONObject object = new JSONObject();
                    object.put("socket", address);
                    object.put("code", code);
                    object.put("reason", reason);
                    object.put("remote", remote);
                    native2js(mId, "onClose", object.toJSONString());
                }
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                String socket = Socket.getRemoteAddress(mWebSocketServer, conn);
                JSONObject object = new JSONObject();
                object.put("socket", socket);
                object.put("message", message);
                native2js(mId, "onMessage", object.toJSONString());
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                String socket = Socket.getRemoteAddress(mWebSocketServer, conn);
                JSONObject object = new JSONObject();
                object.put("socket", socket);
                object.put("ex", ex.getMessage());
                native2js(mId, "onError", object.toJSONString());
            }

            @Override
            public void onStart() {
                String address = DeviceUtil.getLocalIPAddress(mContext) + ":" + getPort();
                JSONObject object = new JSONObject();
                object.put("address", address);
                native2js(mId, "onStart", object.toJSONString());
            }
        };
        mWebSocketServer.start();
    }

    @JavascriptInterface
    public synchronized void close() {
        try {
            if (mWebSocketServer != null) {
                mWebSocketServer.stop();
                mWebSocketServer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public Socket getSocket(String socket) {
        synchronized (mSockets) {
            return mSockets.get(socket);
        }
    }

    private synchronized static int getRandomPort() {
        int mLocalPort = 0;
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(0);
            mLocalPort = ss.getLocalPort();
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (ss != null) {
                    ss.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return mLocalPort;
    }
}
