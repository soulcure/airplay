package com.coocaa.tvpi.module.feedback;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.mine.view.CustomWebView;
import com.coocaa.tvpi.util.FileChooseUtils;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpilib.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @Author: yuzhan
 */
public class FeedbackActivity extends AppCompatActivity implements UnVirtualInputable {

    private final static String TAG = FeedbackActivity.class.getSimpleName();
    private final static String HTTP_PREFIX = "http";
    private final static String HTTPS_PREFIX = "https";
    private final static String TARGET_URL = "post";
    FrameLayout frameLayout;
    private CustomWebView webView;
    private final static int FILECHOOSER_RESULTCODE = 1;
    //微信前缀
    private final static String WECHAT_PREFIX = "weixin://";
    private final static String PRODUCT_ID = "287850";
    private ValueCallback<Uri[]> mUploadMessage;
    private ImageView imgFeedBack;
    private String postData;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        initView();
        initListener();
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {

            }

            @Override
            public void permissionDenied(String[] permission) {
                ToastUtils.getInstance().showGlobalLong("将无法发表图片");
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
//        frameLayout = new FrameLayout(this);
//        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        setContentView(frameLayout);
//
//        webView = new WebView(this);
//        webView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
//        frameLayout.addView(webView);

        initWeb();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    private void initView() {
        webView = findViewById(R.id.wv_content);
        imgFeedBack = findViewById(R.id.feed_back_img);
    }

    private void initListener() {
        imgFeedBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (result == null) {
                mUploadMessage.onReceiveValue(null);
                mUploadMessage = null;
                return;
            }
            Log.i("UPFILE", "onActivityResult" + result.toString());
            String path = FileChooseUtils.getPath(this, result);
            if (TextUtils.isEmpty(path)) {
                mUploadMessage.onReceiveValue(null);
                mUploadMessage = null;
                return;
            }
            Uri uri = Uri.fromFile(new File(path));
            Log.i("UPFILE", "onActivityResult after parser uri:" + uri.toString());
            mUploadMessage.onReceiveValue(new Uri[]{uri});
            mUploadMessage = null;
        }
    }

    private void initWeb() {
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);       // 这个要加上
        initPostData();
        /* 获得 webview url，请注意url单词是product而不是products，products是旧版本的参数，用错地址将不能成功提交 */
        String url = "https://support.qq.com/product/" + PRODUCT_ID;

        /* WebView 内嵌 Client 可以在APP内打开网页而不是跳出到浏览器 */
        WebViewClient webViewClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                super.shouldOverrideUrlLoading(view, url);

                Log.d(TAG, "shouldOverrideUrlLoading: getUrl " +webView.getUrl());
                Log.d(TAG, "shouldOverrideUrlLoading: postUrl " + url);
                if(webView.getUrl().equals(url)){
                    return true;
                }
                if (isStartAPP(url)) {
                    return true;
                }
                if (UserInfoCenter.getInstance().isLogin()) {
                    webView.postUrl(url, postData.getBytes());
                } else {
                    Log.d(TAG, "shouldOverrideUrlLoading: loadUrl");
                    webView.loadUrl(url);
                }

                return true;
            }
        };
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(new XHSWebChromeClient());
        if (UserInfoCenter.getInstance().isLogin()) {
            Log.d(TAG, "initWeb: " + postData);
            webView.postUrl(url, postData.getBytes());
        } else {
            Log.d(TAG, "shouldOverrideUrlLoading: loadUrl");
            webView.loadUrl(url);
        }
    }

    private void initPostData() {
        String mobile = null;
        String avatar = null;
        String openId = null;
        if (UserInfoCenter.getInstance().isLogin()) {
            Log.d(TAG, "initPostData: " + UserInfoCenter.getInstance().getCoocaaUserInfo().getAvatar());
            CoocaaUserInfo coocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
            if (coocaaUserInfo.getMobile() != null && coocaaUserInfo.getMobile().length() == 11) {
                mobile = coocaaUserInfo.getMobile().substring(0, 3) + "****" + coocaaUserInfo.getMobile().substring(7);
            }
            //测试
            //coocaaUserInfo.setAvatar("");
            if (!coocaaUserInfo.getAvatar().startsWith(HTTPS_PREFIX)) {
                avatar = coocaaUserInfo.getAvatar().replaceFirst(HTTP_PREFIX, HTTPS_PREFIX);
                try {
                    avatar = URLEncoder.encode(avatar, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else if (coocaaUserInfo.getAvatar() != null) {
                avatar = coocaaUserInfo.getAvatar();
            }
            if (avatar != null) {
                avatar = avatar.equals("") ? null : avatar;
            }
            if (coocaaUserInfo.getOpen_id() != null) {
                openId = coocaaUserInfo.getOpen_id();
            }
            postData = "nickname=" + mobile + "&avatar=" + avatar + "&openid=" + openId;
        }
    }

    private boolean isStartAPP(String url) {
        if (url.startsWith(WECHAT_PREFIX)) {
            //类型我目前用到的是微信、支付宝、拨号 三种跳转方式，其他类型自加
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            try {
                startActivity(intent);
            } catch (Exception e) {
                ToastUtils.getInstance().showGlobalLong("请下载微信");
            }
            return true;
        } else {
            return false;
        }

    }

    public class XHSWebChromeClient extends WebChromeClient {
        @Override
        @SuppressLint("NewApi")
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(null);
            }
            Log.i("UPFILE", "file chooser params：" + fileChooserParams.toString());
            mUploadMessage = filePathCallback;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            if (fileChooserParams != null && fileChooserParams.getAcceptTypes() != null
                    && fileChooserParams.getAcceptTypes().length > 0) {
                i.setType(fileChooserParams.getAcceptTypes()[0]);
            } else {
                i.setType("*/*");
            }
            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            return true;
        }
    }

    private void goBack() {
//        if (webView.getUrl().contains(TARGET_URL)) {
//            webView.goBack();
//        } else {
//            finish();
//        }

        if (webView.canGoBack()) {
            webView.goBack();
        }else {
            finish();
        }
    }
}
