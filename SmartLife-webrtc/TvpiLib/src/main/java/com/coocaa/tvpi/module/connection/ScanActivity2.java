package com.coocaa.tvpi.module.connection;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.views.SDialog;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.connect.callback.BindCallback;
import com.coocaa.smartscreen.connect.callback.ConnectCallbackImpl;
import com.coocaa.smartscreen.data.channel.events.ConnectEvent;
import com.coocaa.smartscreen.data.channel.events.UnbindEvent;
import com.coocaa.smartscreen.network.api.SkyworthIotService;
import com.coocaa.smartscreen.utils.NetworkUtils;
import com.coocaa.tvpi.event.ScanFastFinishEvent;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.module.connection.adapter.DeviceAdapter2;
import com.coocaa.tvpi.module.homepager.IntentActivity;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.log.ConnectDeviceEvent;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.module.login.LoginActivity;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.mall.MallDetailActivity;
import com.coocaa.tvpi.util.NetworkUtil;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.coocaa.tvpi.util.UriUtils;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpi.view.webview.InputCodeView;
import com.coocaa.tvpilib.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.huawei.hms.hmsscankit.OnResultCallback;
import com.huawei.hms.hmsscankit.RemoteView;
import com.huawei.hms.ml.scan.HmsScan;
import com.king.zxing.util.CodeUtils;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.retrofiturlmanager.RetrofitUrlManager;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;
import swaiotos.channel.iot.ss.session.Session;

import static com.coocaa.tvpi.common.UMengEventId.DEVICE_ADD;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;

public class ScanActivity2 extends BaseActivity implements UnVirtualInputable {
    private static final String TAG = ScanActivity2.class.getSimpleName();
    public static final int REQUEST_CODE_PHOTO = 0X02;

    public static final String KEY_MODE = "KEY_MDOE";
    public static final String KEY_NEED_EXPAND_LIST = "KEY_NEED_EXPAND_LIST";
    private static final int MODE_SCAN = 1;
    private static final int MODE_INPUT = 2;
    private int curMode = -1;
    private int initMode = MODE_SCAN;
    private boolean needExpandList = false;

    private static final String SHORT_QR_HOST = "https://s.skysrt.com";
    private static final String NEW_HOST = "https://ccss.tv";

    private long connectTime;
    private String connectSource = "";

    private boolean isConnecting = false;

    private String scanApplet;

    private ImageView ivFlash;
    private TextView tvFlash;
    private boolean isFlashOpen = false;

    private RemoteView remoteView;
    private Bundle savedInstanceState;

    private View scanLayout;
    private InputCodeView inputCodeView;
    private View scanBtn, codeBtn;
    private TextView scanTV, codeTV;
    private View scanIndicator, codeIndicator;
    private ImageView imgScan;
    private View animLayout;

    private RecyclerView recyclerView;
    private DeviceAdapter2 adapter;

    private volatile boolean connectSuccess = false;

    private View deviceListLayout;
    private BottomSheetBehavior bottomSheetBehavior;

    public static void start(Context context) {
        start(context, MODE_SCAN, false);
    }

    public static void start(Context context, int mode, boolean needExpandList) {
        if (!UserInfoCenter.getInstance().isLogin()) {
            LoginActivity.start(context);
            ToastUtils.getInstance().showGlobalShort("?????????");
            return;
        }
        Intent starter = new Intent(context, ScanActivity2.class);
        starter.putExtra(KEY_MODE, mode);
        starter.putExtra(KEY_NEED_EXPAND_LIST, needExpandList);
        if(!(context instanceof Activity)) {
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        StatusBarHelper.setStatusBarLightMode(ScanActivity2.this);
        StatusBarHelper.translucent(ScanActivity2.this);
        //????????????????????????????????????????????????
//        getWindow().getDecorView().post(new Runnable() {
//            @Override
//            public void run() {
//                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//            }
//        });
        setContentView(R.layout.activity_scan2);
        SSConnectManager.getInstance().addConnectCallback(connectCallback);

        this.savedInstanceState = savedInstanceState;
        if (null != getIntent()) {
            initMode = getIntent().getIntExtra(KEY_MODE, MODE_SCAN);
            needExpandList = getIntent().getBooleanExtra(KEY_NEED_EXPAND_LIST, false);
        }

        initView();
        changeMode(initMode);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (null != remoteView) {
            remoteView.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != remoteView) {
            remoteView.onResume();
        }
        startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != remoteView) {
            remoteView.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != remoteView) {
            remoteView.onStop();
        }
    }


