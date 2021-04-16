package com.coocaa.tvpi.module.share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.businessstate.object.CmdData;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.connect.callback.ConnectCallbackImpl;
import com.coocaa.smartscreen.data.channel.events.ConnectEvent;
import com.coocaa.smartscreen.data.channel.events.DongleInfoEvent;
import com.coocaa.smartscreen.data.channel.events.UnbindEvent;
import com.coocaa.smartscreen.data.device.BindCodeMsg;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.callback.RepositoryCallback;
import com.coocaa.smartscreen.repository.service.BindCodeRepository;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.swaiotos.virtualinput.utils.VirtualInputUtils;
import com.coocaa.tvpi.event.NetworkEvent;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.connection.ScanActivity2;
import com.coocaa.tvpi.module.connection.WifiConnectActivity;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.local.album2.PreviewActivityW7;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.mine.EditDeviceNameActivity;
import com.coocaa.tvpi.module.mine.view.VerificationCodeDialog2;
import com.coocaa.tvpi.module.share.view.ShareFunctionView;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpilib.R;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.king.zxing.util.CodeUtils;
import com.makeramen.roundedimageview.RoundedImageView;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.share.api.define.ShareObject;

import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_BOTH;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_LOCAL;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_NOTHING;
import static com.coocaa.smartscreen.connect.SSConnectManager.NORMAL;
import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_APPSTATE;

public class ShareCodeActivity extends BaseActivity {

    private static final String TAG = ShareCodeActivity.class.getSimpleName();

    private enum STATUS {
        NOT_CONNECTED,
        CONNECTING,
        CONNECTED,
        CONNECT_ERROR,
        CONNECT_NOT_SAME_WIFI
    }

    private final HashMap<Character, Integer> bindCodeMap = new HashMap<>();
    private final List<ImageView> imgNumList = new ArrayList<>();

