package com.swaiotos.skymirror;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;

public class NetSpeed {

    private static final int REQUEST_CODE = 0;

    private static final int START_SPEED = 10;
    private static final int STOP_SPEED = 11;

    public static final int INTERVAL = 1;   //单位秒

    private Activity mAct;
    private MsgHandler mHandler;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private boolean isShow;

    public SpeedView speedView;
    private long preRxBytes, preSeBytes;

    private int initX, initY;

    private static final String PREFERENCES = "NetSpeed";

    public NetSpeed(Activity act) {
        mAct = act;
        mHandler = new MsgHandler(this);

        SharedPreferences sharedPref = act.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        initX = sharedPref.getInt("initX", 0);
        initY = sharedPref.getInt("initY", 0);

        init(act);
    }

    private void init(Context context) {
        speedView = new SpeedView(context);
        // 获取WindowManager服务
        windowManager = (WindowManager) mAct.getSystemService(Context.WINDOW_SERVICE);
        // 设置LayoutParam
        layoutParams = new WindowManager.LayoutParams();

        layoutParams.x = initX;
        layoutParams.y = initY;

        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }


    private void setParams() {
        if (Build.VERSION.SDK_INT >= 23
                && Settings.canDrawOverlays(mAct)) {
            if (Build.VERSION.SDK_INT >= 26) { //  >= 8.0
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            }
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }


    }

    public void startSpeedView() {
        if (Build.VERSION.SDK_INT >= 23
                && !Settings.canDrawOverlays(mAct)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + mAct.getPackageName()));
            mAct.startActivityForResult(intent, REQUEST_CODE);
            return;
        }

        setParams();

        if (speedView.getParent() == null) {
            isShow = true;
            windowManager.addView(speedView, layoutParams);
            preRxBytes = TrafficStats.getTotalRxBytes();
            preSeBytes = TrafficStats.getTotalTxBytes() - preRxBytes;

            if (!mHandler.hasMessages(START_SPEED)) {
                mHandler.sendEmptyMessageDelayed(START_SPEED, INTERVAL * 1000);
            }

        }
    }

    public void stopSpeedView() {
        if (isShow) {
            windowManager.removeView(speedView);
            isShow = false;
        }

        SharedPreferences.Editor editor = mAct.getSharedPreferences(
                PREFERENCES, Context.MODE_PRIVATE).edit();

        editor.putInt("initX", speedView.getViewX());
        editor.putInt("initY", speedView.getViewY());
        editor.apply();
    }

    private void updateSpeed(String downSpeed, String upSpeed) {
        speedView.upText.setText(upSpeed);
        speedView.downText.setText(downSpeed);
    }

    private void calculateNetSpeed() {
        long rxBytes = TrafficStats.getTotalRxBytes();
        long seBytes = TrafficStats.getTotalTxBytes() - rxBytes;

        double ds = rxBytes - preRxBytes;
        double us = seBytes - preSeBytes;

        double downloadSpeed = ds / INTERVAL * 8;  //剩8转换为 bit
        double uploadSpeed = us / INTERVAL * 8;  //剩8转换为 bit

        preRxBytes = rxBytes;
        preSeBytes = seBytes;

        //根据范围决定显示单位
        String upSpeed;
        String downSpeed;

        NumberFormat df = java.text.NumberFormat.getNumberInstance();
        df.setMaximumFractionDigits(2);

        if (downloadSpeed > 1024 * 1024) {
            downloadSpeed /= (1024 * 1024);
            downSpeed = df.format(downloadSpeed) + "Mb/s";
        } else if (downloadSpeed > 1024) {
            downloadSpeed /= (1024);
            downSpeed = df.format(downloadSpeed) + "Kb/s";
        } else {
            downSpeed = df.format(downloadSpeed) + "b/s";
        }

        if (uploadSpeed > 1024 * 1024) {
            uploadSpeed /= (1024 * 1024);
            upSpeed = df.format(uploadSpeed) + "Mb/s";
        } else if (uploadSpeed > 1024) {
            uploadSpeed /= (1024);
            upSpeed = df.format(uploadSpeed) + "Kb/s";
        } else {
            if (uploadSpeed < 0) {
                uploadSpeed = 0;
            }
            upSpeed = df.format(uploadSpeed) + "b/s";
        }

        updateSpeed("↓ " + downSpeed, "↑ " + upSpeed);
    }


    private static class MsgHandler extends Handler {
        WeakReference<NetSpeed> weakReference;

        public MsgHandler(NetSpeed speed) {
            weakReference = new WeakReference<>(speed);
        }

        @Override
        public void handleMessage(Message msg) {
            NetSpeed netSpeed = weakReference.get();
            if (netSpeed == null) {
                return;
            }
            switch (msg.what) {
                case START_SPEED:
                    netSpeed.calculateNetSpeed();
                    if (!netSpeed.mHandler.hasMessages(START_SPEED)) {
                        netSpeed.mHandler.sendEmptyMessageDelayed(START_SPEED, INTERVAL * 1000);
                    }
                    break;
                case STOP_SPEED:
                    netSpeed.mHandler.removeMessages(START_SPEED);
                    break;
            }
        }
    }
}
