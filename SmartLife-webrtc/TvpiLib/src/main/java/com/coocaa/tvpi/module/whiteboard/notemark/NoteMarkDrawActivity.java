package com.coocaa.tvpi.module.whiteboard.notemark;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.data.channel.events.ScreenshotEvent;
import com.coocaa.smartscreen.utils.CmdUtil;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.whiteboard.BrightnessControl;
import com.coocaa.tvpi.module.whiteboard.ReconnectActivity;
import com.coocaa.tvpi.module.whiteboard.WhiteboardActivity;
import com.coocaa.tvpi.util.IntentUtils;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpilib.R;
import com.coocaa.whiteboard.client.WhiteBoardClientListener;
import com.coocaa.whiteboard.notemark.NoteMarkClientSocket;
import com.coocaa.whiteboard.server.ServerCanvasInfo;
import com.coocaa.whiteboard.server.WhiteBoardServerCmdInfo;
import com.coocaa.whiteboard.server.WhiteBoardServerSSCmd;
import com.coocaa.whiteboard.ui.common.notemark.NoteClientIOTChannelHelper;
import com.coocaa.whiteboard.ui.common.notemark.NoteMarkClientHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import swaiotos.runtime.h5.VibratorHelper;
import swaiotos.sensor.data.AccountInfo;

public class NoteMarkDrawActivity extends BaseActivity {

    NoteMarkClientHelper helper;
    BrightnessControl control;
    private volatile boolean isSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TAG = "NMClient";

        helper = new NoteMarkClientHelper(this) {
            @Override
            protected void onSavePicClick() {
                savePic();
            }

            @Override
            protected AccountInfo getAccountInfo() {
                return NoteMarkLoadingActivity.getAccountInfo();
            }
        };
        helper.setConnectCallback(callback);
        setContentView(helper.onCreate(savedInstanceState, NoteMarkClientSocket.INSTANCE.getInitSyncData()));
        overridePendingTransition(0, 0);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
//        startScreenShot(true);
        control = new BrightnessControl(this);
        if (getIntent() != null) {
            String shotScreenUrl = IntentUtils.INSTANCE.getStringExtra(getIntent(),"url");
            if (!TextUtils.isEmpty(shotScreenUrl)) {
                loadScreenShot(shotScreenUrl);
            } else {
                finish();
            }
        }
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
            if (isSuccess) {
                isSuccess = false;
                ReconnectActivity.startAsNoteMark(NoteMarkDrawActivity.this);
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
            Log.d(TAG, "onReceiveCmdInfo : " + cmdInfo.cmd);
            if (WhiteBoardServerSSCmd.CMD_SERVER_SCREEN_SHOT_RESULT.equals(cmdInfo.cmd)) {
                Log.d(TAG, "receive server screen shot result.");
                onScreenShot(cmdInfo.content);
            }
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
            Log.d(TAG, "NoteMark onWhiteBoardAborted, isInterrupt=" + isInterrupt);
            if (isInterrupt) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        VibratorHelper.Vibrate(NoteMarkDrawActivity.this, 100L);
                        showAppBackgrounderTipView();
                    }
                });
            }
        }

        @Override
        public void onWhiteBoardResumeFront() {
            Log.d(TAG, "NoteMark onWhiteBoardResumeFront");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideAppBackgrounderTipView();
                }
            });
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ScreenshotEvent screenshotEvent) {
        Log.d(TAG, "ScreenshotEvent: " + screenshotEvent.url + "\n"
                + screenshotEvent.msg + "\n");
//        loadScreenShot(screenshotEvent.url);
    }

    private void onScreenShot(String json) {
        JSONObject obj = JSON.parseObject(json);
        String url = obj.getString("url");
        int width = obj.getInteger("width");
        int height = obj.getInteger("height");
        Log.d(TAG, "receive screenshot, url=" + url + ", width=" + width + ", height=" + height);
//        loadScreenShot(url);
    }

    private volatile boolean loadScreenShotSuccess;
    private Object lock = new Object();

    private void loadScreenShot(String url) {
        if (loadScreenShotSuccess)
            return;
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    if (loadScreenShotSuccess)
                        return;
                    if (!TextUtils.isEmpty(url)) {
                        Log.d(TAG, "start load screenshot bitmap, url=" + url);
                        Glide.with(NoteMarkDrawActivity.this).asBitmap().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).load(url).into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                Log.d(TAG, "load screenshot bitmap success, url=" + url);
                                synchronized (lock) {
                                    if (loadScreenShotSuccess)
                                        return;
                                    if (resource != null && !resource.isRecycled()) {
                                        Log.d(TAG, "set screenshot bitmap as bg");
                                        Drawable drawable = new BitmapDrawable(resource);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                helper.setBackground(drawable);
                                            }
                                        });
                                        loadScreenShotSuccess = true;
                                    }
                                }
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                Log.d(TAG, "load screenshot bitmap fail, url=" + url);
                                super.onLoadFailed(errorDrawable);
                            }
                        });
                    }
                }
            }
        });
    }

    public void startScreenShot(boolean isLandscape) {
        Log.d(TAG, "startScreenShot: isLandscape: " + isLandscape);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        CmdUtil.sendScreenshot();
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

    @Override
    protected void onDestroy() {
        helper.onDestroy();
        helper.onStop();
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void openWhiteboard() {
        NoteClientIOTChannelHelper.sendStartNoteMsg(WhiteboardActivity.getAccountInfo());
        helper.onResume();
    }

    View appBackgrounderTipView;

    private void showAppBackgrounderTipView() {
        if (appBackgrounderTipView == null && helper != null && helper.getRootView() != null) {
            appBackgrounderTipView = LayoutInflater.from(this).inflate(R.layout.layout_notemark_backgrounder_tip, null);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            params.bottomMargin = DimensUtils.dp2Px(this, 20);
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
}