    private ImageView imgQRCode, imgLoadingCode, imgBgQRCode, imgLinkError, imgEditName;
    private ProgressBar loadProgress;
    private RoundedImageView deviceIcon;
    private TextView tvDeviceName, tvDeviceStatus, tvLoadingError, tvReconnect;
    private Button btnDisconnect, btnChangeDevice;
    private LinearLayout bindCodeLayout, shareCodeLayout, zoomCodeLayout;
    private CommonTitleBar titleBar;
    private ShareFunctionView shareFunctionView;
    private MyShare share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_code);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        EventBus.getDefault().register(this);
        initView();
        initListener();
        SSConnectManager.getInstance().addConnectCallback(connectCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        initData();
        updateDeviceInfoUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        SSConnectManager.getInstance().removeConnectCallback(connectCallback);
    }

    @Override
    public void finish() {
        super.finish();
        //先屏蔽退出动画，部分手机上效果有差异
//        overridePendingTransition(0, 0);
//        overridePendingTransition(0, R.anim.push_bottom_out);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NetworkEvent event) {
        Log.d(TAG, "onEvent: 收到网络变化");
        updateDeviceInfoUI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DongleInfoEvent event) {
        Log.d(TAG, "onEvent: isSystemUpgradeExist" + event.getIsSystemUpgradeExist());
        updateSystemSet(event.getIsSystemUpgradeExist());
    }

    private void initData() {
        //初始化数据
        share = new WebShare(new ShareObject());
        share.setTitle("共享屏");
        share.setDescription("你的好友正在邀请你连接共享屏，点击即可连接共享屏");
        loadBindCode();
        loadDongleInfo();
        Log.d(TAG, "initData: ");
        updateDeviceInfoUI();
    }

    private void loadDongleInfo() {
        int connectState = SSConnectManager.getInstance().getConnectState();
        final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        Log.d(TAG, "pushToTv: connectState" + connectState);
        Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);
        //未连接
        if (connectState == CONNECT_NOTHING || deviceInfo == null) {
            return;
        }
        //本地连接不通
        if (!(connectState == CONNECT_LOCAL || connectState == CONNECT_BOTH)) {
            return;
        }
        CmdData data = new CmdData("cmdToDongle", CmdData.CMD_TYPE.STATE.toString(), "getSystemUpgradeState");
        String cmd = data.toJson();
        SSConnectManager.getInstance().sendTextMessage(cmd, TARGET_APPSTATE, -1);
    }

    private void loadBindCode() {
        Log.d(TAG, "loadBindCode: ");
        imgQRCode.setImageBitmap(null);
        imgBgQRCode.setImageResource(R.drawable.bg_gray_round_4);
        loadProgress.setVisibility(View.VISIBLE);
        imgLoadingCode.setVisibility(View.VISIBLE);
        bindCodeLayout.setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.GONE);
        String accessToken = UserInfoCenter.getInstance().getAccessToken();
        if (TextUtils.isEmpty(accessToken)) {
            showLoadError();
            return;
        }
        Device device = SSConnectManager.getInstance().getHistoryDevice();
        if (device != null && device.getInfo() != null) {
            TVDeviceInfo deviceInfo = (TVDeviceInfo) device.getInfo();
            Repository.get(BindCodeRepository.class)
                    .getBindCode(accessToken, deviceInfo.activeId, device.getSpaceId())
                    .setCallback(new RepositoryCallback.Default<BindCodeMsg>() {
                        @Override
                        public void onSuccess(BindCodeMsg success) {
                            showBindCode(success);
                        }

                        @Override
                        public void onFailed(Throwable e) {
                            showLoadError();
                            Log.d(TAG, "loadBindCode onFailed: " + e.toString());
                            updateDeviceInfoUI();
                        }
                    });
        } else {
            ToastUtils.getInstance().showGlobalShort("请先连接设备");
            showLoadError();
        }
    }

    private void initView() {
        shareCodeLayout = findViewById(R.id.layout_share_code_weixin);
        zoomCodeLayout = findViewById(R.id.layout_share_code_zoom);
        imgQRCode = findViewById(R.id.qr_code_img);
        imgLinkError = findViewById(R.id.link_error_img);
        tvLoadingError = findViewById(R.id.tv_load_error);
        deviceIcon = findViewById(R.id.device_icon);
        tvDeviceName = findViewById(R.id.device_name);
        tvDeviceStatus = findViewById(R.id.tv_device_connect_status);
        btnDisconnect = findViewById(R.id.btn_disconnect);
        btnChangeDevice = findViewById(R.id.btn_change_device);
        loadProgress = findViewById(R.id.code_loading_progress);
        imgLoadingCode = findViewById(R.id.loading_code_img);
        bindCodeLayout = findViewById(R.id.bind_code_layout);
        imgBgQRCode = findViewById(R.id.bg_qr_code_img);
        imgEditName = findViewById(R.id.edit_name_img);
        tvReconnect = findViewById(R.id.tv_device_reconnect);
        titleBar = findViewById(R.id.share_code_title_bar);
        shareFunctionView = findViewById(R.id.share_function);
        imgNumList.add(findViewById(R.id.num_no1_img));
        imgNumList.add(findViewById(R.id.num_no2_img));
        imgNumList.add(findViewById(R.id.num_no3_img));
        imgNumList.add(findViewById(R.id.num_no4_img));
        imgNumList.add(findViewById(R.id.num_no5_img));
        imgNumList.add(findViewById(R.id.num_no6_img));
        imgNumList.add(findViewById(R.id.num_no7_img));
        imgNumList.add(findViewById(R.id.num_no8_img));
        bindCodeMap.put('0', R.drawable.icon_bind_code_zero);
        bindCodeMap.put('1', R.drawable.icon_bind_code_one);
        bindCodeMap.put('2', R.drawable.icon_bind_code_two);
        bindCodeMap.put('3', R.drawable.icon_bind_code_three);
        bindCodeMap.put('4', R.drawable.icon_bind_code_four);
        bindCodeMap.put('5', R.drawable.icon_bind_code_five);
        bindCodeMap.put('6', R.drawable.icon_bind_code_six);
        bindCodeMap.put('7', R.drawable.icon_bind_code_seven);
        bindCodeMap.put('8', R.drawable.icon_bind_code_eight);
        bindCodeMap.put('9', R.drawable.icon_bind_code_nine);
    }

    private void initListener() {
        shareCodeLayout.setOnClickListener(listener);
        zoomCodeLayout.setOnClickListener(listener);
        btnDisconnect.setOnClickListener(listener);
        imgQRCode.setOnClickListener(listener);
        tvReconnect.setOnClickListener(listener);
        titleBar.setOnClickListener(titleBarListener);
        imgEditName.setOnClickListener(listener);
        btnChangeDevice.setOnClickListener(listener);
    }

    private void showBindCode(BindCodeMsg data) {
        imgLoadingCode.setVisibility(View.GONE);
        loadProgress.setVisibility(View.INVISIBLE);
        bindCodeLayout.setVisibility(View.VISIBLE);
        imgQRCode.setVisibility(View.VISIBLE);
        imgBgQRCode.setImageResource(R.drawable.bg_b_1_round_4);
        imgQRCode.setEnabled(false);
        showQRCode(data);
        //"np://com.coocaa.smart.share_code/index?action=smart_screen&bindCode=10942885&type=share"
        Uri.Builder shareUri = new Uri.Builder();
        shareUri.scheme("np").authority("main").path("index");
        shareUri.appendQueryParameter("bc", data.getBindCode());
        shareUri.appendQueryParameter("m", "fx");
        shareUri.appendQueryParameter("ct", "and");
        shareUri.appendQueryParameter("yw", "gxp");
        share.putExtra("bc", data.getBindCode());
        String uri = shareUri.build().toString();
        Log.d(TAG, "showBindCode: " + uri);
        share.setUrl(Uri.encode(uri));
    }

    private void showQRCode(BindCodeMsg data) {
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
//        Uri.Builder bindCodeUri = new Uri.Builder();
//        bindCodeUri.scheme("https").authority("ccss.tv");
//        bindCodeUri.appendQueryParameter("m", "fx");
//        bindCodeUri.appendQueryParameter("bc", data.getBindCode());
//        bindCodeUri.appendQueryParameter("ct", "and");
//        bindCodeUri.appendQueryParameter("yw", "gxp");
        //配置参数
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //容错级别
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        //设置空白边距的宽度
        hints.put(EncodeHintType.MARGIN, 0);
        imgQRCode.setImageBitmap(CodeUtils.createQRCode(data.getUrl(), DimensUtils.dp2Px(this, 135), null, 0.2f, hints));
        if (data.getBindCode().length() != imgNumList.size()) {
            showLoadError();
            return;
        }
        for (int i = 0; i < data.getBindCode().length(); i++) {
            Integer resourceId = bindCodeMap.get(data.getBindCode().charAt(i));
            if (resourceId != null) {
                imgNumList.get(i).setImageResource(resourceId);
            }
        }
    }

    private void showLoadError() {
        imgQRCode.setEnabled(true);
        imgQRCode.setImageBitmap(null);
        loadProgress.setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.VISIBLE);
    }

    private void updateSystemSet(boolean isSystemUpgradeExist) {
        shareFunctionView.setUpdateIconVisible(isSystemUpgradeExist);
    }


    private void updateDeviceInfoUI() {
        Log.d(TAG, "updateDeviceInfoUI: ");
        STATUS status;
        if (SSConnectManager.getInstance().getHistoryDevice() == null) {
            status = STATUS.NOT_CONNECTED;
        } else if (SSConnectManager.getInstance().isConnecting()) {
            Log.d(TAG, "updateDeviceInfoUI: isConnecting");
            status = STATUS.CONNECTING;
        } else {
            switch (SSConnectManager.getInstance().getConnectState()) {
                case SSConnectManager.CONNECT_BOTH:
                    status = STATUS.CONNECTED;
                    break;
                case SSConnectManager.CONNECT_LOCAL:
                    status = STATUS.CONNECTED;
                    break;
                case SSConnectManager.CONNECT_SSE:
                    status = STATUS.CONNECT_NOT_SAME_WIFI;
                    break;
                case SSConnectManager.CONNECT_NOTHING:
                    if (SSConnectManager.getInstance().isHistoryDeviceValid()) {
                        status = STATUS.CONNECT_ERROR;
                    } else {
                        status = STATUS.NOT_CONNECTED;
                    }
                    break;
                default:
                    status = STATUS.NOT_CONNECTED;
                    break;
            }
        }
        setUIStatus(status);
    }

    private void setUIStatus(ShareCodeActivity.STATUS status) {
        HomeUIThread.execute(new Runnable() {
            @Override
            public void run() {
                String deviceName = SSConnectManager.getInstance().getDeviceName(SSConnectManager.getInstance().getHistoryDevice());
                Log.d(TAG, "setUIStatus: state = " + status.toString() + " deviceName = " + deviceName);
                switch (status) {
                    case NOT_CONNECTED:
                        tvReconnect.setVisibility(View.GONE);
                        imgLinkError.setVisibility(View.VISIBLE);
                        tvDeviceStatus.setVisibility(View.VISIBLE);
                        btnDisconnect.setVisibility(View.VISIBLE);
                        tvDeviceStatus.setText("连接异常");
                        tvDeviceStatus.setTextColor(getResources().getColor(R.color.color_connect_not_same_wifi));
                        break;
                    case CONNECTING:
                        tvReconnect.setVisibility(View.GONE);
                        tvDeviceStatus.setVisibility(View.VISIBLE);
                        btnDisconnect.setVisibility(View.GONE);
                        String tips = "正在连接";
                        if (!TextUtils.isEmpty(deviceName)) {
                            tips += "“" + deviceName + "”";
                        }
                        tvDeviceStatus.setTextColor(getResources().getColor(R.color.color_connect_not_same_wifi));
                        tvDeviceStatus.setText(tips);
                        tvDeviceName.setText(deviceName);
                        break;
                    case CONNECTED:
                        setDeviceIcon();
                        tvReconnect.setVisibility(View.GONE);
                        tvDeviceStatus.setVisibility(View.INVISIBLE);
                        imgLinkError.setVisibility(View.INVISIBLE);
                        btnDisconnect.setVisibility(View.VISIBLE);
                        tvDeviceName.setText(deviceName);
                        break;
                    case CONNECT_ERROR:
                        setDeviceIcon();
                        tvReconnect.setVisibility(View.GONE);
                        imgLinkError.setVisibility(View.VISIBLE);
                        tvDeviceStatus.setVisibility(View.VISIBLE);
                        btnDisconnect.setVisibility(View.VISIBLE);
                        tvDeviceName.setText(deviceName);
                        tvDeviceStatus.setText("连接异常");
                        tvDeviceStatus.setTextColor(getResources().getColor(R.color.color_connect_not_same_wifi));
                        break;
                    case CONNECT_NOT_SAME_WIFI:
                        setDeviceIcon();
                        tvReconnect.setVisibility(View.VISIBLE);
                        imgLinkError.setVisibility(View.VISIBLE);
                        tvDeviceStatus.setVisibility(View.VISIBLE);
                        btnDisconnect.setVisibility(View.VISIBLE);
                        tvDeviceName.setText(deviceName);
                        tvDeviceStatus.setText("未连接共享屏WiFi");
                        tvDeviceStatus.setTextColor(getResources().getColor(R.color.color_connect_not_same_wifi));
                        break;
                    default:
                        Log.d(TAG, "Unexpected value: " + status);
                        break;
                }
            }
        });
    }


    private void setDeviceIcon() {
        Device device = SSConnectManager.getInstance().getHistoryDevice();
        if (null != device && !TextUtils.isEmpty(device.getMerchantIcon())
                && this != null && !this.isDestroyed()) {
            Log.d(TAG, "setDeviceIcon: "+device.getMerchantIcon());
            GlideApp.with(this)
                    .load(device.getMerchantIcon())
                    .into(deviceIcon);
        } else {
            deviceIcon.setImageResource(R.drawable.icon_defalut_device);
        }
    }

    private void codeZoom() {
        int connectState = SSConnectManager.getInstance().getConnectState();
        final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        Log.d(TAG, "pushToTv: connectState" + connectState);
        Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);
        //未连接
        if (connectState == CONNECT_NOTHING || deviceInfo == null) {
            ConnectDialogActivity.start(ShareCodeActivity.this);
            return;
        }
        //本地连接不通
        if (!(connectState == CONNECT_LOCAL || connectState == CONNECT_BOTH)) {
            WifiConnectActivity.start(ShareCodeActivity.this);
            return;
        }
        CmdData data = new CmdData("showBigQRCodeWindow", CmdData.CMD_TYPE.STATE.toString(), "");
        String cmd = data.toJson();
        SSConnectManager.getInstance().sendTextMessage(cmd, TARGET_APPSTATE, -1);
        ToastUtils.getInstance().showGlobalShort("请查看大屏二维码，扫码连接");
    }

    private void shareBindCode() {
        SHARE_MEDIA shareMedia = SHARE_MEDIA.WEIXIN;
        if (tvLoadingError.getVisibility() == View.VISIBLE) {
            ToastUtils.getInstance().showGlobalShort("加载失败，无法分享");
            return;
        }
        if (!UMShareAPI.get(this).isInstall(this, shareMedia)) {
            ToastUtils.getInstance().showGlobalShort("未安装微信");
            return;
        }
        share.share(this, shareMedia);
    }

    private void disConnect() {

        SSConnectManager.getInstance().leaveRoom();
        SSConnectManager.getInstance().disconnect();
        SSConnectManager.getInstance().clearHistoryDevice();
        ToastUtils.getInstance().showGlobalShort("已断开连接");
        finish();

//        new SDialog(ShareCodeActivity.this, "断开连接", "是否确认断开与当前设备的连接", R.string.cancel, R.string.confirm_disconnect,
//                new SDialog.SDialog2Listener() {
//                    @Override
//                    public void onClick(boolean left, View view) {
//                        if (!left) {
//                            SSConnectManager.getInstance().leaveRoom();
//                            SSConnectManager.getInstance().disconnect();
//                            SSConnectManager.getInstance().clearHistoryDevice();
//                            ToastUtils.getInstance().showGlobalShort("已断开连接");
//                            finish();
//                        }
//                    }
//                }).show();
    }

    private void reconnect() {
        WifiConnectActivity.start(ShareCodeActivity.this);
    }

    private void changeName() {
        int connectState = SSConnectManager.getInstance().getConnectState();
        final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        Log.d(TAG, "pushToTv: connectState" + connectState);
        Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);
        //未连接
        if (connectState == CONNECT_NOTHING || deviceInfo == null) {
            ConnectDialogActivity.start(ShareCodeActivity.this);
            return;
        }

        Device device = SSConnectManager.getInstance().getHistoryDevice();
        if (device == null) {
            return;
        }

        //C端设备（无商家ID）
        if (TextUtils.isEmpty(device.getMerchantId())) {
            Intent intent = new Intent(ShareCodeActivity.this, EditDeviceNameActivity.class);
            startActivity(intent);
        } else {
            //  B端设备（有商家ID）
            VerificationCodeDialog2 verifyCodeDialog = new VerificationCodeDialog2();
            verifyCodeDialog.setVerifyCodeListener(() -> {
                Intent intent = new Intent(ShareCodeActivity.this, EditDeviceNameActivity.class);
                startActivity(intent);
            });

            if (!verifyCodeDialog.isAdded()) {
                verifyCodeDialog.show(getSupportFragmentManager(), "editDeviceNameDialog");
            }
        }
    }

    private void changeDevice() {
        ScanActivity2.start(this, 1, false);
    }

    private final ConnectCallbackImpl connectCallback = new ConnectCallbackImpl() {

        @Override
        public void onCheckConnect(ConnectEvent connectEvent) {
            updateDeviceInfoUI();
        }

        @Override
        public void onConnecting() {
            Log.d(TAG, "connectCallback onConnecting: ");
        }

        @Override
        public void onSuccess(ConnectEvent connectEvent) {
            Log.d(TAG, "connectCallback onSuccess: " + connectEvent);
            updateDeviceInfoUI();
        }

        @Override
        public void onFailure(ConnectEvent connectEvent) {
            Log.d(TAG, "connectCallback onFailure: " + connectEvent);
            updateDeviceInfoUI();
        }

        @Override
        public void onUnbind(UnbindEvent unbindEvent) {
            Log.d(TAG, "connectCallback onUnbind: ");
            updateDeviceInfoUI();
        }

        @Override
        public void onUnbindByDevice(UnbindEvent unbindEvent) {
            Log.d(TAG, "connectCallback onUnbindByDevice: " + unbindEvent);
            updateDeviceInfoUI();
        }

        @Override
        public void onDeviceOnLine(Device device) {
            Log.d(TAG, "connectCallback onDeviceOnLine: ");
            updateDeviceInfoUI();
        }

        @Override
        public void onDeviceOffLine(Device device) {
            Log.d(TAG, "connectCallback onDeviceOffLine: ");
            updateDeviceInfoUI();
        }

        @Override
        public void onDeviceUpdate(Device device) {
            Log.d(TAG, "connectCallback onDeviceUpdate: ");
            updateDeviceInfoUI();
        }

        @Override
        public void onSessionDisconnect(Session session) {
            Log.d(TAG, "connectCallback onSessionDisconnect: ");
            updateDeviceInfoUI();
        }

        @Override
        public void onDeviceReflushUpdate(List<Device> devices) {
            Log.d(TAG, "connectCallback onDeviceReflushUpdate: ");
            updateDeviceInfoUI();
        }

        @Override
        public void onSessionConnect(Session session) {
            Log.d(TAG, "connectCallback onSessionConnect: ");
            updateDeviceInfoUI();
        }

        @Override
        public void onSessionUpdate(Session session) {
            Log.d(TAG, "connectCallback onSessionUpdate: ");
            updateDeviceInfoUI();
        }

        @Override
        public void loginState(int code, String info) {
            Log.d(TAG, "loginState: ");
            updateDeviceInfoUI();
        }
    };

    private final View.OnClickListener listener = v -> {
        if (v.getId() == R.id.layout_share_code_weixin) {
            shareBindCode();
        } else if (v.getId() == R.id.layout_share_code_zoom) {
            codeZoom();
        } else if (v.getId() == R.id.btn_disconnect) {
            VirtualInputUtils.playVibrate();
            disConnect();
        } else if (v.getId() == R.id.qr_code_img) {
            loadBindCode();
        } else if (v.getId() == R.id.tv_device_reconnect) {
            reconnect();
        } else if (v.getId() == R.id.edit_name_img) {
            changeName();
        } else if (v.getId() == R.id.btn_change_device) {
            changeDevice();
        }
    };


    private final CommonTitleBar.OnClickListener titleBarListener = position -> {
        if (position == CommonTitleBar.ClickPosition.LEFT) {
            finish();
        }
    };

    //二维码保存的相关逻辑
