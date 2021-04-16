package swaiotos.channel.iot.ss.controller;

/**
 * @ClassName: DeviceStateManager
 * @Author: lu
 * @CreateDate: 2020/4/22 8:42 PM
 * @Description:
 */
public interface DeviceStateManager {
    interface OnDeviceStateChangeListener {
        void onDeviceStateUpdate(DeviceState state);
    }

    void open();

    void close();

    void addMyDeviceOnDeviceStateChangeListener(OnDeviceStateChangeListener listener);

    void removeMyDeviceOnDeviceStateChangeListener(OnDeviceStateChangeListener listener);

    void addOnDeviceStateChangeListener(OnDeviceStateChangeListener listener);

    void removeOnDeviceStateChangeListener(OnDeviceStateChangeListener listener);

    void updateConnective(String connective, String value,boolean notify);

    void updateDeviceState(long timestamp, DeviceState state);

    void reflushDeviceStateOfSid();
}
