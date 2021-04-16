package swaiotos.channel.iot.ss.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.manager.lsid.LSIDManager;

/**
 * @ClassName: SSChannelServiceManager
 * @Author: lu
 * @CreateDate: 2020/4/26 10:29 AM
 * @Description:
 */
public abstract class SmartScreenManager<T extends DeviceInfo> {

    public void onCreate(Context context)throws Exception {
        performCreate(context);
    }

    /**
     * 创建mananger，每个SSChannelService实例创建的时候会调用一次
     *
     * @param context the context
     */
    protected abstract void performCreate(Context context) throws Exception;

    /**
     * 获取当前设备的设备信息
     *
     * @param context the context
     * @return the device info
     */
    public abstract T getDeviceInfo(Context context);

    /**
     * 校验SSChannelClientService是否有授权
     *
     * @param context the context
     * @param cn      the cn
     * @param id      the id
     * @param key     the key
     * @return the boolean
     */
    public abstract boolean performClientVerify(Context context, ComponentName cn, String id, String key);

    public abstract LSIDManager getLSIDManager();

    /**
     * 获取关联的SSChannelClientService的Intent
     * SSChannelService会通过此Intent去获取系统中所有符合Intent的Component
     *
     * @param context the context
     * @return 返回空则不会扫描Client
     */
    public Intent getClientServiceIntent(Context context) {
        return new Intent(SSChannelClient.DEFAULT_ACTION);
    }
}