//    private void saveLocalBitmap() {
//        Bitmap cacheBitmap = convertViewToBitmap(view);
//        if (cacheBitmap == null) {
//            Log.i(TAG, "cacheBitmap=null");
//            return;
//        }
//        Bitmap saveBitmap = Bitmap.createBitmap(cacheBitmap);
//        if (saveBitmap == null) {
//            Log.i(TAG, "newBitmap=null");
//            return;
//        }
//        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
//            @Override
//            public void permissionGranted(String[] permission) {
//                saveFile(saveBitmap);
//            }
//
//            @Override
//            public void permissionDenied(String[] permission) {
//                ToastUtils.getInstance().showGlobalLong("保存失败");
//            }
//        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
//    }
//
//    private Bitmap convertViewToBitmap(View view) {
//        view.buildDrawingCache();
//        Bitmap bitmap = view.getDrawingCache();
//        if (bitmap != null) {
//            Bitmap.Config cfg = bitmap.getConfig();
//            Log.d(TAG, "----------------------- cache.getConfig() = " + cfg);
//        }
//        return bitmap;
//    }
//
//    private void saveFile(Bitmap saveBitmap) {
//        File dir = new File(PATH);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//
//        File photoFile = new File(PATH, System.currentTimeMillis() + ".jpg");
//        FileOutputStream fileOutputStream = null;
//        try {
//            fileOutputStream = new FileOutputStream(photoFile);
//            if (saveBitmap != null) {
//                if (saveBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)) {
//                    fileOutputStream.flush();
//                    ToastUtils.getInstance().showGlobalLong("保存成功");
//                }
//            }
//        } catch (FileNotFoundException e) {
//            photoFile.delete();
//            e.printStackTrace();
//            ToastUtils.getInstance().showGlobalLong("保存失败");
//        } catch (IOException e) {
//            photoFile.delete();
//            e.printStackTrace();
//            ToastUtils.getInstance().showGlobalLong("保存失败");
//        } finally {
//            try {
//                fileOutputStream.close();
//                MediaScannerConnection.scanFile(ShareCodeActivity.this, new String[]{PATH}, null, null);
//                //刷新
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}