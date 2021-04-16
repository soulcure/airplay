package com.coocaa.statemanager.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.coocaa.statemanager.StateManager;

import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.utils.EmptyUtils;


/**
 * @author AwenZeng
 */
public class BigQRCodeService extends IntentService {
    private static final String TAG = "BigQRCodeService";

    public BigQRCodeService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            @SuppressLint("WrongConstant")
            NotificationChannel channel = new NotificationChannel("41", "BigQRCodeService", NotificationManager.IMPORTANCE_DEFAULT);

            channel.setSound(null, null);

            //数字是随便写的“40”，
            nm.createNotificationChannel(channel);
            Notification.Builder builder = new Notification.Builder(this, "41");
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "40");

            //其中的2，是也随便写的，正式项目也是随便写
            startForeground(2, builder.build());
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG,"展示大的共享二维码Service启动");
        StateManager.INSTANCE.showBigQrCodeWindow();
    }
}
