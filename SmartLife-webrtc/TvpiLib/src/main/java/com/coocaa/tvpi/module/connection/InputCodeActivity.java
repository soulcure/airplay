package com.coocaa.tvpi.module.connection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.connect.callback.BindCallback;
import com.coocaa.smartscreen.connect.callback.ConnectCallbackImpl;
import com.coocaa.smartscreen.data.channel.events.ConnectEvent;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.module.log.ConnectDeviceEvent;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.module.login.LoginActivity;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.view.LoadingButton;
import com.coocaa.tvpi.util.NetworkUtil;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import swaiotos.channel.iot.ss.device.Device;

import static com.coocaa.tvpi.common.UMengEventId.DEVICE_ADD;

/**
 * @ClassName InputCodeActivity
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/12/21
 * @Version TODO (write something)
 */
public class InputCodeActivity extends BaseActivity implements UnVirtualInputable {

    private EditText edtActiveId;
    private ImageView deleteIV;
    private LoadingButton connectBtn;

    private long connectTime;

    public static void start(Context context) {
        Intent starter = new Intent(context, InputCodeActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBar();
        setContentView(R.layout.activity_input_code);
        initView();
        SSConnectManager.getInstance().addConnectCallback(connectCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SSConnectManager.getInstance().removeConnectCallback(connectCallback);
    }

    private void initStatusBar() {
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
    }

    private void initView() {
        findViewById(R.id.back_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        deleteIV = findViewById(R.id.input_delete_iv);
        deleteIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != edtActiveId) {
                    edtActiveId.setText("");
                }
            }
        });

        edtActiveId = findViewById(R.id.input_et);
        hideKeyboard(edtActiveId);
        edtActiveId.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 5) {
//                    updateConnectTvBtn(false);
                    connectBtn.setEnabled(false);
                } else {
//                    updateConnectTvBtn(true);
                    connectBtn.setEnabled(true);
                }
                if (s.length() == 0) {
                    deleteIV.setVisibility(View.INVISIBLE);
                } else {
                    deleteIV.setVisibility(View.VISIBLE);
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

        connectBtn = findViewById(R.id.connect_btn);
        connectBtn.setEnabled(false);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkUtil.isAvailable(InputCodeActivity.this)) {
                    ToastUtils.getInstance().showGlobalShort("没有网络");
                    return;
                }
                handleBind(edtActiveId.getText().toString());
            }
        });

    }

    private void handleBind(String bindCode) {
        if (!UserInfoCenter.getInstance().isLogin()) {
            LoginActivity.start(this);
            return;
        }
        connectBtn.start();
        SSConnectManager.getInstance().bind(bindCode, new BindCallback() {
            @Override
            public void onSuccess(String bindCode, Device device) {
                Log.d(TAG, "onSuccess: bindCode = " + bindCode + "   device = " + device);
                connectBtn.complete();
                ToastUtils.getInstance().showGlobalShort("正在连接");
                submitEvent("success");
                finish();
            }

            @Override
            public void onFail(String bindCode, String errorType, String msg) {
                Log.d(TAG, "onFail: bindCode = " + bindCode + " errorType = " + errorType + " msg = " + msg);
                connectBtn.complete();
                ToastUtils.getInstance().showGlobalShort("绑定失败：" + msg);
                if (!TextUtils.isEmpty(edtActiveId.getText().toString())) {
                    edtActiveId.setText("");
                }
                submitEvent("fail");
            }
        });
    }

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

    private ConnectCallbackImpl connectCallback = new ConnectCallbackImpl() {
        @Override
        public void onConnecting() {
            super.onConnecting();
            connectTime = System.currentTimeMillis();
        }

        @Override
        public void onSuccess(ConnectEvent connectEvent) {
            super.onSuccess(connectEvent);
            submitManualConnectTime(true);
        }

        @Override
        public void onFailure(ConnectEvent connectEvent) {
            super.onFailure(connectEvent);
            submitManualConnectTime(false);
        }
    };

    private void submitEvent(String result) {
        Map<String, String> map = new HashMap<>();
        map.put("result", result);
        MobclickAgent.onEvent(this, DEVICE_ADD, map);
    }

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
            params.append("connect_source", "input_code_connect");
            LogSubmit.event("connect_device_manual_load_time", params.getParams());

            ConnectDeviceEvent.submit("输入智屏码连接", success, durationLong);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
