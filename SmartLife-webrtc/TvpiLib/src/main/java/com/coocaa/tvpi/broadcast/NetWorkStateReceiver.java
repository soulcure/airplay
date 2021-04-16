package com.coocaa.tvpi.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.coocaa.tvpi.event.NetworkEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * @ClassName NetWorkStateReceiver
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/6/11
 * @Version TODO (write something)
 */
public class NetWorkStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NetWorkStateReceiver", "onReceive: 监听到网络变化");
        EventBus.getDefault().post(new NetworkEvent());
    }
}
