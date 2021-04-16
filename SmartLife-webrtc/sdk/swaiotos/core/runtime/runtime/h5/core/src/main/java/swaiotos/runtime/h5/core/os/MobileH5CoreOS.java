package swaiotos.runtime.h5.core.os;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;

import java.util.Map;
import java.util.regex.Pattern;

import swaiotos.runtime.base.WebMetaData;
import swaiotos.runtime.base.utils.ToastUtils;
import swaiotos.runtime.h5.H5CoreExt;
import swaiotos.runtime.h5.common.bean.SsePushBean;
import swaiotos.runtime.h5.core.os.webview.AppletWebViewClient;
import swaiotos.runtime.h5.core.os.webview.LoadingStateWebViewClient;

/**
 * @Author: yuzhan
 */
public class MobileH5CoreOS extends H5CoreOS implements AppletWebViewClient.IAppletWebViewClient {

    private final String TAG = "MobileH5CoreOS";

    public MobileH5CoreOS(H5RunType.RunType type, SsePushBean pushBean) {
        super(type, pushBean);
    }

    public MobileH5CoreOS(H5RunType.RunType type, SsePushBean pushBean, String id) {
        super(type, pushBean, id);
    }

    @Override
    public View create(Context context, Map<String, H5CoreExt> extension) {
        return super.create(context, extension);
    }

    @Override
    public void load(String url) {
        Log.d(TAG, "load url : " + url);
//        if(isSameUrl(url)) {
//            Log.d(TAG, "same url with current, not load.");
//            return ;
//        }
        super.load(url);
//        urlStack.push(url);
    }

    @Override
    public boolean onBackPressed() {
        Log.d(TAG, "onBackPressed, curUrl = " + mWebView.getUrl());
//        if(urlStack.isEmpty()) {
//            Log.d(TAG, "onBackPressed, empty url history.");
//            return false;
//        }
//        String url = urlStack.pop();
//        popUrl = url;
//        Log.d(TAG, "onBackPressed, pop url : " + url);
//        super.load(url);
        return super.onBackPressed();
    }

    private boolean isSameUrl(String url) {
        String curUrl = mWebView == null ? null : mWebView.getUrl();
        Log.d(TAG, "current url : " + curUrl);
        return TextUtils.equals(curUrl, url);
    }

    @Override
    protected LoadingStateWebViewClient createMobileWebViewClient(Context context) {
        AppletWebViewClient client = new AppletWebViewClient(context, rlayout, this.id);
        client.setListener(this);
        return client;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Log.d(TAG, "shouldOverrideUrlLoading 222 url=" + request.getUrl());
        if(request.getUrl().getScheme().equals("ccsmartscreen")) {
            Log.d(TAG, "no need start ccsmartscreen");
            ToastUtils.getInstance().showGlobalLong("已安装共享屏APP");
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d(TAG, "shouldOverrideUrlLoading url=" + url);
        return false;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.d(TAG, "onPageFinished : " + url);
        mWebView.evaluateJavascript("(function() { " +
                "var ret='';" +
                "var meta = document.getElementsByTagName('meta');\n" +
                "console.log('meta=' + meta);\n" +
                "for(var i=0; i<meta.length;i++) {" +
                "console.log('m[i]=' + meta[i]);\n" +
                "if('description' == meta[i].name) {ret = meta[i].content; break;}}\n" +
                "return ret;"+
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.d(TAG, "onReceiveValue : " + value);
                Pattern r = Pattern.compile("^\"\\s*\"$");
                if(TextUtils.isEmpty(value) || r.matcher(value).matches()) {
                    Log.d(TAG, "empty description");
                } else {
                    WebMetaData.putDescription(url, value);
                }
            }
        });
//        if(TextUtils.equals(popUrl, url)) {
//            return ;
//        }
//        if(!TextUtils.equals(url, urlStack.peek())) {
//            urlStack.push(url);
//        }
    }

    public void onControlBarVisibleChanged(boolean b) {
        if(sw != null) {
            sw.onControlBarVisibleChanged(b);
        }
    }
}