    @Override
    protected void onDestroy() {
        if (null != remoteView) {
            remoteView.onDestroy();
        }
        SSConnectManager.getInstance().removeConnectCallback(connectCallback);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_PHOTO) {
                if (data != null) {
                    parsePhoto(data);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent: " + event.toString());
        stopScanAnimation();
        return super.onTouchEvent(event);
    }

    private void initView() {
        ImageView ivBack = findViewById(R.id.iv_back);
        TextView tvAlbum = findViewById(R.id.tv_album);
        imgScan = findViewById(R.id.scan_img);
        ivFlash = findViewById(R.id.iv_flash);
        tvFlash = findViewById(R.id.tv_flash);
        animLayout = findViewById(R.id.anim_layout);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPhotoCode();
            }
        });

        //?????????
        ivFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFlashOpen = !isFlashOpen;
            }
        });

        scanBtn = findViewById(R.id.scan_btn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMode(MODE_SCAN);
            }
        });
        codeBtn = findViewById(R.id.code_btn);
        codeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMode(MODE_INPUT);
            }
        });

        scanTV = findViewById(R.id.scan_tv);
        codeTV = findViewById(R.id.code_tv);
        scanIndicator = findViewById(R.id.scan_indicator);
        codeIndicator = findViewById(R.id.code_indicator);

        scanLayout = findViewById(R.id.scan_layout);
        inputCodeView = findViewById(R.id.input_code_view);
        inputCodeView.setInputCallback(new InputCodeView.InputCallback() {
            @Override
            public void onConnectBtnClick(String code) {
                handleBind(code);
            }
        });

        deviceListLayout = findViewById(R.id.device_list_layout);
        int height = DimensUtils.getDeviceHeight(this) - DimensUtils.dp2Px(this, 98);
        ViewGroup.LayoutParams layoutParams = deviceListLayout.getLayoutParams();
        layoutParams.height = height;
        deviceListLayout.setLayoutParams(layoutParams);

        bottomSheetBehavior = BottomSheetBehavior.from(deviceListLayout);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                Log.d(TAG, "onStateChanged: " + newState);
                if (newState == STATE_EXPANDED) {
                    submitDeviceListExpandEvent();
                    if (curMode == MODE_SCAN) {
                        stopScan();
                    }
                } else if (newState == STATE_COLLAPSED) {
                    startScan();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.d("BottomSheetDemo", "slideOffset:" + slideOffset);
            }
        });
        if (needExpandList) {
            bottomSheetBehavior.setState(STATE_EXPANDED);
        }

        initDeviceListView();
    }

    private void initScan() {
        Rect rect = new Rect();
        rect.left = 0;
        rect.right = DimensUtils.getDeviceWidth(this);
        rect.top = 0;
        rect.bottom = DimensUtils.getDeviceHeight(this);

        //initialize RemoteView instance, and set calling back for scanning result
        remoteView = new RemoteView.Builder().setContext(this).setBoundingBox(rect).setFormat(HmsScan.ALL_SCAN_TYPE).build();
        remoteView.onCreate(savedInstanceState);
        remoteView.setOnResultCallback(onResultCallback);

        //add remoteView to framelayout
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = findViewById(R.id.rim);
        frameLayout.addView(remoteView, params);

        remoteView.onStart();
        remoteView.onResume();
    }

    private OnResultCallback onResultCallback = new OnResultCallback() {
        @Override
        public void onResult(HmsScan[] result) {
            if (result != null && result.length > 0 && result[0] != null && !TextUtils.isEmpty(result[0].getOriginalValue())) {
//                ToastUtils.getInstance().showGlobalLong(result[0].getOriginalValue());
                String resultStr = result[0].getOriginalValue();
                Log.d(TAG, "onResult: " + resultStr);
                Log.d(TAG, "onResult: curMode = " + curMode);
                if(curMode == MODE_SCAN) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopScan();
                        }
                    });
                    handleScanResult(resultStr);
                }
            }
        }
    };

    private void startScan() {
        Log.d(TAG, "startScan: ");
        if (bottomSheetBehavior.getState() == STATE_EXPANDED) {
            Log.d(TAG, "startScan: bottomSheetBehavior.getState() == STATE_EXPANDED");
            return;
        }
        if (curMode == MODE_SCAN) {
            if (remoteView != null) {
                remoteView.resumeContinuouslyScan();
                startScanAnimation();
            } else {
                initScan();
            }
        }
    }

    private void stopScan() {
        Log.d(TAG, "stopScan: ");
        if (remoteView != null) {
            remoteView.pauseContinuouslyScan();
            stopScanAnimation();
        }
    }

    private void initDeviceListView() {
        recyclerView = findViewById(R.id.device_list_recyclerview);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
//        CommonVerticalItemDecoration decoration = new CommonVerticalItemDecoration(DimensUtils.dp2Px(this, 10f), DimensUtils.dp2Px(this, 10f), 0);
//        recyclerView.addItemDecoration(decoration);
        adapter = new DeviceAdapter2();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new DeviceAdapter2.OnItemClickListener() {
            @Override
            public void onItemClick(int positon, Object object) {
                if (!NetworkUtils.isAvailable(ScanActivity2.this)) {
                    Log.d(TAG, "connect: ?????????????????????");
                    ToastUtils.getInstance().showGlobalShort("?????????????????????");
                    return;
                }

                //???????????????????????????????????????
                if (isConnecting) {
                    return;
                }
                isConnecting = true;

                if (object instanceof Device) {
                    Device device = (Device) object;

                    Session session = SSConnectManager.getInstance().getConnectSession();
                    if (device.getLsid() != null
                            && session != null
                            && device.getLsid().equals(session.getId())
                            && SSConnectManager.getInstance().isConnected()) {
                        ToastUtils.getInstance().showGlobalShort("?????????");
                        isConnecting = false;
                        finish();
                        return;
                    }

                    if (device.getStatus() == 1) {
                        Log.d(TAG, "onItemClick: ????????????");
                        adapter.showConnecting(positon);
                        SSConnectManager.getInstance().selectDevice(device);
                        SSConnectManager.getInstance().connect(device);
                        connectSource = "click_device_item_connect";
                    } else {
                        /*if (((TVDeviceInfo) device.getInfo()).blueSupport == 0) {
                            handleBle(device);
                        }*/
                        ToastUtils.getInstance().showGlobalShort("?????????????????????????????????????????????");
                        isConnecting = false;
                    }
                    submitItemClickEvent(device);
                }
            }

            @Override
            public void onDisconnectClick(Object object) {
                submitItemBtnClickEvent((Device) object, "disconnect");
                SSConnectManager.getInstance().leaveRoom();
                SSConnectManager.getInstance().disconnect();
                SSConnectManager.getInstance().clearHistoryDevice();
                ToastUtils.getInstance().showGlobalShort("???????????????");
                /*new SDialog(ScanActivity2.this, "????????????", "??????????????????????????????????????????", R.string.cancel, R.string.confirm_disconnect,
                        new SDialog.SDialog2Listener() {
                            @Override
                            public void onClick(boolean left, View view) {
                                if (!left) {
                                    SSConnectManager.getInstance().leaveRoom();
                                    SSConnectManager.getInstance().disconnect();
                                    SSConnectManager.getInstance().clearHistoryDevice();
                                    ToastUtils.getInstance().showGlobalShort("???????????????");
                                }
                            }
                        }).show();*/
            }

            @Override
            public void onUnbindClick(Object object) {
                submitItemBtnClickEvent((Device) object, "unbind");
                new SDialog(ScanActivity2.this, "????????????", "?????????????????????????????????????????????", R.string.cancel, R.string.confirm_unbind,
                        new SDialog.SDialog2Listener() {
                            @Override
                            public void onClick(boolean left, View view) {
                                if (!left) {
                                    Device device = (Device) object;
                                    if (device.getIsTemp() == 0) {//0:????????????
                                        Log.d(TAG, "onUnbindClick: device  = " + device.getLsid());
                                        SSConnectManager.getInstance().unbind("", device.getLsid(), 1);
                                    } else if (device.getIsTemp() == 1) { // 1???????????????
                                        Log.d(TAG, "onUnbindClick: tempDevice  = " + device.getLsid());
                                        SSConnectManager.getInstance().unbind("", device.getLsid(), 1);
                                    }

                                }
                            }
                        }).show();
            }
        });


        if (SSConnectManager.getInstance().isConnectedChannel()) {
            setList();
        } else {
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setList();
                }
            }, 2000);
        }
    }

    private void setList() {
        List<Device> devices = SSConnectManager.getInstance().getDeviceOnlineStatus();
        adapter.setList(devices);
    }

    private void startScanAnimation() {
        Log.d(TAG, "startScanAnimation: =====");
        imgScan.post(new Runnable() {
            @Override
            public void run() {
                imgScan.setVisibility(View.VISIBLE);
                AnimatorSet animatorSet  = new AnimatorSet();
                ObjectAnimator alpha = ObjectAnimator.ofFloat(imgScan,"alpha",0,1,1,1,0);
                alpha.setDuration(3000);
                alpha.setRepeatCount(ValueAnimator.INFINITE);
                int measuredHeight =animLayout.getMeasuredHeight();
                float h1 = measuredHeight / 4f;
                ObjectAnimator translationY = ObjectAnimator.ofFloat(imgScan, "translationY",
                        0,
                        h1,
                        2*h1,
                        3*h1,
                        4*h1);
                translationY.setDuration(3000);
                translationY.setRepeatCount(ValueAnimator.INFINITE);
                animatorSet.playTogether(alpha,translationY);
                animatorSet.start();
            }
        });
    }

    private void stopScanAnimation() {
        HomeUIThread.execute(new Runnable() {
            @Override
            public void run() {
                imgScan.clearAnimation();
                imgScan.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void changeMode(int mode) {
        if (curMode == mode)
            return;

        curMode = mode;
        if (mode == MODE_SCAN) {
            scanTV.setTextColor(getResources().getColor(R.color.color_white));
            codeTV.setTextColor(getResources().getColor(R.color.color_white_60));

            scanIndicator.setVisibility(View.VISIBLE);
            codeIndicator.setVisibility(View.GONE);

            scanLayout.setVisibility(View.VISIBLE);
            inputCodeView.setVisibility(View.GONE);

            startScan();
        } else if (mode == MODE_INPUT) {
            scanTV.setTextColor(getResources().getColor(R.color.color_white_60));
            codeTV.setTextColor(getResources().getColor(R.color.color_white));

            scanLayout.setVisibility(View.GONE);
            inputCodeView.setVisibility(View.VISIBLE);

            scanIndicator.setVisibility(View.GONE);
            codeIndicator.setVisibility(View.VISIBLE);

            stopScan();
        }
    }

    private void handleScanResult(String result) {
        Log.d(TAG, "handleScanResult: " + result);

        if (!NetworkUtil.isAvailable(ScanActivity2.this)) {
            ToastUtils.getInstance().showGlobalShort("????????????");
            finish();
            return;
        }

        Map<String, String> map = getURLRequest(result);
        Log.d(TAG, "map: " + map);

        if(!TextUtils.isEmpty(map.get("applet"))) {
            scanApplet = map.get("applet");
        }

        if (result.contains(NEW_HOST)) {
            handleNewScanResult(result, map);
        } else if (!TextUtils.isEmpty(map.get("action"))) {
            handleAction(map.get("action"), map);
        } else if (!TextUtils.isEmpty(map.get("bindCode"))) {
            handleBind(map.get("bindCode"));
        } else if (!TextUtils.isEmpty(map.get("productId"))) {
            handleProduct(map.get("productId"));
        } else if (!TextUtils.isEmpty(map.get("id"))) {
            handleVideoCall(map.get("id"));
        } else if (!TextUtils.isEmpty(result)) {
            if (result.contains(SHORT_QR_HOST)) {
//                String path = result.replace(SHORT_QR_HOST + "/", "");
//                Log.d(TAG, "??????path = " + path);
                handleShortQR(result);
            } else if (result.contains("http")) {
                handleUrl(result);
            } else {
                ToastUtils.getInstance().showGlobalShort("???????????????");
                HomeIOThread.execute(1500, resumeCameraRunnable);
            }
        } else {
            ToastUtils.getInstance().showGlobalShort("???????????????");
            HomeIOThread.execute(1500, resumeCameraRunnable);
        }

    }

    private void handleNewScanResult(String result, Map<String, String> map) {
        Log.d(TAG, "handleNewScanResult: " + map);

        String m = map.get("m");

        if (!TextUtils.isEmpty(m)) {
            if (m.equals("sm") || m.equals("fx")) {//??????????????????
                if (!TextUtils.isEmpty(map.get("bc"))) {//?????????
                    handleBind(map.get("bc"));
                } else if (!TextUtils.isEmpty(map.get("sp"))) {//??????id??????
                    handleTempBind(map.get("sp"), 1);
                }
            } else if (m.equals("pw")) {//??????
                handleBle(map.get("mac"));
            } else {
                ToastUtils.getInstance().showGlobalShort("???????????????");
                HomeIOThread.execute(1500, resumeCameraRunnable);
            }
        } else {
            if(result.toLowerCase().startsWith("http://") || result.toLowerCase().startsWith("https://")) {
                //??????????????????????????????
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(result);
                intent.setData(content_url);
                startActivity(intent);
            } else {
                ToastUtils.getInstance().showGlobalShort("???????????????");
                HomeIOThread.execute(1500, resumeCameraRunnable);
            }
        }
    }

    private void handleAction(String action, Map<String, String> map) {
        Log.d(TAG, "handleAction: " + action + "   map:" + map);

        submitScanSuccess(action);

        if (action.equals("smart_screen")) {
            String mode = map.get("mode");
            if ("3".equals(mode)) {
                String mac = map.get("mac");
                handleBle(mac);
            } else /*if ("2".equals(mode))*/ {
                //???????????????mode??????
                String bindCode = map.get("bindCode");
                Log.d(TAG, "handleAction: bindcode" + bindCode);
                handleBind(bindCode);
            }
        }
    }

    private void handleBle(String mac) {
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                NoNetwortDialogActivity.start(ScanActivity2.this, mac);
                finish();
            }

            @Override
            public void permissionDenied(String[] permission) {
                ToastUtils.getInstance().showGlobalShort("??????????????????????????????????????????Wi-Fi");
            }
        }, Manifest.permission.ACCESS_FINE_LOCATION);
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
                            if (tvDeviceInfo.blueSupport  == 0) {
                                mac = tvDeviceInfo.MAC;
                            }
                            break;
                    }
                }
                NoNetwortDialogActivity.start(ScanActivity2.this, mac);
                finish();
            }

            @Override
            public void permissionDenied(String[] permission) {
                ToastUtils.getInstance().showGlobalShort("??????????????????????????????????????????Wi-Fi");
            }
        }, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void handleBind(String bindCode) {
        if (!UserInfoCenter.getInstance().isLogin()) {
            LoginActivity.start(this);
            return;
        }
        showLoading();
        SSConnectManager.getInstance().bind(bindCode, new BindCallback() {
            @Override
            public void onSuccess(String bindCode, Device device) {
                Log.d(TAG, "onSuccess: bindCode = " + bindCode + "   device = " + device);
                ToastUtils.getInstance().showGlobalShort("????????????");
                EventBus.getDefault().post(new ScanFastFinishEvent());
                dismissLoading();
                submitEvent("success");
                startScanApplet();
                connectSource = "scan_connect";
                finish();
            }

            @Override
            public void onFail(String bindCode, String errorType, String msg) {
                Log.d(TAG, "onFail: bindCode = " + bindCode + " errorType = " + errorType + " msg = " + msg);
                ToastUtils.getInstance().showGlobalShort("???????????????" + msg);
                HomeIOThread.execute(1500, resumeCameraRunnable);
                dismissLoading();
                submitEvent("fail");
            }
        });
    }

    private void handleTempBind(String uniqueId, int type) {
        if (!UserInfoCenter.getInstance().isLogin()) {
            LoginActivity.start(this);
            return;
        }
        showLoading();
        SSConnectManager.getInstance().tempBind(uniqueId, type, new BindCallback() {
            @Override
            public void onSuccess(String bindCode, Device device) {
                Log.d(TAG, "onSuccess: bindCode = " + bindCode + "   device = " + device);
                ToastUtils.getInstance().showGlobalShort("????????????");
                EventBus.getDefault().post(new ScanFastFinishEvent());
                dismissLoading();
                submitEvent("success");
                startScanApplet();
                connectSource = "scan_connect";
                finish();
            }

            @Override
            public void onFail(String bindCode, String errorType, String msg) {
                Log.d(TAG, "onFail: bindCode = " + bindCode + " errorType = " + errorType + " msg = " + msg);
                String failMsg = "???????????????" + msg;
                if (errorType.equals("20011")) {
                    failMsg = "?????????????????????????????????????????????";
                }
                ToastUtils.getInstance().showGlobalShort(failMsg);
                HomeIOThread.execute(1500, resumeCameraRunnable);
                dismissLoading();
                submitEvent("fail");
            }
        });
    }

    private void startScanApplet() {
        if(TextUtils.isEmpty(scanApplet))
            return ;
        try {
            boolean ret = IntentActivity.handleShareIntent(this, Uri.parse(scanApplet));
            Log.d(TAG, "startScanApplet, ret=" + ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleProduct(String productId) {
        MallDetailActivity.start(this, productId);
        finish();
    }

    private void handleVideoCall(String id) {
        finish();
    }

    private void handleUrl(String url) {
//        SimpleWebViewActivity.start(this, url);
        //????????????web???????????????web?????????runtime js-api????????????
        Log.d(TAG, "handleUrl : " + url);
        TvpiClickUtil.onClick(this, url);
        finish();
    }

    private void startPhotoCode() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(pickIntent, REQUEST_CODE_PHOTO);
    }

    private void parsePhoto(Intent data) {
        final String path = UriUtils.getImagePath(this, data);
        Log.d(TAG, "path:" + path);
        if (TextUtils.isEmpty(path)) {
            return;
        }
        //????????????
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                final String result = CodeUtils.parseCode(path);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "result:" + result);
                        ToastUtils.getInstance().showGlobalLong(result);
                    }
                });

            }
        });

    }

    private ConnectCallbackImpl connectCallback = new ConnectCallbackImpl() {

        @Override
        public void onConnecting() {
            Log.d(TAG, "onConnecting: ");
            connectTime = System.currentTimeMillis();
        }

        @Override
        public void onSuccess(ConnectEvent connectEvent) {
            Log.d(TAG, "connectCallback onSuccess: " + connectEvent);
            connectSuccess = true;
            Log.d(TAG, "onSuccess: " + isFinishing());
            if (!isFinishing()) {
                ToastUtils.getInstance().showGlobalShort("?????????");
            }
            submitManualConnectTime(true);
            finish();
        }

        @Override
        public void onFailure(ConnectEvent connectEvent) {
            Log.d(TAG, "connectCallback onFailure: " + connectEvent);
            ToastUtils.getInstance().showGlobalShort("??????????????????????????????");
            submitManualConnectTime(false);
            finish();
        }

        @Override
        public void onUnbind(UnbindEvent unbindEvent) {
            Log.d(TAG, "connectCallback onUnbind: ");
//            dismissLoading();
            if (null != unbindEvent && unbindEvent.isUnbinded) {
                Log.d(TAG, "onUnbind: " + unbindEvent.lsid + " = " + unbindEvent.isUnbinded);
                adapter.removeItem(unbindEvent.lsid);
                ToastUtils.getInstance().showGlobalShort("???????????????");
            } else {
                Log.d(TAG, "onUnbind: " + "?????????????????????" + unbindEvent.msg);
                ToastUtils.getInstance().showGlobalShort("?????????????????????" + unbindEvent.msg);
            }
        }

        @Override
        public void onUnbindByDevice(UnbindEvent unbindEvent) {
            Log.d(TAG, "connectCallback onUnbindByDevice: " + unbindEvent);
            if (null != unbindEvent && unbindEvent.isUnbinded) {
                Log.d(TAG, "onUnbind: " + unbindEvent.lsid + " = " + unbindEvent.isUnbinded);
                adapter.removeItem(unbindEvent.lsid);
            }
        }

        @Override
        public void onDeviceOnLine(Device device) {
            Log.d(TAG, "connectCallback onDeviceOnLine: " + device);
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    adapter.updateDevice(device);
                }
            });
        }

        @Override
        public void onDeviceOffLine(Device device) {
            Log.d(TAG, "connectCallback onDeviceOffLine: " + device);
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    adapter.updateDevice(device);
                }
            });
        }

        @Override
        public void onDeviceUpdate(Device device) {
            Log.d(TAG, "connectCallback onDeviceUpdate: " + device);
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    adapter.updateDevice(device);
                }
            });
        }

        @Override
        public void onSessionDisconnect(Session session) {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onDeviceReflushUpdate(List<Device> devices) {
            adapter.setList(devices);
        }

    };

    /**
     * ?????????url?????????????????????
     * ??? "index.jsp?Action=del&id=123"????????????Action:del,id:123??????map???
     *
     * @param URL url??????
     * @return url??????????????????
     */
    private Map<String, String> getURLRequest(String URL) {
        Map<String, String> mapRequest = new HashMap<String, String>();

        try {
            Uri uri = Uri.parse(URL);
            Set<String> keySet = uri.getQueryParameterNames();
            for(String s : keySet) {
                mapRequest.put(s, uri.getQueryParameter(s));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapRequest;
    }

    private Runnable resumeCameraRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    private void handleShortQR(String urlString) {
        Log.d(TAG, "handleShortQR: ");
        getShortQRService()
                .queryShorQR(urlString)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        Log.d(TAG, "onNext: ");
                        String response = "";
                        try {
                            response = responseBody.string();
                            Log.d(TAG, "onNext: " + response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private SkyworthIotService skyworthIotService;

    private SkyworthIotService getShortQRService() {
        if (null == skyworthIotService) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient okHttpClient = RetrofitUrlManager.getInstance().with(new OkHttpClient.Builder()) //RetrofitUrlManager ?????????
                    .readTimeout(10, TimeUnit.SECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(new RedirectInterceptor())
//                    .addNetworkInterceptor(new RedirectInterceptor())
                    .followRedirects(false) //???????????????
                    .followSslRedirects(false)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SHORT_QR_HOST)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//??????rxjava
                    .addConverterFactory(GsonConverterFactory.create())//??????Gson
                    .client(okHttpClient)
                    .build();

            skyworthIotService = retrofit.create(SkyworthIotService.class);
        }
        return skyworthIotService;
    }

    //???????????????????????????
    public class RedirectInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            int code = response.code();
            Log.d(TAG, "intercept: response = " + response);
            if (code == 301) {
                //????????????????????????
                String location = response.headers().get("Location");
                Log.d(TAG, "??????????????? location = " + location);
                if (!TextUtils.isEmpty(location) && !location.contains(SHORT_QR_HOST)) {
                    HomeUIThread.execute(new Runnable() {
                        @Override
                        public void run() {
                            handleScanResult(location);
                        }
                    });
                }
                //??????????????????
                Request newRequest = request.newBuilder().url(location).build();
                response = chain.proceed(newRequest);
            }
            return response;
        }
    }



    /*???????????? start*/
    private void submitEvent(String result) {
        Map<String, String> map = new HashMap<>();
        map.put("result", result);
        MobclickAgent.onEvent(this, DEVICE_ADD, map);
    }

    /**
     * ??????????????????scan_qrcode_success
     * @param action
     */
    private void submitScanSuccess(String action) {
        LogParams params = LogParams.newParams();
        params.append("qrcode_action", action);
        LogSubmit.event("scan_qrcode_success", params.getParams());
    }

    /**
     * ???????????????????????????
     */
    private void submitClickInputMode() {
        LogSubmit.event("click_connect_device_by_code_btn", null);
    }

    /**
     * scan_connect
     * click_device_item_connect
     * input_code_connect
     */
    private void submitManualConnectTime(boolean success) {
        try {
            DecimalFormat decimalFormat = new DecimalFormat("0.0");
            long durationLong = System.currentTimeMillis() - connectTime;
            if (durationLong > (10 * 1000)) {
                durationLong = 10 * 1000;
            }
            String duration = decimalFormat.format((float)durationLong/1000);
            Log.d(TAG, "submitManualConnectTime: " + duration);
            LogParams params = LogParams.newParams();
            params.append("duration", duration);
            params.append("connect_source", connectSource);
            LogSubmit.event("connect_device_manual_load_time", params.getParams());

            ConnectDeviceEvent.submit("????????????????????????", success, durationLong);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void submitDeviceListExpandEvent() {
        LogParams params = LogParams.newParams();

        Device curDevice = SSConnectManager.getInstance().getDevice();
        if (null != curDevice) {
            params.append("ss_device_type", curDevice.getZpRegisterType());
        }

        LogSubmit.event("device_list_expand", params.getParams());
    }

    private void submitItemClickEvent(Device device) {
        LogParams params = LogParams.newParams();

        if (null != device) {
            params.append("clicked_device_type", device.getZpRegisterType());
            params.append("online_or_offline", device.getStatus() == 1 ? "online" : "offline");
        }

        Device curDevice = SSConnectManager.getInstance().getDevice();
        if (null != curDevice) {
            params.append("ss_device_type", curDevice.getZpRegisterType());
        }

        LogSubmit.event("device_item_clicked", params.getParams());
    }

    private void submitItemBtnClickEvent(Device device, String btn_name) {
        LogParams params = LogParams.newParams();

        if (null != device) {
//            params.append("clicked_device_id", device.getLsid());
            params.append("clicked_device_type", device.getZpRegisterType());
            params.append("online_or_offline", device.getStatus() == 1 ? "online" : "offline");
        }

        Device curDevice = SSConnectManager.getInstance().getDevice();
        if (null != curDevice) {
//            params.append("ss_device_id", curDevice.getLsid());
            params.append("ss_device_type", curDevice.getZpRegisterType());
        }
        params.append("btn_name", btn_name);


        LogSubmit.event("device_item_btn_clicked", params.getParams());
    }
}
