package com.coocaa.tvpi.module.mine.about;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coocaa.smartscreen.data.upgrade.UpgradeData;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.module.mine.SettingsActivity;
import com.coocaa.tvpi.module.share.ShareActivity;
import com.coocaa.tvpi.module.upgrade.UpgradeManager;
import com.coocaa.tvpi.util.FastClick;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.Utils;
import com.coocaa.tvpi.view.webview.SimpleWebViewActivity;
import com.coocaa.tvpilib.R;

public class AboutActivity extends AppCompatActivity implements UnVirtualInputable {

    private final static  String VERSION_PREFIX = "Version ";

    private ImageView imgBack;
    private TextView tvVersionCode;
    private TextView tvAboutUse;
    private TextView tvPrivacy;
    private RelativeLayout rlShareApp;
    private RelativeLayout rlVersion;
    private RelativeLayout rlAboutUs;
    private TextView tvVersionDesc;
    private ImageView ivUpdate;
    private FastClick fastClick = new FastClick();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        initView();
        initListener();
        initUpgradeData();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        imgBack = findViewById(R.id.back_img);
        tvVersionCode = findViewById(R.id.tv_version_code);
        rlVersion = findViewById(R.id.setting_rl_version);
        rlShareApp = findViewById(R.id.rl_share_app);
        tvVersionDesc = findViewById(R.id.version_desc);
        tvAboutUse = findViewById(R.id.tv_about_use);
        tvPrivacy = findViewById(R.id.tv_privacy);
        ivUpdate = findViewById(R.id.iv_update);
        rlAboutUs = findViewById(R.id.rl_about_us);
        tvVersionCode.setText(VERSION_PREFIX+String.format("%s", Utils.getAppVersionName(this)));
    }

    private void initListener() {
        imgBack.setOnClickListener(v -> finish());

        rlVersion.setOnClickListener(v -> {
            if(!fastClick.isFaskClick()) {
                UpgradeManager.getInstance().downloadLatestAPK();
            }
        });

        rlAboutUs.setOnClickListener(v -> {
            startAboutUs();
        });

        rlShareApp.setOnClickListener(v -> {
            if(!fastClick.isFaskClick()) {
                startShare();
            }
        });

        tvPrivacy.setOnClickListener(v ->{
            startPrivacy();
        });

        tvAboutUse.setOnClickListener(v ->{
            startUse();
        });
    }

    private void initUpgradeData() {
        UpgradeManager.getInstance().upgradeLatest(new UpgradeManager.UpgradeCallback() {
            @Override
            public void onSuccess(UpgradeData upgradeData) {
                if (null != upgradeData) {
                    long versionCode = UpgradeManager.getInstance().getAppVersionCode(AboutActivity.this);
                    if (versionCode < upgradeData.version_code) {
                        tvVersionDesc.setVisibility(View.GONE);
                        ivUpdate.setVisibility(View.VISIBLE);
                    } else {
                        tvVersionDesc.setVisibility(View.VISIBLE);
                        tvVersionDesc.setText("当前已是最新版本");
                        ivUpdate.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailed(Throwable e) {

            }
        });
    }

    private void startPrivacy() {
        SimpleWebViewActivity.start(this,"http://sky.fs.skysrt.com/statics/server/kkzp_privacy.html");
    }

    private void startUse() {
        SimpleWebViewActivity.start(this,"http://sky.fs.skysrt.com/statics/server/kkzp_service.html");
    }

    private void startShare() {
        //分享app填入空参数使用默认值即可
        ShareActivity.startShareUMWeb(AboutActivity.this,
                "",
                "",
                "",
                "",
                0,
                "");
    }

    private void startAboutUs() {
        Intent intent = new Intent(AboutActivity.this,CallUsActivity.class);
        startActivity(intent);
    }
}