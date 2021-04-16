package com.coocaa.statemanager.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.coocaa.statemanager.businessstate.BusinessStateTvReport;

import swaiotos.channel.iot.utils.EmptyUtils;
import swaiotos.channel.iot.utils.ThreadManager;


/**
 * Describe: 业务状态上报Service
 * Created by AwenZeng on 2020/1/27
 */
public class BusinessStateReportService extends Service {

    private static final String TAG = "BusinessState";

    private static final String STATE_CMD = "cmd";
    private static final String STATE_ID = "id";
    private static final String STATE_VALUE = "values";

    private static final String CMD_BUSINESS_STATE_REPORT = "report";
    private static final String CMD_BUSINESS_STATE_EXIT = "exit";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BusinessStateReportService start");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            @SuppressLint("WrongConstant")
            NotificationChannel channel = new NotificationChannel("41", "BusinessStateReportService", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setSound(null, null);
            //数字是随便写的“40”，
            nm.createNotificationChannel(channel);
            Notification.Builder builder = new Notification.Builder(this, "41");
            //其中的2，是也随便写的，正式项目也是随便写
            startForeground(2, builder.build());
        }
        BusinessStateTvReport.getDefault().init(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleData(intent);
        return super.onStartCommand(intent, flags, startId);
    }
    /**
     * 处理相关业务数据
     *
     * @param intent
     */
    private synchronized void handleData(Intent intent) {
        try {
           String id = intent.getStringExtra(STATE_ID);
           String values = intent.getStringExtra(STATE_VALUE);
            Log.d(TAG, "BusinessStateReportService id:" + id);
            if (EmptyUtils.isNotEmpty(values)) {
                JSONObject jsonObject = JSONObject.parseObject(values);
                String cmd = jsonObject.getString(STATE_CMD);
                if (EmptyUtils.isNotEmpty(cmd)) {
                    if(cmd.equals(CMD_BUSINESS_STATE_EXIT)){
                        BusinessStateTvReport.getDefault().exitBusiness();
                    }else{
                        updateBusinessState(id,values);
                    }
                } else {
                    updateBusinessState(id,values);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void updateBusinessState(final String id, final String values){
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                BusinessStateTvReport.getDefault().updateBusinessState(id, values);
            }
        },1000);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
