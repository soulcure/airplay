package swaiotos.runtime.h5.core.os.webview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.RotateDrawable;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import swaiotos.runtime.h5.R;
import swaiotos.runtime.h5.common.event.UrlLoadFinishedEvent;
import swaiotos.runtime.h5.common.util.LogUtil;
import swaiotos.runtime.h5.core.os.H5CoreOS;


/**
 * @ClassName: AppletWebViewClinet
 * @Author: AwenZeng
 * @CreateDate: 2020/10/22 17:56
 * @Description:
 */
public class AppletWebViewClient extends LoadingStateWebViewClient {
    private RelativeLayout relativeLayout;
    private Context mContext;
    private boolean loadErr;
    private String id;

    private IAppletWebViewClient listener;

    public interface IAppletWebViewClient {
        boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request);
        boolean shouldOverrideUrlLoading(WebView view, String url);
        void onPageFinished(WebView view, String url);
    }

    public void resetLoadState(){
        loadErr = false;
        if(relativeLayout!=null){
            TextView txtView = relativeLayout.findViewById(R.id.id_content_view);
            txtView.setVisibility(View.INVISIBLE);

            Button btnRetry = relativeLayout.findViewById(R.id.btn_retry);
            btnRetry.setVisibility(View.INVISIBLE);

            ImageView loadImageView = relativeLayout.findViewById(R.id.id_loading_view);
            loadImageView.setImageResource(R.drawable.nodata);
            loadImageView.setVisibility(View.INVISIBLE);
        }
    }

    public void setListener(IAppletWebViewClient listener) {
        this.listener = listener;
    }

    public AppletWebViewClient(Context context, RelativeLayout rLayout) {
        mContext = context;
        this.relativeLayout = rLayout;

    }
    public AppletWebViewClient(Context context, RelativeLayout rLayout,String id) {
        mContext = context;
        this.relativeLayout = rLayout;
        this.id = id;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        LogUtil.androidLog(H5CoreOS.TAG, "======== shouldOverrideUrlLoading 222   =======");
        LogUtil.androidLog(H5CoreOS.TAG, "Url 222 =" + request.getUrl());
        if(listener != null) {
            return listener.shouldOverrideUrlLoading(view, request);
        }
        return false;
    }


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        LogUtil.androidLog(H5CoreOS.TAG, "======== shouldOverrideUrlLoading   =======");
        LogUtil.androidLog(H5CoreOS.TAG, "Url=" + url);
        if(listener != null) {
            return listener.shouldOverrideUrlLoading(view, url);
        }
        return false;
//        try {
//            if (url.startsWith("http:") || url.startsWith("https:")) {
//                view.loadUrl(url);
//            } else {
////                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
////                mContext.startActivity(intent);
//            }
//            return true;
//        } catch (Exception e){
//            return false;
//        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        isLoadOk = false;
        loadErr = false;
        if(relativeLayout!=null){
            TextView txtView = relativeLayout.findViewById(R.id.id_content_view);
            txtView.setVisibility(View.INVISIBLE);

            Button btnRetry = relativeLayout.findViewById(R.id.btn_retry);
            btnRetry.setVisibility(View.INVISIBLE);

            ImageView loadImageView = relativeLayout.findViewById(R.id.id_loading_view);
            loadImageView.setImageResource(R.drawable.loading);
            loadImageView.setVisibility(View.VISIBLE);
            RotateDrawable animationDrawable = (RotateDrawable) loadImageView.getDrawable();
            ObjectAnimator anim = ObjectAnimator.ofInt(animationDrawable, "level", 0, 10000);
            anim.setDuration(1000);
            anim.setRepeatCount(ValueAnimator.INFINITE);
            anim.start();
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        isLoadOk = true;
        LogUtil.androidLog(H5CoreOS.TAG, "======== onPageFinished   ======= ");
        LogUtil.androidLog(H5CoreOS.TAG, "finish url->" + (TextUtils.isEmpty(url) ? "" : url));

        UrlLoadFinishedEvent event = new UrlLoadFinishedEvent("",this.id);
        event.setData(url);
        EventBus.getDefault().post(event);

        if(!loadErr && relativeLayout!=null){
            TextView txtView = relativeLayout.findViewById(R.id.id_content_view);
            txtView.setVisibility(View.INVISIBLE);

            Button btnRetry = relativeLayout.findViewById(R.id.btn_retry);
            btnRetry.setVisibility(View.INVISIBLE);

            ImageView loadImageView = relativeLayout.findViewById(R.id.id_loading_view);
            loadImageView.setImageResource(R.drawable.nodata);
            loadImageView.setVisibility(View.INVISIBLE);
        }

        loadErr = false;
        if(listener != null) {
            listener.onPageFinished(view, url);
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        LogUtil.androidLog(H5CoreOS.TAG, "======== onReceivedError   =======");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LogUtil.androidLog(H5CoreOS.TAG, "error.getDescription=" + error.getDescription());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int errorCode = error.getErrorCode();
            if (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT || errorCode == ERROR_TIMEOUT ) {
                loadErr = true;
                if(relativeLayout!=null){
                    TextView txtView = relativeLayout.findViewById(R.id.id_content_view);
                    txtView.setVisibility(View.VISIBLE);
                    txtView.setText(R.string.mobile_contents_fail);

                    Button btnRetry = relativeLayout.findViewById(R.id.btn_retry);
                    btnRetry.setVisibility(View.VISIBLE);

                    ImageView loadImageView = relativeLayout.findViewById(R.id.id_loading_view);
                    loadImageView.setImageResource(R.drawable.nodata);
                    loadImageView.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LogUtil.androidLog(H5CoreOS.TAG, "======== onReceivedHttpError   =======" + request.getUrl());
        } else {
            LogUtil.androidLog(H5CoreOS.TAG, "======== onReceivedHttpError   =======" + request);
        }
        //影视页面需要屏蔽掉http error处理
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            int statusCode = errorResponse.getStatusCode();
//            if (404 == statusCode || 500 == statusCode) {
//                loadErr = true;
//                if(relativeLayout!=null){
//                    TextView txtView = relativeLayout.findViewById(R.id.id_content_view);
//                    txtView.setVisibility(View.VISIBLE);
//                    txtView.setText(R.string.mobile_network_fail);
//
//                    Button btnRetry = relativeLayout.findViewById(R.id.btn_retry);
//                    btnRetry.setVisibility(View.VISIBLE);
//
//                    ImageView loadImageView = relativeLayout.findViewById(R.id.id_loading_view);
//                    loadImageView.setImageResource(R.drawable.nodata);
//                    loadImageView.setVisibility(View.VISIBLE);
//                }
//            }
//        }

    }
}
