package com.coocaa.tvpi.module.live;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.coocaa.publib.base.BaseAppletActivity;
import com.coocaa.smartscreen.repository.http.HttpRequest;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;

/**
 * @Author: yuzhan
 */
public class LiveH5Activity extends BaseAppletActivity {
    FrameLayout layout;
    WebView webView;

    private final String URL = "http://m.91kds.cn/index.html";
    private final String JS = "http://beta.webapp.skysrt.com/lxw/ceshi/livePlugin.js";
    private String curUrl = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "LiveH5";
        Log.d("chen", "onCreate: ");
        layout = new FrameLayout(this);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(layout);
        initWebView();

    }

    private void initWebView() {
        webView = new WebView(this);
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.addView(webView);
        webView.setVisibility(View.INVISIBLE);

        WebSettings webSettings = webView.getSettings();
        // 设置与Js交互的权限
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(0);
        // 设置允许JS弹窗
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebChromeClient(new WebChromeClient() {

        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "onPageFinished, url=" + url);
                if(!TextUtils.equals(url, curUrl) && !url.contains(".js")) {
                    curUrl = url;
                    Log.d(TAG, "start load js : " + JS);
//                    StringBuilder sb = new StringBuilder("javascript:(function() {console.log('**********************test js**********************');");
//                    sb.append("var headerDom = document.getElementsByClassName('headerNfooter')[0];");
//                    sb.append("headerDom.parentNode.removeChild(headerDom);");
//                    sb.append("})()");
//                    String js = sb.toString();
//                    Log.d(TAG, "real start load js : " + js);
//                    webView.loadUrl(js);
                    HomeIOThread.execute(new Runnable() {
                        @Override
                        public void run() {
                            String js = HttpRequest.requestBodySync(JS);
                            Log.d(TAG, "js http content : " + js);
                            StringBuilder sb = new StringBuilder("javascript:(function() {");
                            sb.append(js);
                            sb.append("})()");
                            final String realJs = sb.toString();
                            HomeUIThread.execute(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "real start load js : " + realJs);
                                    webView.loadUrl(realJs);
                                    HomeUIThread.execute(1000, new Runnable() {
                                        @Override
                                        public void run() {
                                            webView.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        });

//        HomeUIThread.execute(new Runnable() {
//            @Override
//            public void run() {
//                webView.loadUrl("javascript:" + JS);
//            }
//        });
        webView.loadUrl(URL);
    }


}
