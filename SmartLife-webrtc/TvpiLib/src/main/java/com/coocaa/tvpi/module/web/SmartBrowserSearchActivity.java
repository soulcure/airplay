package com.coocaa.tvpi.module.web;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.share.ShareOut;
import com.coocaa.tvpi.module.web.adapter.BrowserSearchGuideAdapter;
import com.coocaa.tvpi.util.ClipboardUtil;
import com.coocaa.tvpi.util.SoftKeyBoardUtil;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;
import com.tencent.smtt.sdk.WebIconDatabase;
import com.tencent.smtt.sdk.WebView;

import swaiotos.runtime.h5.core.os.H5RunType;

/**
 * 拉起dongle端/TV端web页面
 *
 * @Author: yuzhan
 */
public class SmartBrowserSearchActivity extends AppCompatActivity implements View.OnClickListener {

    private String clipboardContent;
    private String webUrl;
    private String webTitle;
    private String webDescription;
    private String webIcon;
    private ImageView ivBack;
    private ImageView ivActivityBack;
    private ImageView ivNext;
    private ImageView ivClear;
    private ImageView ivRefresh;
    private ImageView ivShare;
    private ConstraintLayout ClHint;
    private View viewHint;
    private View viewDark;
    private TextView tvOpen;
    private ImageView ivAdd;
    private ImageView ivProjection;
    private TextView tvClipboardContent;
    private EditText et;
    private RecyclerView recyclerView;
    private BrowserSearchGuideAdapter guideAdapter;
    private WebView webView;
    private boolean hadAddUrl = false;
    String TAG = "SmartBrowser";

//    private Map<String, TempWebRecord> tempMap = new ArrayMap<>();

    WebViewInfoResolver resolver;

    public static void start(Context context, String url) {
        if (context == null)
            return;
        Intent intent = new Intent(context, SmartBrowserSearchActivity.class);
        intent.putExtra("webUrl", url);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());
        setContentView(R.layout.activity_smart_browser_search);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        initView();
        setListener();

