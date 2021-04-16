package swaiotos.runtime.h5.core.os.webview;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import org.greenrobot.eventbus.EventBus;

import swaiotos.runtime.h5.common.event.UrlLoadFinishedEvent;
import swaiotos.runtime.h5.common.util.LogUtil;
import swaiotos.runtime.h5.core.os.H5CoreOS;

public class TVAppletWebViewClient  extends LoadingStateWebViewClient {

    TVWebViewLoading mLoadingView;

    boolean loadErr = false;
    public TVAppletWebViewClient(TVWebViewLoading tvLoadingListener) {
        mLoadingView = tvLoadingListener;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        LogUtil.androidLog(H5CoreOS.TAG, "======== onLoadUrl   =======");
        LogUtil.androidLog(H5CoreOS.TAG, "Url=" + url);
        try {
            if (url.startsWith("http:") || url.startsWith("https:")) {
                view.loadUrl(url);
            } else {
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                mContext.startActivity(intent);
            }
            return true;
        } catch (Exception e){
            return false;
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        LogUtil.androidLog(H5CoreOS.TAG, "======== onPageStarted   =======");
        loadErr = false;
        isLoadOk = false;
        if(mLoadingView!=null){
            mLoadingView.showLoadingView();
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        LogUtil.androidLog(H5CoreOS.TAG, "======== onPageFinished   =======");
        LogUtil.androidLog(H5CoreOS.TAG, "finish url->" + (TextUtils.isEmpty(url) ? "" : url));
        isLoadOk = true;
        UrlLoadFinishedEvent event = new UrlLoadFinishedEvent();
        event.setData(url);
        EventBus.getDefault().post(event);

        if(mLoadingView!=null && !loadErr){
            mLoadingView.dismissLoadingView();
        }
        loadErr = false;
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        LogUtil.androidLog(H5CoreOS.TAG, "======== onReceivedError   =======");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && request.getUrl() != null) {
            LogUtil.androidLog(H5CoreOS.TAG,"url: " + request.getUrl().toString());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && error.getDescription() != null) {
            LogUtil.androidLog(H5CoreOS.TAG, "desc: " + error.getDescription().toString());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int errorCode = error.getErrorCode();
            if (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT || errorCode == ERROR_TIMEOUT) {
                loadErr = true;
                if(mLoadingView!=null){
                    mLoadingView.showErrView();
                }
            }
        }

    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        LogUtil.androidLog(H5CoreOS.TAG, "======== onReceivedHttpError   =======");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && request.getUrl() != null) {
                LogUtil.androidLog(H5CoreOS.TAG,"url: " + request.getUrl().toString());
            }
            int statusCode = errorResponse.getStatusCode();
            if (404 == statusCode || 500 == statusCode) {
//                loadErr = true;
//                if(mLoadingView!=null){
//                    mLoadingView.showErrView();
//                }
            }
        }

    }
    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        LogUtil.d( "shouldOverrideKeyEvent() called with event = [" + event.toString() + "]");
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU || event.getKeyCode() == 962 ||  event.getKeyCode() == 852 ||  event.getKeyCode() == 851 || event.getKeyCode() == 850 || event.getKeyCode() == 769) {
            return true;
        }
        return super.shouldOverrideKeyEvent(view, event);
    }
}
