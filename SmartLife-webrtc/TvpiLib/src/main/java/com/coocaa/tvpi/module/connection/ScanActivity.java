package com.coocaa.tvpi.module.connection;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.connect.callback.BindCallback;
import com.coocaa.smartscreen.connect.callback.ConnectCallbackImpl;
import com.coocaa.smartscreen.data.channel.events.ConnectEvent;
import com.coocaa.smartscreen.network.api.SkyworthIotService;
import com.coocaa.tvpi.event.ScanFastFinishEvent;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.module.homepager.IntentActivity;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.module.login.LoginActivity;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.mall.MallDetailActivity;
import com.coocaa.tvpi.util.NetworkUtil;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.UriUtils;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpi.view.webview.SimpleWebViewActivity;
import com.coocaa.tvpilib.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
//import com.king.zxing.CaptureHelper;
//import com.king.zxing.OnCaptureCallback;
import com.king.zxing.ViewfinderView;
//import com.king.zxing.camera.CameraManager;
import com.king.zxing.util.CodeUtils;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
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

import static android.view.animation.Animation.INFINITE;
import static com.coocaa.tvpi.common.UMengEventId.DEVICE_ADD;
import static com.coocaa.tvpi.common.UMengEventId.SCAN_QR_RESULT;

@Deprecated
public class ScanActivity extends BaseActivity implements UnVirtualInputable {
    private static final String TAG = ScanActivity.class.getSimpleName();
    public static final int REQUEST_CODE_PHOTO = 0X02;

    private static final int MODE_SCAN = 1;
    private static final int MODE_INPUT = 2;
    private int curMode = MODE_SCAN;
    private static final int MSG_ZOOM = 0x001;

    private static final String SHORT_QR_HOST = "https://s.skysrt.com";

    private long connectTime;

    private ImageView ivFlash;
    private TextView tvFlash;
//    private CaptureHelper captureHelper;
    private boolean isFlashOpen = false;

    private TextView scanBtn, inputBtn, screenCodeBtn;
    private View scanLayout, inputLayout, indicator;
    private EditText edtActiveId;
    private TextView connectTvBtn;
    private ImageView imgScan;
    private ProgressBar progressBar;
    private volatile boolean connectSuccess = false;

    private View deviceListLayout;
    private BottomSheetBehavior bottomSheetBehavior;
    private String scanApplet;

    private volatile boolean isResume = false;

    public static void start(Context context) {
        if (!UserInfoCenter.getInstance().isLogin()) {
            LoginActivity.start(context);
            ToastUtils.getInstance().showGlobalShort("未登录");
            return;
        }
        Intent starter = new Intent(context, ScanActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        StatusBarHelper.setStatusBarLightMode(this);
        StatusBarHelper.translucent(this);
        setContentView(R.layout.activity_scan);
        initView();
        SSConnectManager.getInstance().addConnectCallback(connectCallback);

        HomeIOThread.execute(3000, zoomCameraRunnable);

    }

    @Override
    public void onResume() {
        super.onResume();
//        captureHelper.onResume();
        isResume = true;
    }

    @Override
    public void onPause() {
        super.onPause();
//        captureHelper.onPause();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

//        captureHelper.onTouchEvent(event);
        Log.d(TAG, "onTouchEvent: " + event.toString());
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        isResume = false;
        HomeIOThread.removeTask(zoomCameraRunnable);
//        captureHelper.onDestroy();
        SSConnectManager.getInstance().removeConnectCallback(connectCallback);
        if(!connectSuccess) {
            EventBus.getDefault().post(new ScanFastFinishEvent());
        }
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

    private void initView() {
        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        ViewfinderView viewfinderView = findViewById(R.id.viewfinderView);
        ImageView ivBack = findViewById(R.id.iv_back);
        TextView tvAlbum = findViewById(R.id.tv_album);
        imgScan = findViewById(R.id.scan_img);
        ivFlash = findViewById(R.id.iv_flash);
        tvFlash = findViewById(R.id.tv_flash);

        startScanAnimation();

//        captureHelper = new CaptureHelper(this, surfaceView, viewfinderView);
//        captureHelper.setOnCaptureCallback(onCaptureCallback);
//        captureHelper.onCreate();
//        captureHelper.getCameraManager().setOnTorchListener(onTorchListener);
//        captureHelper.vibrate(true)
//                .supportLuminanceInvert(true)//支持黑白反转
//                .supportAutoZoom(true)
//                .fullScreenScan(true)//全屏扫码
////                .supportVerticalCode(true)//支持扫垂直条码，建议有此需求时才使用。
//                .continuousScan(true);//是否支持连续扫码

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curMode == MODE_INPUT) {
                    changeMode(MODE_SCAN);
                } else {
                    finish();
                }
            }
        });

        tvAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPhotoCode();
            }
        });

        //闪光灯
        ivFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFlashOpen = !isFlashOpen;
