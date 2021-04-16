package com.coocaa.tvpi.connect;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.connect.callback.BindCallback;
import com.coocaa.smartscreen.connect.callback.ConnectCallback;
import com.coocaa.smartscreen.connect.callback.ConnectCallbackImpl;
import com.coocaa.smartscreen.data.channel.events.ConnectEvent;
import com.coocaa.tvpi.module.login.LoginActivity;
import com.coocaa.tvpi.module.login.UserInfoCenter;

import swaiotos.channel.iot.ss.device.Device;

/**
 * @Author: yuzhan
 */
public class FastConnectDevice {

    Context context;
    String bindCode;
    String type;
    String spaceId;
    volatile boolean waitSse = false;
    volatile boolean isBind = false;
    long startTime = 0;
    private boolean callConnect = false;

    private final static String TAG = "MainActivity";

    public FastConnectDevice(Context context) {
        this.context = context;
    }

    public void setBindCode(String bindCode, String type) {
        Log.d(TAG, "setBindCode : bc=" + bindCode + ", type=" + type);
        if(!TextUtils.isEmpty(bindCode))
            this.bindCode = bindCode;
        if(!TextUtils.isEmpty(type))
            this.type = type;
    }

    public void setSpaceId(String spaceId, String type) {
        Log.d(TAG, "setSpaceId : spaceId=" + spaceId + ", type=" + type);
        if(!TextUtils.isEmpty(spaceId))
            this.spaceId = spaceId;
        if(!TextUtils.isEmpty(type))
            this.type = type;
    }

    public void start() {
        if(TextUtils.isEmpty(bindCode) && TextUtils.isEmpty(spaceId))
            return ;
        SSConnectManager.getInstance().addConnectCallback(callback);
        if(SSConnectManager.getInstance().isConnectSSE()) {
            waitSse = false;
            startBind();
        } else {
            waitSse = true;
        }
    }

    public void stop() {
        waitSse = false;
        SSConnectManager.getInstance().removeConnectCallback(callback);
    }

    private ConnectCallback callback = new ConnectCallbackImpl() {
        @Override
        public void sseLoginSuccess() {
            Log.d(TAG, "on sseLoginSuccess, waitSse = " + waitSse);
            if(waitSse) {
                startBind();
            }
        }

        @Override
        public void onSuccess(ConnectEvent connectEvent) {
            Log.d(TAG, "fast connect device, onSuccess");
            if(callConnect) {
                ToastUtils.getInstance().showGlobalShort("连接成功");
            }
            callConnect = false;
            super.onSuccess(connectEvent);
            submitLog(true);
        }

        @Override
        public void onFailure(ConnectEvent connectEvent) {
            super.onFailure(connectEvent);
            Log.d(TAG, "fast connect device, onFailure");
            if(callConnect) {
                ToastUtils.getInstance().showGlobalShort("连接失败");
            }
            callConnect = false;
            submitLog(false);
        }
    };

    private void startBind() {
        if(TextUtils.isEmpty(bindCode) && TextUtils.isEmpty(spaceId)) {
            return ;
        }
        if (!UserInfoCenter.getInstance().isLogin()) {
            LoginActivity.start(context);
            return;
        }
        callConnect = true;
        if(!TextUtils.isEmpty(bindCode)) {
            bindByBindCode();
        }else if(!TextUtils.isEmpty(spaceId)) {
            bindBySpaceId();
        }
    }

    private void bindByBindCode() {
        startTime = System.currentTimeMillis();
        Log.d(TAG, "real start fast bind device by bindCode: " + bindCode);
        SSConnectManager.getInstance().bind(bindCode, new BindCallback() {
            @Override
            public void onSuccess(String bindCode, Device device) {
                Log.d(TAG, "onSuccess: bindCode = " + bindCode + "   device = " + device);
                ToastUtils.getInstance().showGlobalShort("正在连接");
                SSConnectManager.getInstance().connect(device);
                isBind = true;
            }

            @Override
            public void onFail(String bindCode, String errorType, String msg) {
                Log.d(TAG, "onFail: bindCode = " + bindCode + " errorType = " + errorType + " msg = " + msg);
                ToastUtils.getInstance().showGlobalShort("绑定失败：" + msg);
                isBind = true;
            }
        });
    }

    private void bindBySpaceId() {
        startTime = System.currentTimeMillis();
        Log.d(TAG, "real start fast bind device by spaceId: " + spaceId);
        SSConnectManager.getInstance().tempBind(spaceId, 1, new BindCallback() {
            @Override
            public void onSuccess(String bindCode, Device device) {
                Log.d(TAG, "onSuccess: bindCode = " + bindCode + "   device = " + device);
                ToastUtils.getInstance().showGlobalShort("正在连接");
                SSConnectManager.getInstance().connect(device);
                isBind = true;
            }

            @Override
            public void onFail(String bindCode, String errorType, String msg) {
                Log.d(TAG, "onFail: bindCode = " + bindCode + " errorType = " + errorType + " msg = " + msg);
                ToastUtils.getInstance().showGlobalShort("绑定失败：" + msg);
                isBind = true;
            }
        });
    }

    public boolean isBind() {
        return isBind;
    }

    public void setBind(boolean bind) {
        isBind = bind;
    }

    //TODO 需要精确判断一下，连接成功的设备，是对应bindCode的才能提交日志，还要减少后台接口请求；
    private void submitLog(boolean success) {
//        try {
//            DecimalFormat decimalFormat = new DecimalFormat("0.0");
//            long durationLong = System.currentTimeMillis() - startTime;
//            if (durationLong > (10 * 1000)) {
//                durationLong = 10 * 1000;
//            }
//            String duration = decimalFormat.format((float)durationLong/1000);
//            Log.d(TAG, "submitManualConnectTime: " + duration);
//            LogParams params = LogParams.newParams();
//            params.append("duration", duration);
//            params.append("connect_source", type);
//            params.append("connect_bind_code", bindCode);
//            LogSubmit.event("connect_device_manual_load_time", params.getParams());
//
//            ConnectDeviceEvent.submit("通过分享等方式快速连接", success, durationLong);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
