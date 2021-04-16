package com.coocaa.tvpi.module.whiteboard;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.utils.NetworkUtils;
import com.coocaa.tvpi.module.connection.NoNetwortDialogActivity;
import com.coocaa.tvpi.module.connection.WifiConnectActivity;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpilib.R;
import com.coocaa.whiteboard.client.WhiteBoardClientSSEvent;
import com.coocaa.whiteboard.server.WhiteBoardServerSSCmd;
import com.coocaa.whiteboard.ui.common.WBClientIOTChannelHelper;
import com.coocaa.whiteboard.ui.common.WhiteBoardUIEvent;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;
import swaiotos.channel.iot.ss.session.Session;

/**
 * @ClassName ReconnectActivity
 * @Description 画板断开重新连接
 * @User heni
 * @Date 2021/3/11
 */
public class ReconnectActivity extends BaseActivity {

    private static final String TAG = ReconnectActivity.class.getSimpleName();

    View btnLayout;
    TextView tvCancel;
    TextView tvConfirm;
    ImageView ivLoading;
    private RotateAnimation rotateAnimation;
    private boolean isWhiteBoard = true;

    public static void start(Context context) {
        Intent intent = new Intent();
        if(!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, ReconnectActivity.class);
        context.startActivity(intent);
    }

    public static void startAsNoteMark(Context context) {
        Intent intent = new Intent();
        if(!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, ReconnectActivity.class);
        intent.putExtra("mode", "NoteMark"); //批注
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_reconnect_device);

        String mode = getIntent().getStringExtra("mode");
        if("NoteMark".equals(mode)) {
            isWhiteBoard = false;
        }
        Log.d(TAG, "ReconnectActivity start mode=" + mode + ", isWhiteBoard=" + isWhiteBoard);

        initView();
        initListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        ivLoading.clearAnimation();
    }

    private void initView() {
        btnLayout = findViewById(R.id.btn_layout);
        tvCancel = findViewById(R.id.btn_cancel);
        tvConfirm = findViewById(R.id.btn_confirm);
        ivLoading = findViewById(R.id.device_connecting_icon);

        showDefaultUI();
    }

    private void showDefaultUI() {
        btnLayout.setVisibility(View.VISIBLE);
        ivLoading.setVisibility(View.GONE);
    }

    private void initListener() {
        tvCancel.setOnClickListener(v -> {
            EventBus.getDefault().post(new WhiteBoardUIEvent(WhiteBoardUIEvent.DO_WHAT_EXIT));
            finish();
        });

        tvConfirm.setOnClickListener(v -> {
            btnLayout.setVisibility(View.GONE);
            startLoadingAnim();
            reconnectDevice();
        });
    }

    private void startLoadingAnim() {
        if (rotateAnimation == null) {
            rotateAnimation = new RotateAnimation(0f, 359f,
                    android.view.animation.Animation.RELATIVE_TO_SELF,
                    0.5f, android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(300);//设置动画持续时间
            LinearInterpolator lin = new LinearInterpolator();
            rotateAnimation.setInterpolator(lin);
            rotateAnimation.setRepeatCount(-1);//设置重复次数
            rotateAnimation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
            rotateAnimation.setStartOffset(10);//执行前的等待时间
        }
        ivLoading.setVisibility(View.VISIBLE);
        ivLoading.startAnimation(rotateAnimation);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(WhiteBoardClientSSEvent event) {
        Log.d(TAG, "Reconnect Activity receive onEvent : " + event);
        if(event.info == null)
            return ;
        if(WhiteBoardServerSSCmd.CMD_SERVER_REPLY_START.equals(event.info.cmd)) {
            ivLoading.clearAnimation();
            finish();
        }
    }

    private void reconnectDevice() {
        if (!NetworkUtils.isAvailable(ReconnectActivity.this)) {
            Log.d(TAG, "reconnectDevice: 当前网络不可用");
            ToastUtils.getInstance().showGlobalShort("当前网络不可用");
            ivLoading.clearAnimation();
            showDefaultUI();
            return;
        }
        if(!SSConnectManager.getInstance().isSameWifi()) {
            WifiConnectActivity.start(ReconnectActivity.this);
            return ;
        }
        Device historyDevice = SSConnectManager.getInstance().getHistoryDevice();
        Log.d(TAG, "reconnectDevice: " + new Gson().toJson(historyDevice));

        if (historyDevice != null) {
            Session session = SSConnectManager.getInstance().getConnectSession();
            if (historyDevice.getLsid() != null
                    && session != null
                    && historyDevice.getLsid().equals(session.getId())
                    && SSConnectManager.getInstance().isConnected()) {
                if(!EventBus.getDefault().isRegistered(this))
                    EventBus.getDefault().register(this);
                WBClientIOTChannelHelper.sendStartWhiteBoardMsg(WhiteboardActivity.getAccountInfo(), false);

                showDefaultUI();
                return;
            }

            if (historyDevice.getStatus() == 1) {
                Log.d(TAG, "reconnect device");
                SSConnectManager.getInstance().selectDevice(historyDevice);
                SSConnectManager.getInstance().connect(historyDevice);
            } else {
                if (((TVDeviceInfo) historyDevice.getInfo()).blueSupport == 0) {
                    handleBle(historyDevice);
                }
            }
        }
    }

    private void handleBle(Device device) {
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                String mac = null;
                DeviceInfo deviceInfo = device.getInfo();
                if (null != deviceInfo) {
                    switch (deviceInfo.type()) {
                        case TV:
                            TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) deviceInfo;
                            if (tvDeviceInfo.blueSupport == 0) {
                                mac = tvDeviceInfo.MAC;
                            }
                            break;
                    }
                }
                NoNetwortDialogActivity.start(ReconnectActivity.this, mac);
                finish();
            }

            @Override
            public void permissionDenied(String[] permission) {
                ToastUtils.getInstance().showGlobalShort("需要获取位置信息权限才能读取Wi-Fi");
            }
        }, Manifest.permission.ACCESS_FINE_LOCATION);
    }


}
