package swaiotos.channel.iot.callback;

import swaiotos.channel.iot.db.bean.Device;

public interface BindResult {
    void onSuccess(Device deviceData);

    void onFail(int code, String message);
}
