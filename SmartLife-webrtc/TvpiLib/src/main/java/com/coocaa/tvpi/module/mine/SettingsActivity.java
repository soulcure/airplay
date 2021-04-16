package com.coocaa.tvpi.module.mine;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.views.SDialog;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.tvpi.module.connection.ScanActivity2;
import com.coocaa.tvpi.module.login.LoginActivity;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.mine.about.AboutActivity;
import com.coocaa.tvpi.module.mine.lab.SmartLabActivity2;
import com.coocaa.tvpi.module.mine.userinfo.UserInfoActivity;
import com.coocaa.tvpi.module.openapi.JumpWxMPActivity;
import com.coocaa.tvpi.module.share.ShareActivity;
import com.coocaa.tvpi.util.LogoutHelp;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.Utils;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpi.view.webview.SimpleWebViewActivity;
import com.coocaa.tvpilib.R;

import java.util.Objects;

public class SettingsActivity extends BaseActivity {
    private final static String VERSION_PREFIX = "Version ";
    private SDialog exitDialog;
    private SDialog connectTipDialog;
    private TextView tvLoginOrExit;

    private View userInfoLayout;
    private View beginnerGuideLayout;
    private View officialWebsiteLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        initView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        tvLoginOrExit.setText(UserInfoCenter.getInstance().isLogin() ? "退出登录" : "去登录");
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        CommonTitleBar titleBar = findViewById(R.id.titleBar);
        TextView tvVersionName = findViewById(R.id.tvVersionName);
        tvVersionName.setText(VERSION_PREFIX + String.format("%s", Utils.getAppVersionName(this)));
        tvLoginOrExit = findViewById(R.id.tvExit);

        userInfoLayout = findViewById(R.id.user_info_layout);
        beginnerGuideLayout = findViewById(R.id.beginner_guide);
        officialWebsiteLayout = findViewById(R.id.official_website);

        userInfoLayout.setOnClickListener(mClickListener);
        beginnerGuideLayout.setOnClickListener(mClickListener);
        officialWebsiteLayout.setOnClickListener(mClickListener);


        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    finish();
                }
            }
        });

        findViewById(R.id.smartScreenManagerLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UserInfoCenter.getInstance().isLogin()) {
                    ToastUtils.getInstance().showGlobalShort("请先登录");
                    LoginActivity.start(SettingsActivity.this);
                    return;
                }

                if (!SSConnectManager.getInstance().isConnected()) {
                    showConnectTipDialog();
                    return;
                }


//                if (!SSConnectManager.getInstance().isDongle()) {
//                    ToastUtils.getInstance().showGlobalShort("当前连接设备暂不支持此功能");
//                    return;
//                }

                Intent intent = new Intent(SettingsActivity.this, DeviceManagerActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.shareLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //分享app填入空参数使用默认值即可
                ShareActivity.startShareUMWeb(SettingsActivity.this,
                        "",
                        "",
                        "",
                        "",
                        0,
                        "");
            }
        });

        findViewById(R.id.aboutLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        tvLoginOrExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserInfoCenter.getInstance().isLogin()) {
                    showLogoutDialog();
                } else {
                    LoginActivity.start(SettingsActivity.this);
                }
            }
        });

        findViewById(R.id.myOderLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, JumpWxMPActivity.class);
                intent.putExtra("id", "gh_1e4528d77d8f");
                intent.putExtra("path", "__plugin__/wx34345ae5855f892d/pages/orderList/orderList.html?tabId=all");
                startActivity(intent);
            }
        });

        findViewById(R.id.lab_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Objects.requireNonNull(SettingsActivity.this), SmartLabActivity2.class);
                startActivity(intent);
            }
        });

       /* VerificationCodeDialog verifyCodeDialog = new VerificationCodeDialog();
        verifyCodeDialog.setVerifyCodeListener(new VerificationCodeDialog.VerifyCodeListener() {
            @Override
            public void onVerifyPass() {
                Device device = SSConnectManager.getInstance().getDevice();
                if (device != null) {
                    DeviceInfo deviceInfo = device.getInfo();
                    if (null != deviceInfo) {
                        if (deviceInfo.type() == DeviceInfo.TYPE.TV) {
                            TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) deviceInfo;
                            DongleActivity.start(SettingsActivity.this, tvDeviceInfo.MAC);
                        }
                    }
                }
            }
        });
        verifyCodeDialog.show(getSupportFragmentManager(), "verifyCodeDialog");*/
    }

    private void showLogoutDialog() {
        if (exitDialog == null) {
            exitDialog = new SDialog(this, "退出登录", "退出后将无法使用共享屏",
                    R.string.cancel, R.string.mine_login_out,
                    new SDialog.SDialog2Listener() {
                        @Override
                        public void onClick(boolean l, View view) {
                            if (!l) {
                                LogoutHelp.logout();
                                finish();
                            }
                        }
                    });
        }
        if (!exitDialog.isShowing()) {
            exitDialog.show();
        }
    }

    private void showConnectTipDialog() {
        if (connectTipDialog == null) {
            connectTipDialog = new SDialog(this, "未连接共享屏", "连接共享屏才能进行共享屏管理",
                    R.string.dialog_live_tips_confirm, R.string.bt_connect_wifi,
                    new SDialog.SDialog2Listener() {
                        @Override
                        public void onClick(boolean l, View view) {
                            if (!l) {
                                PermissionsUtil.getInstance().requestPermission(SettingsActivity.this, new PermissionListener() {
                                    @Override
                                    public void permissionGranted(String[] permission) {
                                        ScanActivity2.start(SettingsActivity.this);
                                    }

                                    @Override
                                    public void permissionDenied(String[] permission) {
                                        ToastUtils.getInstance().showGlobalShort("需要开启相机权限");
                                    }
                                }, Manifest.permission.CAMERA);
                            }
                        }
                    });
        }
        if (!connectTipDialog.isShowing()) {
            connectTipDialog.show();
        }
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == userInfoLayout) {
                if (!UserInfoCenter.getInstance().isLogin()) {
                    ToastUtils.getInstance().showGlobalShort("请先登录");
                    return;
                }

                Intent intent = new Intent(SettingsActivity.this, UserInfoActivity.class);
                startActivity(intent);
            } else if (v == beginnerGuideLayout) {
                SimpleWebViewActivity.startAsApplet(SettingsActivity.this, "https://webapp.skysrt.com/swaiot/novice-guide/index.html");
            } else if (v == officialWebsiteLayout) {
                SimpleWebViewActivity.startAsH5(SettingsActivity.this, "https://www.ccss.tv");
            }
        }
    };
}
