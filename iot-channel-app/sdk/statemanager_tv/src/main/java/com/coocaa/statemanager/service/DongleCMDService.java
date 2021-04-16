package com.coocaa.statemanager.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.coocaa.statemanager.businessstate.BusinessStateTvReport;
import com.coocaa.statemanager.common.bean.CmdData;

import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.utils.EmptyUtils;


/**
 * Describe: Dongle端指令处理Service
 * Created by AwenZeng on 2020/1/27
 */
public class DongleCMDService extends IntentService {
    private static final String TAG = "DongleCMDService";

    public DongleCMDService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            @SuppressLint("WrongConstant")
            NotificationChannel channel = new NotificationChannel("41", "DongleCMDService", NotificationManager.IMPORTANCE_DEFAULT);

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
            Log.d(TAG,"DongleCMDService state:"+" imMessage:"+imMessage);
            CmdData cmdData = JSON.parseObject(imMessage.getContent(),CmdData.class);
            Log.d(TAG,"CmdData:"+cmdData.toJson());
            handCmd(imMessage,cmdData);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handCmd(IMMessage imMessage,CmdData cmdData){
        if(EmptyUtils.isNotEmpty(cmdData)&&EmptyUtils.isNotEmpty(cmdData.param)){
            switch (cmdData.param){
                case "getSystemUpgradeState":
                    BusinessStateTvReport.getDefault().reportSystemUpdateState(imMessage);
                    break;
            }
        }

    }
}
