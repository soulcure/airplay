package com.coocaa.tvpi.module.live;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.smartscreen.repository.utils.Preferences;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.coocaa.tvpilib.R;

public class LiveTipDialogActivity extends BaseActivity {

    public static String KEY_URL = "KEY_URL";

    private Button btnCancel;
    private Button btnConfirm;
    private String url = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_live_tips_dialog);
        initData();
        initView();
        initListener();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void initData() {
        if(getIntent().hasExtra(KEY_URL)){
            url = getIntent().getStringExtra(KEY_URL);
        }
    }

    private void initView() {
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm = findViewById(R.id.btn_confirm);
    }

    private void initListener() {
        btnCancel.setOnClickListener(v -> {
            Preferences.LiveTipConfirm.saveLiveTipConfirm(true);
            if(isLive(url)){
                TvpiClickUtil.onClick(this, url);
            }
            finish();
        });

        btnConfirm.setOnClickListener(v -> {
            if(isLive(url)){
                TvpiClickUtil.onClick(this, url);
            }
            finish();
        });
    }

    private boolean isLive(String url) {
        if(url == null){
            return false;
        }
        try {
            return "m.91kds.cn".equals(Uri.parse(url).getHost());
        } catch (Exception e) {
            return false;
        }
    }
}
