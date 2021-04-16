package com.coocaa.statemanager.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.coocaa.statemanager.businessstate.BusinessStateTvReport;
import com.coocaa.statemanager.common.bean.User;

import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.utils.EmptyUtils;


/**
 * Describe: 用户断开连接Service
 * Created by AwenZeng on 2020/1/27
 */
public class DisconnectService extends IntentService {
    private static final String TAG = "DisconnectService";

    public DisconnectService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            @SuppressLint("WrongConstant")
            NotificationChannel channel = new NotificationChannel("41", "DisconnectService", NotificationManager.IMPORTANCE_DEFAULT);

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
        try {
            IMMessage imMessage = intent.getExtras().getParcelable("mMessage");
            Log.d(TAG,"DisconnectService state:"+" imMessage:"+imMessage);
            String uid = imMessage.getExtra("open_id");
            String owner = imMessage.getExtra("owner");
            if(EmptyUtils.isNotEmpty(uid)){
                BusinessStateTvReport.getDefault().disconnectBackHome(uid);
            }else if(EmptyUtils.isNotEmpty(owner)){
                User user = User.decode(owner);
                BusinessStateTvReport.getDefault().disconnectBackHome(user.userID);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
