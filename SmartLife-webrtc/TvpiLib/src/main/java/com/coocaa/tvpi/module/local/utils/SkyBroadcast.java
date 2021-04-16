package com.coocaa.tvpi.module.local.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Created by dvlee1 on 11/19/15.
 * 注: 所有广播Action都写在SkyAction枚举里面
 */
public class SkyBroadcast {

    private static final String TAG = SkyBroadcast.class.getSimpleName();
    /**
     * 所有广播的Action
     */
    public enum SkyAction{
        /** 设备相关 */
        DEVICE_CONNECTED,
        DEVICE_DISCONNECTED,
        GOT_APP_VERSION,
        BOTTOM_AD_VISIABLE,
        BOTTOM_AD_GONE,
        COUNT_OF_LOCAL_IMAGE,

        /* 上传影单 */
        SUBMIT_VIDEOLIST,
        SUBMIT_VIDEOLIST_FAILED,
        SUBMIT_VIDEOLIST_SUCCESS,

        /* 关注 */
        FOLLOW_RESULT,
        UNFOLLOW_RESULT,

        /*个人资料*/
        MODIFY_STH
    }

    /**
     * 发送广播
     * @param context
     * @param action
     */
    public static void send(Context context,SkyAction action){
        Log.d(TAG, "SkyBroadcast send " + action.toString());
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(action.toString()));
    }

    public static void send(Context context,SkyAction action,Intent intent){
        intent.setAction(action.toString());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * 注册监听
     * @param context
     * @param receiver
     * @param actions
     */
    public static void register(Context context,BroadcastReceiver receiver,SkyAction... actions){
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        IntentFilter intentFilter = new IntentFilter();
        for(SkyAction action : actions){
            intentFilter.addAction(action.toString());
        }
        lbm.registerReceiver(receiver, intentFilter);
    }

    /**
     * 取消注册监听
     * @param context
     * @param receiver
     */
    public static void unregister(Context context,BroadcastReceiver receiver){
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.unregisterReceiver(receiver);
    }
}
