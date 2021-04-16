package com.coocaa.tvpi.module.whiteboard.ui;

import android.Manifest;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.IUserInfo;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.whiteboard.BrightnessControl;
import com.coocaa.tvpi.module.whiteboard.ReconnectActivity;
import com.coocaa.tvpi.module.whiteboard.WhiteboardActivity;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpilib.R;
import com.coocaa.whiteboard.client.WhiteBoardClientListener;
import com.coocaa.whiteboard.client.WhiteBoardClientSocket;
import com.coocaa.whiteboard.server.ServerCanvasInfo;
import com.coocaa.whiteboard.server.WhiteBoardServerCmdInfo;
import com.coocaa.whiteboard.ui.common.WBClientHelper;
import com.coocaa.whiteboard.ui.common.WBClientIOTChannelHelper;

import swaiotos.runtime.h5.VibratorHelper;
import swaiotos.sensor.data.AccountInfo;

public class WhiteBoardDrawActivity extends BaseActivity implements UnVirtualInputable {

    WBClientHelper helper;
    BrightnessControl control;
    View appBackgrounderTipView;
    private volatile boolean isSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TAG = "WBClient";

        helper = new WBClientHelper(this) {
            @Override
            protected void onSavePicClick() {
                savePic();
            }

            @Override
            protected AccountInfo getAccountInfo() {
                return getMobileAccountInfo();
            }
        };
        helper.setConnectCallback(callback);
        setContentView(helper.onCreate(savedInstanceState, WhiteBoardClientSocket.INSTANCE.getInitSyncData()));

        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        control = new BrightnessControl(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            control.resetBrightness();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            control.downBrightness();
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.i(TAG, "onWindowFocusChanged: " + hasFocus);
        if (hasFocus) {
            control.downBrightness();
        } else {
            control.resetBrightness();
        }
    }

    @Override
    public void finish() {
        helper.finish();
        super.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        helper.onResume();
        control.downBrightness();
    }

    @Override
    protected void onStop() {
        super.onStop();
        helper.onStop();
    }

    private void savePic() {
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                Log.d(TAG, "WRITE_EXTERNAL_STORAGE permissionGranted");
                if (!isFinishing() && !isDestroyed()) {
                    Log.d(TAG, "real start save pic.");
                    HomeIOThread.execute(new Runnable() {
                        @Override
                        public void run() {
                            boolean ret = helper.savePicture();
                            if (ret) {
                                HomeUIThread.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtils.getInstance().showGlobalLong("保存图片成功");
                                    }
                                });
                            }
                        }
                    });
                }
            }

            @Override
            public void permissionDenied(String[] permission) {
                Log.d(TAG, "WRITE_EXTERNAL_STORAGE permissionDenied!");
                ToastUtils.getInstance().showGlobalShort("SD卡读写权限被禁，请前往手机设置打开");
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private WhiteBoardClientListener callback = new WhiteBoardClientListener() {
        @Override
        public void onConnectSuccess() {
            Log.d(TAG, "onConnectSuccess");
//            ToastUtils.getInstance().showGlobalLong("连接画布成功");
            hideAppBackgrounderTipView();
            isSuccess = true;
        }

        @Override
        public void onConnectFail(String reason) {
            Log.d(TAG, "onConnectFail : " + reason);
//            ToastUtils.getInstance().showGlobalLong("连接画布失败：" + reason);
        }

        @Override
        public void onConnectFailOnce(String reason) {
            Log.d(TAG, "onConnectFailOnce : " + reason);
            if(isSuccess) {
                isSuccess = false;
                ReconnectActivity.start(WhiteBoardDrawActivity.this);
            }
        }

        @Override
        public void onConnectClose() {
            Log.d(TAG, "onConnectClose : ");
        }

        @Override
        public void onReceiveMsg(String msg) {

        }

        @Override
        public void onReceiveCmdInfo(WhiteBoardServerCmdInfo cmdInfo) {

        }

        @Override
        public void onRenderChanged(String renderData) {

        }

        @Override
        public void onCanvasChanged(ServerCanvasInfo serverCanvas) {
            Log.d(TAG, "onCanvasChanged : " + serverCanvas);
        }

        @Override
        public void onWhiteBoardAborted(boolean isInterrupt) {
            Log.d(TAG, "onWhiteBoardAborted: " + isInterrupt);
            if (isInterrupt) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        VibratorHelper.Vibrate(WhiteBoardDrawActivity.this,100L);
                        showAppBackgrounderTipView();
                    }
                });
            }
        }

        @Override
        public void onWhiteBoardResumeFront() {
            Log.d(TAG, "onWhiteBoardResumeFront");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideAppBackgrounderTipView();
                }
            });
        }
    };


    @Override
    protected void onDestroy() {
        helper.onDestroy();
        control.resetBrightness();
        super.onDestroy();
    }

    private void showAppBackgrounderTipView() {
        if (appBackgrounderTipView == null && helper != null && helper.getRootView() != null) {
            appBackgrounderTipView = LayoutInflater.from(this).inflate(R.layout.layout_app_backgrounder_tip, null);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            params.bottomMargin = DimensUtils.dp2Px(this,20);
            helper.getRootView().addView(appBackgrounderTipView, params);
            TextView tvRetry = appBackgrounderTipView.findViewById(R.id.tv_retry);
            tvRetry.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            tvRetry.getPaint().setAntiAlias(true);
            tvRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openWhiteboard();
                }
            });
        }
        if (appBackgrounderTipView != null) {
            appBackgrounderTipView.setVisibility(View.VISIBLE);
        }
    }

    private void hideAppBackgrounderTipView() {
        if (appBackgrounderTipView != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    appBackgrounderTipView.setVisibility(View.GONE);
                }
            });
        }
    }

    private void openWhiteboard() {
        WBClientIOTChannelHelper.sendStartWhiteBoardMsg(WhiteboardActivity.getAccountInfo(), false);
        helper.onResume();
    }


    public AccountInfo getMobileAccountInfo() {
        AccountInfo info = new AccountInfo();
        IUserInfo userInfo = SmartApi.getUserInfo();
        if (userInfo != null) {
            info.accessToken = userInfo.accessToken;
            info.avatar = userInfo.avatar;
            info.mobile = userInfo.mobile;
            info.open_id = userInfo.open_id;
            info.nickName = userInfo.nickName;
        }
        Log.d(TAG, "getAccountInfo()============" + info.toString());
        return info;
    }
}
