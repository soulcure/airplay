package swaiotos.runtime.h5.core.os.webview;

import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

import swaiotos.runtime.h5.BaseH5AppletActivity;
import swaiotos.runtime.h5.common.util.LogUtil;
import swaiotos.runtime.h5.core.os.H5CoreOS;

public class TVAppletWebViewChromeClient extends WebChromeClient {

    private BaseH5AppletActivity mActivity;

    private  TVWebViewLoading tvWebViewLoading;

    public TVAppletWebViewChromeClient(BaseH5AppletActivity activity,TVWebViewLoading loadingView) {
        this.mActivity = activity;
        this.tvWebViewLoading = loadingView;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        return super.onJsAlert(view, url, message, result);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        LogUtil.androidLog(H5CoreOS.TAG, "Website Title= " + title);
//            setTitle(title);

        if(tvWebViewLoading!=null){
            tvWebViewLoading.showTitle(title);
        }
// android 6.0 以下通过title获取
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (title.contains("404") || title.contains("500") || title.contains("Error")) {
//                view.loadUrl("about:blank");// 避免出现默认的错误界面
//                view.loadUrl(mErrorUrl);
                if(tvWebViewLoading!=null){
                    tvWebViewLoading.showErrView();
                }
            }
        }

    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        super.onShowCustomView(view, callback);
    }

    @Override
    public void onHideCustomView() {
        super.onHideCustomView();
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
//        if (newProgress == 100) {
//            mProgressBar.setVisibility(View.GONE);
//        } else {
//            if (mProgressBar.getVisibility() == View.GONE) {
//                mProgressBar.setVisibility(View.VISIBLE);
//            }
//            mProgressBar.setProgress(newProgress);
//        }
        super.onProgressChanged(view, newProgress);
    }

    // For 3.0+ Devices (Start)
    // onActivityResult attached before constructor
    public  void openFileChooser(ValueCallback uploadMsg, String acceptType)
    {
       mActivity.openFileChooser(uploadMsg,acceptType);
    }


    // For Lollipop 5.0+ Devices
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
    {
        return mActivity.onShowFileChooser(mWebView,filePathCallback,fileChooserParams);
    }

    //For Android 4.1 only
    protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
    {
        mActivity.openFileChooser(uploadMsg,acceptType,capture);
    }

    protected void openFileChooser(ValueCallback<Uri> uploadMsg)
    {
        mActivity.openFileChooser(uploadMsg);
    }
}
