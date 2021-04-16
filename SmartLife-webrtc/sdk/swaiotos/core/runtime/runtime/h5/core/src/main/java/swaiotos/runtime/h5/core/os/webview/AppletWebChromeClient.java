package swaiotos.runtime.h5.core.os.webview;

import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import swaiotos.runtime.h5.BaseH5AppletActivity;
import swaiotos.runtime.h5.R;
import swaiotos.runtime.h5.common.util.LogUtil;
import swaiotos.runtime.h5.core.os.H5CoreOS;


/**
 * @ClassName: AppletWebChromeClient
 * @Author: AwenZeng
 * @CreateDate: 2020/10/22 18:00
 * @Description:
 */
public class AppletWebChromeClient extends WebChromeClient {

//    private ImageView mProgressBar;
    private BaseH5AppletActivity mActivity;
    private RelativeLayout relativeLayout;

    public AppletWebChromeClient(BaseH5AppletActivity activity,RelativeLayout rLayout) {
        this.mActivity = activity;
        this.relativeLayout = rLayout;
    }

    public void resetLoadState(){
        if(relativeLayout!=null){
            TextView txtView = relativeLayout.findViewById(R.id.id_content_view);
            txtView.setVisibility(View.INVISIBLE);

            Button btnRetry = relativeLayout.findViewById(R.id.btn_retry);
            btnRetry.setVisibility(View.INVISIBLE);

            ImageView loadImageView = relativeLayout.findViewById(R.id.id_loading_view);
            loadImageView.setImageResource(R.drawable.net_error);
            loadImageView.setVisibility(View.INVISIBLE);
        }
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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (title.contains("404") || title.contains("500") || title.contains("Error")) {
//                view.loadUrl("about:blank");// 避免出现默认的错误界面
//                view.loadUrl(mErrorUrl);
                TextView txtView = relativeLayout.findViewById(R.id.id_content_view);
                txtView.setVisibility(View.VISIBLE);

                Button btnRetry = relativeLayout.findViewById(R.id.btn_retry);
                btnRetry.setVisibility(View.VISIBLE);

                ImageView loadImageView = relativeLayout.findViewById(R.id.id_loading_view);
                loadImageView.setImageResource(R.drawable.net_error);
                loadImageView.setVisibility(View.VISIBLE);
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

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        Log.i("console", consoleMessage.message());
        return true;
    }

    @Override
    public void onConsoleMessage(String message, int lineNumber, String sourceID) {
        Log.i("console", message);
    }

    // For 3.0+ Devices (Start)
    // onActivityResult attached before constructor
    protected void openFileChooser(ValueCallback uploadMsg, String acceptType)
    {
//        mUploadMessage = uploadMsg;
//        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//        i.addCategory(Intent.CATEGORY_OPENABLE);
//        i.setType("image/*");
//        startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
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