//                captureHelper.getCameraManager().setTorch(isFlashOpen);
            }
        });

        scanLayout = findViewById(R.id.scan_layout);
        inputLayout = findViewById(R.id.input_layout);
        indicator = findViewById(R.id.indicator);

        scanBtn = findViewById(R.id.scan_btn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanAnimation();
                changeMode(MODE_SCAN);
            }
        });
        inputBtn = findViewById(R.id.input_btn);
        inputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScanAnimation();
                changeMode(MODE_INPUT);
                submitClickInputMode();
            }
        });
        screenCodeBtn = findViewById(R.id.screen_code_btn);
        screenCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScanAnimation();
                changeMode(MODE_INPUT);
                submitClickInputMode();
            }
        });

        edtActiveId = findViewById(R.id.input_et);
        hideKeyboard(edtActiveId);
        edtActiveId.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 5) {
                    updateConnectTvBtn(false);
                } else {
                    updateConnectTvBtn(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 输入的内容变化的监听
                /*if (s.length() == 8) {
                    handleBind(edtActiveId.getText().toString());
                }*/
            }
        });
        edtActiveId.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "onFocusChange: " + hasFocus);
                if (hasFocus) {
                    // 获得焦点
                    openKeyboard();
                } else {
                    // 失去焦点
                    hideKeyboard(edtActiveId);
                }

            }

        });

        connectTvBtn = findViewById(R.id.btn_connect_tv);
        connectTvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkUtil.isAvailable(ScanActivity.this)) {
                    ToastUtils.getInstance().showGlobalShort("没有网络");
                    return;
                }
                handleBind(edtActiveId.getText().toString());
            }
        });

        deviceListLayout = findViewById(R.id.device_list_layout);
        bottomSheetBehavior = BottomSheetBehavior.from(deviceListLayout);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                Log.d(TAG, "onStateChanged: " + newState);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.d("BottomSheetDemo", "slideOffset:" + slideOffset);
            }
        });
    }

    private void startScanAnimation() {
        HomeUIThread.execute(new Runnable() {
            @Override
            public void run() {
                imgScan.setVisibility(View.VISIBLE);
                Animation scanAnim = AnimationUtils.loadAnimation(ScanActivity.this, R.anim.scan_up_dowm);
                scanAnim.setRepeatCount(INFINITE);
                imgScan.startAnimation(scanAnim);
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
            scanBtn.setTextColor(getResources().getColor(R.color.c_2));
            inputBtn.setTextColor(getResources().getColor(R.color.c_6));
            scanLayout.setVisibility(View.VISIBLE);
            inputLayout.setVisibility(View.GONE);
            edtActiveId.clearFocus();
//            if(captureHelper != null) {
//                captureHelper.getBeepManager().setVibrate(true);
//            }
            screenCodeBtn.setVisibility(View.VISIBLE);
        } else if (mode == MODE_INPUT) {
            scanBtn.setTextColor(getResources().getColor(R.color.c_6));
            inputBtn.setTextColor(getResources().getColor(R.color.c_2));
            scanLayout.setVisibility(View.GONE);
            inputLayout.setVisibility(View.VISIBLE);
            edtActiveId.requestFocus();
//            if(captureHelper != null) {
//                captureHelper.getBeepManager().setVibrate(false);
//            }
            screenCodeBtn.setVisibility(View.GONE);
        }
        startAnimation();
    }

    private void startAnimation() {
        Log.d(TAG, "startAnimation: " + indicator.getWidth());
        if (curMode == MODE_INPUT) {
            ObjectAnimator translationYAnimTop = ObjectAnimator.ofFloat(indicator, "translationX", 0f, indicator.getWidth());
            translationYAnimTop.setDuration(200).start();
        } else if (curMode == MODE_SCAN) {
            ObjectAnimator translationYAnimTop = ObjectAnimator.ofFloat(indicator, "translationX", indicator.getWidth(), 0f);
            translationYAnimTop.setDuration(200).start();
        }
    }


//    private CameraManager.OnTorchListener onTorchListener = new CameraManager.OnTorchListener() {
//        @Override
//        public void onTorchChanged(boolean torch) {
//            tvFlash.setText(torch ? "轻触关闭" : "轻触打开");
//            ivFlash.setSelected(torch);
//        }
//    };
//
//    private OnCaptureCallback onCaptureCallback = new OnCaptureCallback() {
//        @Override
//        public boolean onResultCallback(String result) {
//            if(curMode == MODE_SCAN) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        captureHelper.onPause();
//                    }
//                });
//                stopScanAnimation();
//                handleScanResult(result);
//            }
//            return true;
//        }
//    };

    private void handleScanResult(String result) {
        Log.d(TAG, "onResultCallback: " + result);

        if (!NetworkUtil.isAvailable(ScanActivity.this)) {
            ToastUtils.getInstance().showGlobalShort("没有网络");
            finish();
            return;
        }

        String bindCode = null;
        Map<String, String> map = getURLRequest(result);
        Log.d(TAG, "map: " + map);

        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("result", "success");
        if(!TextUtils.isEmpty(map.get("applet"))) {
            scanApplet = map.get("applet");
        }
        if (!TextUtils.isEmpty(map.get("action"))) {
            handleAction(map.get("action"), map);
        } else if (!TextUtils.isEmpty(map.get("bindCode"))) {
            handleBind(map.get("bindCode"));
            eventMap.put("type", "device");
        } else if (!TextUtils.isEmpty(map.get("productId"))) {
            handleProduct(map.get("productId"));
            eventMap.put("type", "mall");
        } else if (!TextUtils.isEmpty(map.get("id"))) {
            handleVideoCall(map.get("id"));
            eventMap.put("type", "video_call");
        } else if (!TextUtils.isEmpty(result)) {
            if (result.contains(SHORT_QR_HOST)) {
//                String path = result.replace(SHORT_QR_HOST + "/", "");
//                Log.d(TAG, "短码path = " + path);
                handleShortQR(result);
            } else if (result.contains("http")) {
                handleUrl(result);
                eventMap.put("type", "web");
            } else {
                eventMap.put("result", "fail");
                ToastUtils.getInstance().showGlobalShort("扫一扫失败");
                HomeIOThread.execute(1500, resumeCameraRunnable);
            }
        } else {
            eventMap.put("result", "fail");
            ToastUtils.getInstance().showGlobalShort("扫一扫失败");
            HomeIOThread.execute(1500, resumeCameraRunnable);
        }

        MobclickAgent.onEvent(ScanActivity.this, SCAN_QR_RESULT, eventMap);
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
                //旧版本没有mode字段
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
                ConnectNetForDongleActivity.start(ScanActivity.this, mac);
                finish();
            }

            @Override
            public void permissionDenied(String[] permission) {
                ToastUtils.getInstance().showGlobalShort("需要获取位置信息权限才能读取Wi-Fi");
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
                ToastUtils.getInstance().showGlobalShort("正在连接");
                dismissLoading();
                if (!TextUtils.isEmpty(edtActiveId.getText().toString())) {
                    edtActiveId.setText("");
                }
                submitEvent("success");
                startScanApplet();
                finish();
            }

            @Override
            public void onFail(String bindCode, String errorType, String msg) {
                Log.d(TAG, "onFail: bindCode = " + bindCode + " errorType = " + errorType + " msg = " + msg);
                ToastUtils.getInstance().showGlobalShort("绑定失败：" + msg);
                HomeIOThread.execute(1500, resumeCameraRunnable);
                dismissLoading();
                if (!TextUtils.isEmpty(edtActiveId.getText().toString())) {
                    edtActiveId.setText("");
                }
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
        SimpleWebViewActivity.start(this, url);
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
        //异步解析
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
            ToastUtils.getInstance().showGlobalShort("已连接");
            submitManualConnectTime();
            finish();
        }

        @Override
        public void onFailure(ConnectEvent connectEvent) {
            Log.d(TAG, "connectCallback onFailure: " + connectEvent);
            ToastUtils.getInstance().showGlobalShort("连接失败，再试一次吧");
            submitManualConnectTime();
            finish();
        }

    };

    /**
     * 解析出url参数中的键值对
     * 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     *
     * @param URL url地址
     * @return url请求参数部分
     */
    public static Map<String, String> getURLRequest(String URL) {
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
//            captureHelper.onResume();
            if (curMode == MODE_SCAN) {
                startScanAnimation();
            }
        }
    };

    private Runnable zoomCameraRunnable = new Runnable() {
        @Override
        public void run() {
            int count = 0;
            while (count < 3 && isResume) {
//                zoomCamera();
                count++;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    /**
     * 打开软键盘
     */
    private void openKeyboard() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //隐藏虚拟键盘
    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);

        }
    }

    private void submitEvent(String result) {
        Map<String, String> map = new HashMap<>();
        map.put("result", result);
        MobclickAgent.onEvent(this, DEVICE_ADD, map);
    }

