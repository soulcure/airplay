package com.coocaa.smartscreen.connect.callback;

import swaiotos.channel.iot.ss.device.Device;

/**
 * @ClassName BindCallback
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/5/16
 * @Version TODO (write something)
 */
public interface BindCallback {
    void onSuccess(String bindCode, Device device);

    void onFail(String bindCode, String errorType, String msg);
}
