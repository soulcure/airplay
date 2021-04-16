package com.coocaa.tvpi.module.web;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.util.regex.Pattern;

public class WebViewInfoResolver {

    public interface IWebInfoCallback {
        void onWebIconLoaded(String url, Bitmap bitmap);
        void onWebTitleLoaded(String url, String title);
        void onWebDescriptionLoaded(String url, String description);
        boolean shouldOverrideUrlLoading(String url);
        void onLoadResource(String url);
        void onPageFinished(String url);
    }


    private WebView webView;
    private IWebInfoCallback callback;
    private String url;

    private final String TAG = "SmartWebInfo";

    public WebViewInfoResolver(WebView webView, IWebInfoCallback callback) {
        this.webView = webView;
        this.callback = callback;

        initWebSetting();
        initWebView();
    }

    private void initWebView() {
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView webView, String s) {
                Log.d(TAG, "onReceivedTitle, url=" + url + ", title=" + s);
                if(callback != null) {
                    callback.onWebTitleLoaded(url, s);
                }
                super.onReceivedTitle(webView, s);
            }

            @Override
            public void onReceivedIcon(WebView webView, Bitmap bitmap) {
                Log.d(TAG, "onReceivedIcon, url=" + url);
                if(callback != null) {
                    callback.onWebIconLoaded(url, bitmap);
                }
                super.onReceivedIcon(webView, bitmap);
            }
        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
                Log.d(TAG, "onPageStarted, url=" + s);
                url = s;
                super.onPageStarted(webView, s, bitmap);
            }

            @Override
            public void onPageFinished(WebView webView, String s) {
                Log.d(TAG, "onPageFinished, url=" + s);
                if(callback != null) {
                    callback.onPageFinished(s);
                }
                webView.evaluateJavascript("(function() { " +
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
                        Log.d(TAG, "retrieve description, onReceiveValue : " + value + ", url=" + url);
                        Pattern r = Pattern.compile("^\"\\s*\"$");
                        if(TextUtils.isEmpty(value) || r.matcher(value).matches()) {
                            Log.d(TAG, "empty description");
                        } else {
                            if(callback != null) {
                                callback.onWebDescriptionLoaded(url, value);
                            }
                        }
                    }
                });
                super.onPageFinished(webView, s);
            }

            @Override
            public void onLoadResource(WebView webView, String s) {
                Log.d(TAG, "onLoadResource : " + webView.getUrl());
                if(callback != null) {
                    callback.onLoadResource(webView.getUrl());
                }
                super.onLoadResource(webView, s);
            }

            @Override
            public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
                sslErrorHandler.proceed();// 接受所有网站的证书
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
                Log.d(TAG, "shouldOverrideUrlLoading222 : " + webResourceRequest.getUrl());
                if(callback != null) {
                    callback.shouldOverrideUrlLoading(webResourceRequest.getUrl().toString());
                }
                if(webResourceRequest.getUrl().toString().toLowerCase().startsWith("http")) {
                    return false;
                }
                return true;
//                return super.shouldOverrideUrlLoading(webView, webResourceRequest);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String s) {
                Log.d(TAG, "shouldOverrideUrlLoading111 : " + s);
                if(callback != null) {
                    callback.shouldOverrideUrlLoading(s);
                }
                if(s.toLowerCase().startsWith("http")) {
                    return false;
                }
                return true;
//                return super.shouldOverrideUrlLoading(webView, s);
            }
        });
    }

    private void initWebSetting() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setBuiltInZoomControls(false); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(true); //隐藏原生的缩放控件
        webSettings.setBlockNetworkImage(false);//解决图片不显示
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = webView.getContext().getCacheDir().getAbsolutePath();
        webSettings.setAppCachePath(appCachePath);
        webSettings.setAllowFileAccess(true);    // 可以读取文件缓存
        webSettings.setAppCacheEnabled(true);    //开启H5(APPCache)缓存功能
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMixedContentMode(WebSettings.LOAD_NORMAL);     // https下访问http资源
        //开启后前进后退将不再重新加载页面
//        webView.getSettingsExtension().setContentCacheEnable(true);
    }


}
