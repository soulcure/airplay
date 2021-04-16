package com.coocaa.swaiotos.virtualinput;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.swaiotos.virtualinput.utils.VirtualInputUtils;

import swaiotos.channel.iot.ss.device.Device;

/**
 * @Author: yuzhan
 */
public class VirtualInputStarter {

    public static void show(Context context, boolean isAuto) {
        boolean needShowGuide = false; //VirtualInputUtils.needShowGuide(context);
        if(!isAuto && !needShowGuide) {
            return ;//只有首次投屏，和需要自动显示的时候才需要这么做
        }

        boolean isConnected = SSConnectManager.getInstance().isConnected();
        Device device = SSConnectManager.getInstance().getDevice();
        Log.d("SmartVI", "start virtualInput");
        if (isConnected && device != null) {
            Intent intent = new Intent();
            intent.setPackage(context.getPackageName());
            if(! (context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            String url = "np://com.coocaa.smart.floatvirtualinput/index?from=floatui";
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        }
    }
}