        Intent intent = getIntent();
        if (intent != null) {
            webUrl = intent.getStringExtra("webUrl");
        }
        Log.d(TAG, "webUrl=" + webUrl);
        if (!TextUtils.isEmpty(webUrl)) {
            recyclerView.setVisibility(View.GONE);
            load();
            et.setHint(webUrl);
        }
    }

    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        ivActivityBack = findViewById(R.id.iv_activity_back);
        et = findViewById(R.id.et);
        ivShare = findViewById(R.id.iv_share);
        ivNext = findViewById(R.id.iv_next);
        ivClear = findViewById(R.id.iv_clear);
        ivRefresh = findViewById(R.id.iv_refresh);
        ClHint = findViewById(R.id.cl_hint);
        viewHint = findViewById(R.id.top_bg);
        viewDark = findViewById(R.id.bottom_bg);
        tvOpen = findViewById(R.id.tv_open);
        tvClipboardContent = findViewById(R.id.tv_clipboard_content);
        recyclerView = findViewById(R.id.recycler_view);
        webView = findViewById(R.id.web_view);
        ivAdd = findViewById(R.id.iv_add);
        ivProjection = findViewById(R.id.iv_projection);
        guideAdapter = new BrowserSearchGuideAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recyclerView.setAdapter(guideAdapter);
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        ivProjection.setVisibility(View.GONE);
        ivAdd.setVisibility(View.GONE);
        ClHint.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        ivBack.setVisibility(View.GONE);
        ivNext.setVisibility(View.GONE);
        ivClear.setVisibility(View.GONE);
        tvOpen.setVisibility(View.GONE);
        ivRefresh.setVisibility(View.GONE);
        et.setCompoundDrawablesRelativeWithIntrinsicBounds(
                getResources().getDrawable(R.drawable.smart_browser_search_icon),
                null, null, null);
        et.setPadding(DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 10),
                0, 0, 0);
    }

    private void setListener() {
        ivActivityBack.setOnClickListener(this);
        ivShare.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        ivClear.setOnClickListener(this);
        ivRefresh.setOnClickListener(this);
        viewHint.setOnClickListener(this);
        viewDark.setOnClickListener(this);
        tvOpen.setOnClickListener(this);
        ivAdd.setOnClickListener(this);
        ivProjection.setOnClickListener(this);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    ivClear.setVisibility(View.GONE);
                } else {
                    if (ivRefresh.getVisibility() == View.GONE) {
                        et.setPadding(DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 10),
                                0, DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 100), 0);
                        ivClear.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!TextUtils.isEmpty(et.getText())) {
                        ivClear.setVisibility(View.VISIBLE);
                    } else {
                        parseClipboard();
                    }
                    ivRefresh.setVisibility(View.GONE);
                    tvOpen.setVisibility(View.VISIBLE);
                    et.setPadding(DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 10),
                            0, DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 100), 0);
                    SoftKeyBoardUtil.openKeyBoard(SmartBrowserSearchActivity.this, et);
                } else {
                    et.setPadding(DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 10),
                            0, DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 10), 0);
                    ivClear.setVisibility(View.GONE);
                    tvOpen.setVisibility(View.GONE);
                    SoftKeyBoardUtil.hideKeyBoard(SmartBrowserSearchActivity.this, et);
                }
            }
        });
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        openWeb();
                        break;
                }
                return true;
            }
        });

        resolver = new WebViewInfoResolver(webView, callback);
    }

    private WebViewInfoResolver.IWebInfoCallback callback = new WebViewInfoResolver.IWebInfoCallback() {
        @Override
        public void onWebIconLoaded(String url, Bitmap bitmap) {
            Log.d(TAG, "onWebIconLoaded");
            if (bitmap != null && !bitmap.isRecycled()) {
                HomeIOThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        WebIconUtils.saveIcon(SmartBrowserSearchActivity.this, url, bitmap);
                        webIcon = WebIconUtils.getWebIconUrl(SmartBrowserSearchActivity.this, url);
                    }
                });
            }
        }

        @Override
        public void onWebTitleLoaded(String url, String title) {
            Log.d(TAG, "onWebTitleLoaded : " + title);
            webTitle = title;
        }

        @Override
        public boolean shouldOverrideUrlLoading(String url) {
            Log.d(TAG, "shouldOverrideUrlLoading           url=====: " + url);
            changeIcon();
            return false;
        }

        @Override
        public void onLoadResource(String url) {
            webUrl = url;
            et.setText(webUrl);
        }

        @Override
        public void onWebDescriptionLoaded(String url, String description) {
            Log.d(TAG, "onWebDescriptionLoaded, url=" + url + ", description=" + description);
            webDescription = description;
        }

        @Override
        public void onPageFinished(String url) {
            Log.d(TAG, "onPageFinished, url=" + url);
            webView.setVisibility(View.VISIBLE);
            et.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    getResources().getDrawable(R.drawable.smart_browser_browser_icon),
                    null, null, null);
            ivAdd.setVisibility(View.VISIBLE);
            hadAddUrl = BrowserRecordUtils.checkUrlExist(webUrl);
            if (hadAddUrl) {
                ivAdd.setImageDrawable(getResources().getDrawable(R.drawable.smart_browser_search_added_icon));
            } else {
                ivAdd.setImageDrawable(getResources().getDrawable(R.drawable.smart_browser_search_add_icon));
            }
            ivProjection.setVisibility(View.VISIBLE);
            ivRefresh.setVisibility(View.VISIBLE);
            ivClear.setVisibility(View.GONE);
            tvOpen.setVisibility(View.GONE);
            et.setPadding(DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 10),
                    0, DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 38), 0);
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            String url = intent.getStringExtra("webUrl");
            if (!TextUtils.isEmpty(url)) {
                webUrl = url;
                load();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    private void parseClipboard() {
        String clipboard = getClipboardContent();
        if (!TextUtils.isEmpty(clipboard)) {
            clipboardContent = ClipboardUtil.conversionKeywordLoadOrSearch(getClipboardContent());
            Log.d(TAG, "clipboardContent=" + clipboardContent);
            if (!TextUtils.isEmpty(clipboardContent) && validUrl(clipboardContent)) {
                webUrl = clipboardContent;
                tvClipboardContent.setText(webUrl);
                ClHint.setVisibility(View.VISIBLE);
            } else {
                ClHint.setVisibility(View.GONE);
            }
        }
    }

    private boolean validUrl(String content) {
        if (content == null)
            return false;
        String lowerContent = content.toLowerCase();
        return lowerContent.startsWith("http://") || lowerContent.startsWith("https://");
    }

    private String getClipboardContent() {
        ClipboardManager cm = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData data = cm.getPrimaryClip();
            if (data != null && data.getItemCount() > 0) {
                ClipData.Item item = data.getItemAt(0);
                if (item != null) {
                    CharSequence sequence = item.coerceToText(this);
                    if (sequence != null) {
                        return sequence.toString();
                    }
                }
            }
        }
        return null;
    }


    private void startWeb() {
        if (TextUtils.isEmpty(webUrl)) {
            ToastUtils.getInstance().showGlobalShort("链接不能为空");
            return;
        }
        JSONObject content = new JSONObject();
        content.put("do", "launcher_browser");
        content.put("url", webUrl);
        content.put("name", "web页面");
        content.put("pageType", "browser");

        SSConnectManager.getInstance().sendTextMessage(JSON.toJSONString(content),
                "ss-clientID-runtime-h5-channel");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_activity_back) {
            finish();
        } else if (id == R.id.iv_share) {
            if (validUrl(webUrl)) {
                ShareOut.shareWeb(SmartBrowserSearchActivity.this, webUrl);
            }
        } else if (id == R.id.iv_back) {
            webView.goBack();
            if (ivBack.getVisibility() == View.VISIBLE) {
                et.setPadding(DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 10),
                        0, DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 50), 0);
            } else {
                et.setPadding(DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 10),
                        0, DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 10), 0);
            }
            ivClear.setVisibility(View.GONE);
            tvOpen.setVisibility(View.GONE);
            changeIcon();
        } else if (id == R.id.iv_next) {
            webView.goForward();
            if (ivBack.getVisibility() == View.VISIBLE) {
                et.setPadding(DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 10),
                        0, DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 50), 0);
            } else {
                et.setPadding(DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 10),
                        0, DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 10), 0);
            }
            ivClear.setVisibility(View.GONE);
            tvOpen.setVisibility(View.GONE);
            changeIcon();
        } else if (id == R.id.iv_clear) {
            et.getText().clear();
            ivRefresh.setVisibility(View.GONE);
        }
        if (id == R.id.tv_open) {
            openWeb();
        } else if (id == R.id.iv_refresh) {
            webUrl = et.getText().toString();
            load();
        } else if (id == R.id.top_bg) {
            webUrl = tvClipboardContent.getText().toString();
            load();
            et.setPadding(DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 10),
                    0, DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 38), 0);
            ivClear.setVisibility(View.GONE);
            ClHint.setVisibility(View.GONE);
            et.clearFocus();
        } else if (id == R.id.bottom_bg) {
            ClHint.setVisibility(View.GONE);
        } else if (id == R.id.iv_projection) {
            if (!SmartApi.isDeviceConnect()) {
                SmartApi.startConnectDevice();
            } else {
                if (!SmartApi.isSameWifi()) {
                    SmartApi.startConnectSameWifi(H5RunType.RUNTIME_NETWORK_FORCE_LAN);
                } else {
                    startWeb();
                }
            }
        } else if (id == R.id.iv_add) {
            if (hadAddUrl) {
                hadAddUrl = false;
                ivAdd.setImageDrawable(getResources().getDrawable(R.drawable.smart_browser_search_add_icon));
                //这里不能用webTitle、webContent变量了，要从tempMap里拿webUrl对应的
                BrowserRecordUtils.removeWebRecord(this,
                        new WebRecordBean(webTitle, webDescription,
                                WebIconUtils.getWebIconUrl(SmartBrowserSearchActivity.this, webIcon),
                                webUrl));
            } else {
                hadAddUrl = true;
                ivAdd.setImageDrawable(getResources().getDrawable(R.drawable.smart_browser_search_added_icon));
                //这里不能用webTitle、webContent变量了，要从tempMap里拿webUrl对应的
                BrowserRecordUtils.addWebRecord(this,
                        new WebRecordBean(webTitle, webDescription,
                                WebIconUtils.getWebIconUrl(SmartBrowserSearchActivity.this, webIcon),
                                webUrl));
            }
        }
    }

    private void changeIcon() {
        Log.d(TAG, "webView.canGoBack()====" + webView.canGoBack());
        Log.d(TAG, "webView.canGoForward()====" + webView.canGoForward());
        if (webView.canGoBack()) {
            if (ivBack.getVisibility() == View.GONE) {
                ivBack.setVisibility(View.VISIBLE);
                ivNext.setVisibility(View.VISIBLE);
            }
            ivBack.setEnabled(true);
            ivBack.setImageResource(R.drawable.smart_browser_back_dark_icon);
        } else {
            ivBack.setEnabled(false);
            ivBack.setImageResource(R.drawable.smart_browser_back_gary_icon);
        }

        if (webView.canGoForward()) {
            ivNext.setEnabled(true);
            ivNext.setImageResource(R.drawable.smart_browser_next_dark_icon);
        } else {
            ivNext.setEnabled(false);
            ivNext.setImageResource(R.drawable.smart_browser_next_gray_icon);
        }
    }

    private void load() {
        webView.loadUrl(webUrl);
    }

    private void openWeb() {
        if (TextUtils.isEmpty(et.getText())) {
            ToastUtils.getInstance().showGlobalShort("请输入地址");
            return;
        }
        String url = ClipboardUtil.conversionKeywordLoadOrSearch(et.getText().toString());
        if (!validUrl(url)) {
            ToastUtils.getInstance().showGlobalShort("请输入地址");
            return;
        }
        ClHint.setVisibility(View.GONE);
        webUrl = url;
        load();
        et.setPadding(DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 10),
                0, DimensUtils.dp2Px(SmartBrowserSearchActivity.this, 10), 0);
        ivClear.setVisibility(View.GONE);
        tvOpen.setVisibility(View.GONE);
        et.clearFocus();
    }

}
