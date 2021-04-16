package com.coocaa.tvpi.module.log;

import android.content.Context;
import android.util.Log;

import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.constant.SmartConstans;
import com.umeng.analytics.MobclickAgent;

import java.util.Map;

import swaiotos.channel.iot.ss.device.Device;

/**
 * @Author: yuzhan
 */
class UmengLogSubmit extends BaseLogSubmit{

    public UmengLogSubmit(Context context) {
        super(context);
    }

    @Override
    public void event(String eventId, Map<String, String> params) {
        submit(new Runnable() {
            @Override
            public void run() {
                if(SmartConstans.getBuildInfo().debug) {
                    Log.d("SmartLog", "submit event : " + eventId + ", params=" + params);
                }
                appendPublicParams(params);
                removeUmengUnSupportParams(params);
                MobclickAgent.onEvent(context, eventId, fullfilParams(params));
            }
        });
    }

    private void removeUmengUnSupportParams(Map<String, String> params) {
        if(params.containsKey("account")) {
            params.remove("account");//移除账户这种过多数据
        }
        if(params.containsKey("ss_device_id")) {
            params.remove("ss_device_id");//移除设备id这种过多数据
        }
    }

    private void appendPublicParams(Map<String, String> params) {
        Device device = SSConnectManager.getInstance().getDevice();
        params.put("ss_device_type", device == null ? "disconnected" : device.getZpRegisterType());
    }
}