//    private void zoomCamera() {
//        if (!captureHelper.getCameraManager().isOpen()) {
//            return;
//        }
//        Camera camera = captureHelper.getCameraManager().getOpenCamera().getCamera();
//        if (camera == null) {
//            return;
//        }
//        Camera.Parameters parameters = null;
//        try {
//            parameters = camera.getParameters();
//        } catch (Exception e) {
//            Log.d(TAG, "zoomCamera: " + e);
//        }
//        if (parameters != null && parameters.isZoomSupported()) {
//            int maxZoom = parameters.getMaxZoom();
//            int curZoom = parameters.getZoom();
//            curZoom += 10;
//            if (curZoom > maxZoom) {
//                curZoom = maxZoom;
//            }
//            Log.e("QRCodeReader", "放大 curZoom = " + curZoom);
//            parameters.setZoom(curZoom);
//            try {
//                camera.setParameters(parameters);
//            } catch (Exception e) {
//                Log.d(TAG, "zoomCamera: error");
//            }
//        }
//    }

    private void updateConnectTvBtn(boolean isEnable) {
        connectTvBtn.setEnabled(isEnable);
        if (isEnable) {
            connectTvBtn.setTextColor(getResources().getColor(com.coocaa.publib.R.color.c_6));
        } else {
            connectTvBtn.setTextColor(getResources().getColor(com.coocaa.publib.R.color.c_5));
        }
    }

    /**
     * @param urlString value = IvEFz2
     */
    private void handleShortQR(String urlString) {
        Log.d(TAG, "handleShortQR: " + urlString);
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
            OkHttpClient okHttpClient = RetrofitUrlManager.getInstance().with(new OkHttpClient.Builder()) //RetrofitUrlManager 初始化
                    .readTimeout(10, TimeUnit.SECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .followRedirects(false) //禁止重定向
                    .addInterceptor(new RedirectInterceptor())
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SHORT_QR_HOST)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//使用rxjava
                    .addConverterFactory(GsonConverterFactory.create())//使用Gson
                    .client(okHttpClient)
                    .build();

            skyworthIotService = retrofit.create(SkyworthIotService.class);
        }
        return skyworthIotService;
    }

    //处理重定向的拦截器
    public class RedirectInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            okhttp3.Request request = chain.request();
            Response response = chain.proceed(request);
            int code = response.code();
            if (code == 301) {
                //获取重定向的地址
                String location = response.headers().get("Location");
                Log.d(TAG, "重定向地址 location = " + location);
                if (!TextUtils.isEmpty(location) && !location.contains(SHORT_QR_HOST)) {
                    HomeUIThread.execute(new Runnable() {
                        @Override
                        public void run() {
                            handleScanResult(location);
                        }
                    });
                }
                //重新构建请求
                Request newRequest = request.newBuilder().url(location).build();
                response = chain.proceed(newRequest);
            }
            return response;
        }
    }

    /**
     * 扫码成功事件scan_qrcode_success
     * @param action
     */
    private void submitScanSuccess(String action) {
        LogParams params = LogParams.newParams();
        params.append("qrcode_action", action);
        LogSubmit.event("scan_qrcode_success", params.getParams());
    }

    /**
     * 点击智屏码连接按钮
     */
    private void submitClickInputMode() {
        LogSubmit.event("click_connect_device_by_code_btn", null);
    }

    private void submitManualConnectTime() {
        try {
            DecimalFormat decimalFormat = new DecimalFormat("0.0");
            long durationLong = System.currentTimeMillis() - connectTime;
            String duration = decimalFormat.format((float)durationLong/1000);
            Log.d(TAG, "submitManualConnectTime: " + duration);
            LogParams params = LogParams.newParams();
            params.append("duration", duration);
            String connect_source = "";
            if (curMode == MODE_SCAN) {
                connect_source = "scan_connect";
            } else {
                connect_source = "input_code_connect";
            }
            params.append("connect_source", connect_source);
            LogSubmit.event("connect_device_manual_load_time", params.getParams());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
