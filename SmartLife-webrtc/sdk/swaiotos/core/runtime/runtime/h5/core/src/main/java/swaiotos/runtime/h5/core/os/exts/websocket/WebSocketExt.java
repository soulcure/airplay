package swaiotos.runtime.h5.core.os.exts.websocket;

import android.content.Context;
import android.webkit.JavascriptInterface;

import swaiotos.runtime.h5.H5CoreExt;

/**
 * @ClassName: WebSocketExt
 * @Author: lu
 * @CreateDate: 11/30/20 6:07 PM
 * @Description:
 */
public class WebSocketExt extends H5CoreExt {
    public static final String NAME = "websocket";

    private static H5CoreExt ext = null;

    public static synchronized H5CoreExt get(Context context) {
        if (ext == null) {
            ext = new WebSocketExt(context);
        }
        return ext;
    }

    private Context mContext;

    WebSocketExt(Context context) {
        mContext = context;
    }

    @JavascriptInterface
    public Server createServer() {
        Server server = new Server(mContext);
        server.setWebView(getWebView());
        return server;
    }
}
