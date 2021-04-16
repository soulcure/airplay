package swaiotos.runtime.h5;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.Iterator;

import swaiotos.runtime.h5.common.util.LogUtil;

/**
 * @ClassName: H5CoreExt
 * @Author: lu
 * @CreateDate: 2020/11/13 5:38 PM
 * @Description:
 */
public class H5CoreExt implements Serializable {
    public static final String ON_RECEIVE = "onReceive";
    private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());
    private static final String NATIVE2JS = "javascript:callbackFromNative(\"%s\",%s)";

    protected final static String RET_SUCCESS = "success";
    protected final static String RET_FAIL = "fail";

    private WebView mWebView;
    protected static Context context;

    public final void setWebView(WebView webView) {
        this.mWebView = webView;
    }

    public final WebView getWebView() {
        return mWebView;
    }

    public void setContext(Context c) {
        if(context == null) {
            context = c.getApplicationContext();
        }
    }

    public void attach(Context context) {

    }

    public void detach(Context context) {
        if(mWebView != null && mWebView.getContext() == context) {
            LogUtil.androidLog("detach context : " + context + ", ext=" + this);
            mWebView = null;
        }
    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void onStop() {

    }

    protected final void native2js(String id, String name, String params) {
        JSONObject object = new JSONObject();
        object.put("methodName", name);
        object.put("res", params);
        String native2js = String.format(NATIVE2JS, id, object.toString());
        LogUtil.androidLog("callbackFromNative(), evaluateJavascript---- " + native2js);
        if (mWebView != null) {
            UI_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    if(mWebView != null) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                            mWebView.evaluateJavascript(native2js, null);
                        } else {
                            mWebView.loadUrl(native2js);
                        }
                    }
                }
            });
        }
    }
}
