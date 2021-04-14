package swaiotos.channel.iot.callback;

import java.util.List;

import swaiotos.channel.iot.db.bean.Device;

public interface DeviceCallback {
    void onSuccess(List<Device> list);

    void onFail(int code, String msg);
}
