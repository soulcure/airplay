package swaiotos.runtime.h5.core.os.exts.websocket;

import android.webkit.JavascriptInterface;

import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;

import swaiotos.runtime.h5.H5CoreExt;

/**
 * @ClassName: Socket
 * @Author: lu
 * @CreateDate: 11/30/20 7:53 PM
 * @Description:
 */
public class Socket extends H5CoreExt {
    public static String getRemoteAddress(WebSocketServer server, WebSocket webSocket) {
        return server.getRemoteSocketAddress(webSocket).toString();
    }

    private WebSocket mWebSocket;
    private WebSocketServer mServer;

    public Socket(WebSocketServer server, WebSocket webSocket) {
        mServer = server;
        mWebSocket = webSocket;
    }

    @JavascriptInterface
    public void sendText(String text) {
        mWebSocket.send(text);
    }

    @JavascriptInterface
    public String getRemoteAddress() {
        return getRemoteAddress(mServer, mWebSocket);
    }
}
