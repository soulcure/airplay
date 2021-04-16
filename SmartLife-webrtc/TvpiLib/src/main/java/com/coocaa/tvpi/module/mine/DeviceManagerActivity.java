package com.coocaa.tvpi.module.mine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.channel.CmdData;
import com.coocaa.smartscreen.data.channel.StartAppParams;
import com.coocaa.smartscreen.utils.CmdUtil;
import com.coocaa.tvpi.module.connection.ConnectNetForDongleActivity;
import com.coocaa.tvpi.module.mine.lab.networktest.NetworkTestActivityW3;
import com.coocaa.tvpi.module.mine.view.VerificationCodeDialog2;
import com.coocaa.tvpi.util.OnDebouncedClick;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpilib.R;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;

import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_APPSTATE;

public class DeviceManagerActivity extends BaseActivity {

    private ImageView ivDeviceCover;
    private TextView tvDeviceName;
    private TextView tvDeviceRegisterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_screen_manager);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDeviceInfo(SSConnectManager.getInstance().getDevice());
    }

    private void initView() {
        ivDeviceCover = findViewById(R.id.ivDeviceCover);
        tvDeviceName = findViewById(R.id.tvDeviceName);
        tvDeviceRegisterId = findViewById(R.id.tvDeviceId);
        CommonTitleBar titleBar = findViewById(R.id.titleBar);
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    finish();
                }
            }
        });

        findViewById(R.id.networkTestLayout).setOnClickListener(new OnDebouncedClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                TvpiClickUtil.onClick(DeviceManagerActivity.this,
//                        "np://com.coocaa.smart.networktest3/index?from=SmartScreenManagerActivity");
                //新网络测试
                Intent intent = new Intent(DeviceManagerActivity.this, NetworkTestActivityW3.class);
                startActivity(intent);
            }
        }));

        View chageDeviceLayout = findViewById(R.id.changeDeviceLayout);
        View changeNameLine = findViewById(R.id.changeName_line);
        Device device = SSConnectManager.getInstance().getHistoryDevice();
        if (null != device
                && device.getInfo() instanceof TVDeviceInfo
                && ((TVDeviceInfo) device.getInfo()).blueSupport == 0) {//0支持蓝牙

            chageDeviceLayout.setVisibility(View.VISIBLE);
            changeNameLine.setVisibility(View.VISIBLE);

            chageDeviceLayout.setOnClickListener(new OnDebouncedClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!SSConnectManager.getInstance().isConnected()) {
                        ToastUtils.getInstance().showGlobalLong("请先连接设备");
                        return;
                    }

                    DeviceInfo deviceInfo = device.getInfo();
                    if (null != deviceInfo) {
                        if (deviceInfo.type() == DeviceInfo.TYPE.TV) {
                            TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) deviceInfo;
                            ConnectNetForDongleActivity.start(DeviceManagerActivity.this, tvDeviceInfo.MAC);
                        } else {
                            Log.d(TAG, "onVerifyPass: device info is null ");
                        }
                    }
                }
            }));
        } else {
            chageDeviceLayout.setVisibility(View.GONE);
            changeNameLine.setVisibility(View.GONE);
        }

        findViewById(R.id.deviceSettingLayout).setOnClickListener(new OnDebouncedClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SSConnectManager.getInstance().isConnected()) {
                    ToastUtils.getInstance().showGlobalLong("请先连接设备");
                    return;
                }

                Device device = SSConnectManager.getInstance().getHistoryDevice();
                if (device == null) {
                    ToastUtils.getInstance().showGlobalLong("请先连接设备");
                    return;
                }

                //C端设备（无商家ID）
                if (TextUtils.isEmpty(device.getMerchantId())) {
                    CmdUtil.sendKey(769);
                    CmdUtil.startSettingApp();
                    TvpiClickUtil.onClick(DeviceManagerActivity.this,
                            "np://com.coocaa.smart.donglevirtualinput/index?from=DeviceManagerActivity");
                } else {
                    //  B端设备（有商家ID）
                    VerificationCodeDialog2 verifyCodeDialog = new VerificationCodeDialog2();
                    verifyCodeDialog.setVerifyCodeListener(new VerificationCodeDialog2.VerifyCodeListener() {
                        @Override
                        public void onVerifyPass() {
                            // SSConnectManager.getInstance().sendKey(769);
                            CmdUtil.startSettingApp();
                            TvpiClickUtil.onClick(DeviceManagerActivity.this,
                                    "np://com.coocaa.smart.donglevirtualinput/index?from=DeviceManagerActivity");
                        }
                    });

                    if (!verifyCodeDialog.isAdded()) {
                        verifyCodeDialog.show(getSupportFragmentManager(), "deviceSettingDialog");
                    }
                }
            }
        }));

        findViewById(R.id.deviceAboutLayout).setOnClickListener(new OnDebouncedClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceManagerActivity.this, SmartScreenAboutActivity.class);
                startActivity(intent);
            }
        }));

        findViewById(R.id.activeLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivePage();
            }
        });

        findViewById(R.id.changeNameLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceManagerActivity.this, EditDeviceNameActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setDeviceInfo(Device device) {
        if (device != null) {
            DeviceInfo deviceInfo = device.getInfo();
            if (null != deviceInfo) {
                if (deviceInfo.type() == DeviceInfo.TYPE.TV) {
                    TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) deviceInfo;
                    tvDeviceRegisterId.setText(String.format("激活ID %s", tvDeviceInfo.activeId));
                }
            }

            tvDeviceName.setText(SSConnectManager.getInstance()
                    .getDeviceName(SSConnectManager.getInstance().getHistoryDevice()));
            GlideApp.with(this)
                    .load(device.getMerchantIcon())
                    .placeholder(R.drawable.icon_connect_tv_normal)
                    .centerCrop()
                    .into(ivDeviceCover);
        }
    }

    private void startActivePage() {
        StartAppParams startAppParams = new StartAppParams();
        startAppParams.packagename = "com.coocaa.dongle.launcher";
        startAppParams.dowhat = StartAppParams.DOWHAT_START_ACTIVITY;
        startAppParams.bywhat = StartAppParams.BYWHAT_ACTION;
        startAppParams.byvalue = "coocaa.intent.action.DONGLE_ACTIVE";
        String param = startAppParams.toJson();

        CmdData data = new CmdData(StartAppParams.CMD.LIVE_VIDEO.toString(),
                CmdData.CMD_TYPE.START_APP.toString(),
                param);
        String cmd = data.toJson();
        SSConnectManager.getInstance().sendTextMessage(cmd, TARGET_APPSTATE);
    }
}
