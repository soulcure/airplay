package com.coocaa.tvpi.module.homepager.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.Nullable;

import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpilib.R;
import com.yhao.floatwindow.FloatWindow;
import com.yhao.floatwindow.MoveType;

public class FloatViewService extends Service {
    private static final String TAG = FloatViewService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotification();
    }

    private void createNotification() {
        Log.d(TAG, "createNotification: ==");
        String channelId = "CHANNEL_FLOAT_ID";
        String channelName = "FloatViewService";
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
            this.startForeground(111, (new Notification.Builder(this, channelId)).build());
        } else {
            this.startForeground(111, new Notification());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initFloatView();
        showFloatView();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissFloatView();
        destroyFloatView();
    }

    private void initFloatView() {
        Log.d(TAG, "initFloatView: ==");
        if(FloatWindow.get(TAG) == null) {
            FloatWindow.with(getApplicationContext())
                    .setView(R.layout.layout_float_controller)
                    .setX(DimensUtils.getDeviceWidth(this) - DimensUtils.dp2Px(this, 76))
                    .setY(DimensUtils.getDeviceHeight(this) - DimensUtils.dp2Px(this, 160))
                    .setDesktopShow(false)
                    .setMoveType(MoveType.active)
                    .setMoveStyle(300, new AccelerateInterpolator())
                    .setTag(TAG)
                    .build();
            FloatWindow.get(TAG).getView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(FloatViewService.this, RemoteActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
                }
            });
        }
    }

    private void showFloatView() {
        Log.d(TAG, "showFloatView: ==");
        if(FloatWindow.get(TAG) != null) {
            FloatWindow.get(TAG).show();
        }
    }

    private void dismissFloatView() {
        if (null != FloatWindow.get(TAG) && FloatWindow.get(TAG).isShowing()) {
            Log.d(TAG, "dismissFloatView: ==");
            FloatWindow.get(TAG).hide();
        }
    }

    private void destroyFloatView(){
        FloatWindow.destroy();
        stopSelf();
    }
}
