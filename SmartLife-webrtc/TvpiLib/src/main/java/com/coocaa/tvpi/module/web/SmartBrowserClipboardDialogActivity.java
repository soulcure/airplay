package com.coocaa.tvpi.module.web;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpilib.R;
import com.tencent.smtt.sdk.WebView;


public class SmartBrowserClipboardDialogActivity extends AppCompatActivity implements View.OnClickListener {
    private WebView webView;
    private WebViewInfoResolver resolver;
    private String webUrl;
    private TextView tvTitle;
    private ImageView iv;
    private TextView tvDescription;
    private Bitmap iconBitmap;
    String TAG = "SmartBrowserClipboardDialog";

    public static void start(Context context, String url) {
        if (context == null)
            return;
        Intent intent = new Intent(context, SmartBrowserClipboardDialogActivity.class);
        intent.putExtra("webUrl", url);
        context.startActivity(intent);
    }

    private WebViewInfoResolver.IWebInfoCallback callback = new WebViewInfoResolver.IWebInfoCallback() {
        @Override
        public void onWebIconLoaded(String url, Bitmap bitmap) {
            Log.d(TAG, "onWebIconLoaded, bitmap=" + bitmap
                    + ", isRecycled=" + (bitmap == null ? "null" : bitmap.isRecycled()));
            iconBitmap = bitmap;
            if (bitmap != null && !bitmap.isRecycled()) {
                Log.d(TAG, "web icon size, w=" + bitmap.getWidth() + ", h=" + bitmap.getHeight());
                HomeIOThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        WebIconUtils.saveIcon(SmartBrowserClipboardDialogActivity.this, url, bitmap);
                    }
                });
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!SmartBrowserClipboardDialogActivity.this.isDestroyed())
                        updateIcon(bitmap);
                }
            });
        }

        @Override
        public void onWebTitleLoaded(String url, String title) {
            Log.d(TAG, "onWebTitleLoaded : " + title);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!SmartBrowserClipboardDialogActivity.this.isDestroyed()) {
                        updateTitle(title);
                    }
                }
            });
        }

        @Override
        public boolean shouldOverrideUrlLoading(String url) {
            return false;
        }

        @Override
        public void onLoadResource(String url) {

        }

        @Override
        public void onWebDescriptionLoaded(String url, String description) {
            Log.d(TAG, "onWebDescriptionLoaded, url=" + url + ", description=" + description);
            HomeUIThread.execute(new Runnable() {
                @Override
                public void run() {
                    if (!SmartBrowserClipboardDialogActivity.this.isDestroyed())
                        updateContent(description);
                }
            });
        }

        @Override
        public void onPageFinished(String url) {
            Log.d(TAG, "onPageFinished : " + url);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateIcon(iconBitmap);
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_browser_clipboard);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initView();

        parseIntent(getIntent());
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseIntent(intent);
    }

    private void initView() {
        tvTitle = findViewById(R.id.tv_title);
        tvDescription = findViewById(R.id.tv_content);
        iv = findViewById(R.id.iv);

        TextView tvOpen = findViewById(R.id.tv_open);
        TextView tvAdd = findViewById(R.id.tv_add);
        TextView tvCancel = findViewById(R.id.tv_cancel);
        tvOpen.setOnClickListener(this);
        tvAdd.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        webView = new WebView(this);
        resolver = new WebViewInfoResolver(webView, callback);
    }

    public void updateTitle(String title) {
        if (tvTitle != null) {
            if ((!TextUtils.isEmpty(title) && !title.equals("null"))) {
                tvTitle.setText(title);
            } else {
                tvTitle.setText(webUrl);
            }
        }
    }

    public synchronized void updateIcon(Bitmap icon) {
        this.iconBitmap = icon;
        if (iv != null && !isDestroyed())
            Glide.with(this).load(icon).into(iv);
    }

    public synchronized void updateContent(String content) {
        if (tvDescription != null) {
            if (!TextUtils.isEmpty(content) && !content.equals("null")) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(content);
            } else {
                tvDescription.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_open) {
            Intent intent = new Intent(SmartBrowserClipboardDialogActivity.this,
                    SmartBrowserSearchActivity.class);
            intent.putExtra("webUrl", webUrl);
            startActivity(intent);
        } else if (id == R.id.tv_add) {
            WebRecordBean webRecordBean =
                    new WebRecordBean(tvTitle.getText().toString(), tvDescription.getText().toString(),
                            WebIconUtils.getWebIconUrl(SmartBrowserClipboardDialogActivity.this, webUrl),
                            webUrl);
            BrowserRecordUtils.addWebRecord(SmartBrowserClipboardDialogActivity.this, webRecordBean);
        }
        finish();
    }

    private void parseIntent(Intent intent) {
        if (intent != null) {
            webUrl = intent.getStringExtra("webUrl");
        }

        if (!TextUtils.isEmpty(webUrl)) {
            Log.d(TAG, "webUrl=" + webUrl);
            webView.loadUrl(webUrl);
            tvTitle.setText(webUrl);
            tvDescription.setVisibility(View.GONE);
            Glide.with(this).load(R.drawable.smart_browser_default_icon).into(iv);
        }
    }
}
