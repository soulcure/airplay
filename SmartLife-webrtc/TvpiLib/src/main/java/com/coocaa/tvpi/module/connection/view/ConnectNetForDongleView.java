package com.coocaa.tvpi.module.connection.view;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.network.common.Constants;
import com.coocaa.tvpi.module.connection.OpenHotspotActivity;
import com.coocaa.tvpi.module.connection.WifiListActivity;
import com.coocaa.tvpi.module.connection.constant.ConnNetForDongleUIState;
import com.coocaa.tvpi.util.GpsUtil;
import com.coocaa.tvpi.util.WifiUtil;
import com.coocaa.tvpi.util.NetworkUtil;
import com.coocaa.tvpi.util.TextWatchAdapter;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpi.view.webview.SimpleWebViewActivity;
import com.coocaa.tvpilib.R;
import com.umeng.commonsdk.debug.I;

import java.io.IOException;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ConnectNetForDongleView extends FrameLayout {
    private static final String TAG = ConnectNetForDongleView.class.getSimpleName();
    private static final int REQUEST_ENABLE_BLUETOOTH = 0;
    private static final int REQUEST_CHOOSE_WIFI = 3;

    private ConstraintLayout unConnectLayout;
    private TextView tvUnConnectTitle;
    private TextView tvUnConnectSubTitle;
    private TextView tvUserHotpots;
    private Button btChangeWifi;
    private EditText etWifiSSid;
    private EditText etWifiPsd;
    private Button btClearPsd;
    private TextView tvConnect;
    private LinearLayout bleTipLayout;

    private FrameLayout connectingAndResultLayout;
    private SurfaceView connectingSurface;
    private SurfaceHolder connectingSurfaceHolder;
    private ImageView ivConnectState;
    private TextView tvConnectStateTitle;
    private TextView tvConnectStateSubTitle;
    private Button btConnectState;

    private String wifiSSid;
    private boolean isNeedPassword;
    private boolean isUserWifiConnNet;
    private boolean isCanceledConnect;
    private ConnNetForDongleUIState uiState;

    private ConnNetListener connNetListener;
    private WifiStateReceiver wifiStateReceiver;


    public ConnectNetForDongleView(@NonNull Context context) {
        this(context, null);
    }

    public ConnectNetForDongleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConnectNetForDongleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        registerReceiver();
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: " + requestCode + requestCode + data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                connNetForDongle();
                ToastUtils.getInstance().showGlobalShort("蓝牙已开启");
            } else {
                bleTipLayout.setVisibility(VISIBLE);
                ToastUtils.getInstance().showGlobalShort("需开启蓝牙，才能连接酷开共享屏");
            }
        } else if (requestCode == REQUEST_CHOOSE_WIFI) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    wifiSSid = data.getStringExtra("wifi");
                    isNeedPassword = data.getBooleanExtra("hasPassword", true);
                    etWifiSSid.setText(wifiSSid);
                    etWifiPsd.requestFocus();
                }
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterReceiver();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_conn_net_for_dongle, this, true);

        ImageView ivBack = findViewById(R.id.ivBack);
        tvUserHotpots = findViewById(R.id.tvUserHotpots);
        unConnectLayout = findViewById(R.id.unConnectLayout);
        tvUnConnectTitle = findViewById(R.id.tvUnConnectTitle);
        tvUnConnectSubTitle = findViewById(R.id.tvUnConnectSubtitle);
        btChangeWifi = findViewById(R.id.btChangeWifi);
        etWifiSSid = findViewById(R.id.etWifiName);
        etWifiPsd = findViewById(R.id.etWifiPassword);
        btClearPsd = findViewById(R.id.btClear);
        tvConnect = findViewById(R.id.tvConnect);
        bleTipLayout = findViewById(R.id.bleTipLayout);
        bleTipLayout.setVisibility(View.GONE);

        connectingAndResultLayout = findViewById(R.id.connectingAndResultLayout);
        connectingSurface = findViewById(R.id.connectingSurface);
        connectingSurface.setVisibility(View.GONE);
        connectingSurfaceHolder = connectingSurface.getHolder();
        connectingSurfaceHolder.addCallback(surfaceHolderCallback);
        ivConnectState = findViewById(R.id.ivConnectState);
        tvConnectStateTitle = findViewById(R.id.tvConnectStateTitle);
        tvConnectStateSubTitle = findViewById(R.id.tvConnectStateSubTitle);
        btConnectState = findViewById(R.id.btConnectState);
        tvUnConnectSubTitle.setMovementMethod(LinkMovementMethod.getInstance());

        ivBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() != null && getContext() instanceof Activity) {
                    ((Activity) getContext()).finish();
                }
            }
        });

        tvUserHotpots.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() != null && getContext() instanceof Activity) {
                    getContext().startActivity(new Intent(getContext(), OpenHotspotActivity.class));
                }
            }
        });

        etWifiSSid.addTextChangedListener(new TextWatchAdapter() {
            @Override
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                setConnectButtonEnable();
            }
        });

        etWifiPsd.addTextChangedListener(new TextWatchAdapter() {
            @Override
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                setConnectButtonEnable();
                btClearPsd.setVisibility(c.length() > 0 ? View.VISIBLE : View.GONE);
            }
        });

        btChangeWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() != null && getContext() instanceof Activity) {
                    Intent intent = new Intent(getContext(), WifiListActivity.class);
                    intent.putExtra(WifiListActivity.KEY_START_MODE, WifiListActivity.START_MODE_START_ACTIVITY_FOR_RESULT_WIFI_SSID);
                    ((Activity) getContext()).startActivityForResult(intent, REQUEST_CHOOSE_WIFI);
                }
            }
        });

        btClearPsd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etWifiPsd.getText().clear();
            }
        });


        tvConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connNetForDongle();
            }
        });

        btConnectState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (uiState) {
                    case Connecting:
                        isCanceledConnect = true;
                        setUiState(ConnNetForDongleUIState.UnConnect);
                        break;
                    case ConnectError:
                    case ConnectTimeout:
                        setUiState(ConnNetForDongleUIState.UnConnect);
                        break;
                    case ConnectSuccess:
                        if (isUserWifiConnNet) {
                            if (getContext() != null && getContext() instanceof Activity) {
                                ((Activity) getContext()).finish();
                            }
                        } else {
                            //热点配网成功的情况下直接返回首页
                            if (getContext() != null) {
                                Intent intent = new Intent();
                                intent.setAction("android.intent.action.kickout.back");
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                getContext().startActivity(intent);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void initConnNetInfo(String wifiSSid, boolean isNeedPassword, boolean isUserWifiConnNet) {
        this.wifiSSid = wifiSSid;
        this.isNeedPassword = isNeedPassword;
        this.isUserWifiConnNet = isUserWifiConnNet;
        setUiState(ConnNetForDongleUIState.UnConnect);
    }


    public void setUiState(ConnNetForDongleUIState uiState) {
        this.uiState = uiState;
        switch (uiState) {
            case UnConnect:
                unConnectLayout.setVisibility(View.VISIBLE);
                connectingAndResultLayout.setVisibility(View.GONE);
                if (isUserWifiConnNet) {
                    tvUnConnectTitle.setText("为共享屏连网");
                    tvUnConnectSubTitle.setText("输入 WiFi 的名称及密码");
                    btChangeWifi.setVisibility(View.VISIBLE);
                    etWifiSSid.setHint("输入 Wi-Fi 名称");
                    etWifiPsd.setHint("输入 Wi-Fi 密码");
                    tvUserHotpots.setVisibility(VISIBLE);
                    if (!TextUtils.isEmpty(wifiSSid)) {
                        etWifiSSid.setText(wifiSSid);
                    }
                    if (isNeedPassword) {
                        etWifiPsd.setHint("请输入Wi-Fi密码");
                        etWifiPsd.setFocusable(true);
                        etWifiPsd.setCursorVisible(true);
                        etWifiPsd.setFocusableInTouchMode(true);
                        etWifiPsd.requestFocus();
                    } else {
                        etWifiPsd.setHint("该Wi-Fi无需密码");
                        etWifiPsd.setCursorVisible(false);
                        etWifiPsd.setFocusable(false);
                        etWifiPsd.setFocusableInTouchMode(false);
                    }
                } else {
                    tvUnConnectTitle.setText("使用热点连网");
                    tvUnConnectSubTitle.setText(createHotspotSubtitleBuilder());
                    btChangeWifi.setVisibility(View.GONE);
                    etWifiSSid.setHint("输入热点名称");
                    etWifiPsd.setHint("请输入热点密码，如无密码可不输入");
                    tvUserHotpots.setVisibility(GONE);
                }
                setConnectButtonEnable();
                break;
            case Connecting:
                unConnectLayout.setVisibility(View.GONE);
                connectingAndResultLayout.setVisibility(View.VISIBLE);
                connectingAndResultLayout.setBackgroundResource(R.drawable.bg_dongle_connecting);
                ivConnectState.setBackgroundResource(R.drawable.connect_dongle_connecting);
                tvConnectStateTitle.setText("正在连接");
                tvConnectStateSubTitle.setText("Wi-Fi：" + etWifiSSid.getText());
                btConnectState.setText("取消");
                connectingSurface.setVisibility(View.VISIBLE);
                break;
            case ConnectSuccess:
                unConnectLayout.setVisibility(View.GONE);
                connectingAndResultLayout.setVisibility(View.VISIBLE);
                connectingAndResultLayout.setBackgroundResource(R.drawable.bg_dongle_connect_success);
                ivConnectState.setBackgroundResource(R.drawable.connect_dongle_connect_success);
                tvConnectStateTitle.setText("连接成功");
                tvConnectStateSubTitle.setText("试试推送视频、照片或音乐吧");
                btConnectState.setText("完成");
                connectingSurface.setVisibility(View.GONE);
                break;
            case ConnectError:
                unConnectLayout.setVisibility(View.GONE);
                connectingAndResultLayout.setVisibility(View.VISIBLE);
                connectingAndResultLayout.setBackgroundResource(R.drawable.bg_dongle_connect_error);
                ivConnectState.setBackgroundResource(R.drawable.connect_dongle_connect_error);
                tvConnectStateTitle.setText("连接失败，请重试");
                tvConnectStateSubTitle.setText("Wi-Fi：" + etWifiSSid.getText());
                btConnectState.setText("返回重试");
                connectingSurface.setVisibility(View.GONE);
                break;
            case ConnectTimeout:
                unConnectLayout.setVisibility(View.GONE);
                connectingAndResultLayout.setVisibility(View.VISIBLE);
                connectingAndResultLayout.setBackgroundResource(R.drawable.bg_dongle_connect_error);
                ivConnectState.setBackgroundResource(R.drawable.connect_dongle_connect_error);
                tvConnectStateTitle.setText("连接超时，请重试");
                tvConnectStateSubTitle.setText("Wi-Fi：" + etWifiSSid.getText());
                btConnectState.setText("返回重试");
                connectingSurface.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private void connNetForDongle() {
        if (isUserWifiConnNet && isNeedPassword) {
            if (etWifiPsd.getText() == null || TextUtils.isEmpty(etWifiPsd.getText().toString())) {
                ToastUtils.getInstance().showGlobalShort("请输入WiFi密码");
                return;
            }
        }

        if (getContext() != null && getContext() instanceof Activity) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(((Activity) getContext()).getWindow().getDecorView().getWindowToken(), 0);
        }

        bleTipLayout.setVisibility(View.GONE);
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) getContext()).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
            return;
        }

        isCanceledConnect = false;

        setUiState(ConnNetForDongleUIState.Connecting);

        if (connNetListener != null) {
            connNetListener.onConnectClick();
        }
    }

    private void setConnectButtonEnable() {
        if (isUserWifiConnNet && isNeedPassword) {
            if (isWifiSsidEditTextInput() && isWifiPsdEditTextInputFinish() ) {
                tvConnect.setBackgroundResource(R.drawable.bg_yellow_round_12);
            } else {
                tvConnect.setBackgroundResource(R.drawable.bg_yellow_round_12_disable);
            }
        } else {
            if (isWifiSsidEditTextInput()) {
                tvConnect.setBackgroundResource(R.drawable.bg_yellow_round_12);
            } else {
                tvConnect.setBackgroundResource(R.drawable.bg_yellow_round_12_disable);
            }
        }
    }

    private boolean isWifiSsidEditTextInput() {
        return etWifiSSid.getText() != null
                && !TextUtils.isEmpty(etWifiSSid.getText().toString());
    }

    private boolean isWifiPsdEditTextInputFinish(){
        return etWifiPsd.getText() != null
                &&!TextUtils.isEmpty(etWifiPsd.getText().toString())
                &&etWifiPsd.getText().toString().length() >= 8;
    }

    private void registerReceiver() {
        if (wifiStateReceiver == null) {
            wifiStateReceiver = new WifiStateReceiver();
        }
        if (getContext() != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//wifi开发监听
            getContext().registerReceiver(wifiStateReceiver, filter);
        }
    }

    private void unregisterReceiver() {
        if (wifiStateReceiver != null) {
            try {
                if (getContext() != null && getContext() instanceof Activity) {
                    getContext().unregisterReceiver(wifiStateReceiver);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private SpannableStringBuilder createHotspotSubtitleBuilder() {
        String tipStr = "查看热点名称和密码 ";
        String settingStr = "点击查看";
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(tipStr)
                .append(settingStr);
        int settingStart = tipStr.length();
        int settingEnd = tipStr.length() + settingStr.length();
        builder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                gotoHotspotSetting();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(true);
            }
        }, settingStart, settingEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorText_F86239)),
                settingStart, settingEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    private void gotoHotspotSetting() {
        Intent tetherIntent = new Intent();
        tetherIntent.addCategory(Intent.CATEGORY_DEFAULT);
        tetherIntent.setAction("android.intent.action.MAIN");
        ComponentName componentName = new ComponentName(
                "com.android.settings", "com.android.settings.Settings$TetherSettingsActivity");
        tetherIntent.setComponent(componentName);
        if (getContext() != null) {
            PackageManager packageManager = getContext().getPackageManager();
            List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(tetherIntent, 0);
            if (resolveInfos != null && !resolveInfos.isEmpty()) {
                getContext().startActivity(tetherIntent);
            } else {
                Intent settingIntent = new Intent();
                settingIntent.setAction(Settings.ACTION_SETTINGS);
                getContext().startActivity(settingIntent);
            }
        }
    }


    public class WifiStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + intent);
            if (getContext() != null && WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                if (WifiManager.WIFI_STATE_ENABLED == wifiState) {
                    //没权限
                    if (!GpsUtil.isOpen(getContext())
                            || !PermissionsUtil.getInstance().hasPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                        return;
                    }

                    if (isUserWifiConnNet
                            && uiState == ConnNetForDongleUIState.UnConnect
                            && NetworkUtil.isWifiConnected(context)) {
                        etWifiSSid.setText(WifiUtil.getConnectWifiSsid(getContext()));
                    }
                }
            }
        }
    }


    public boolean isCanceledConnect() {
        return isCanceledConnect;
    }

    public ConnNetForDongleUIState getUiState() {
        return uiState;
    }

    public EditText getSSidEditText() {
        return etWifiSSid;
    }

    public EditText getPasswordEditText() {
        return etWifiPsd;
    }

    public void setConnNetListener(ConnNetListener connNetListener) {
        this.connNetListener = connNetListener;
    }

    public interface ConnNetListener {
        void onConnectClick();
    }

    private MediaPlayer mediaPlayer;
    private final SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDisplay(connectingSurfaceHolder);
            try {
                AssetFileDescriptor afd = getResources().getAssets().openFd("dongle_connecting.mp4");
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                }
            });
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    };
}
