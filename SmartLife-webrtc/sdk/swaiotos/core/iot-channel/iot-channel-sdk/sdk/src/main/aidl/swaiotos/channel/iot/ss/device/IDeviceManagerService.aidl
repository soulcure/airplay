// IbindDevices.aidl
package swaiotos.channel.iot.ss.device;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.IBaseOnDeviceChangedListener;
import swaiotos.channel.iot.ss.device.IDeviceRelationListener;
import swaiotos.channel.iot.ss.device.IBaseDeviceInfoUpdateListener;
import swaiotos.channel.iot.ss.device.IBaseDevicesReflushListener;
import swaiotos.channel.iot.ss.device.IBaseTempDeviceStatusListener;

interface IDeviceManagerService {
    List<Device> getDevices();

    List<Device> getDeviceOnlineStatus();

    Device getCurrentDevice();

    void addOnDeviceChangedListener(in IBaseOnDeviceChangedListener listener);
    void removeOnDeviceChangedListener(in IBaseOnDeviceChangedListener listener);
    void addDeviceBindListener(in IDeviceRelationListener listener);
    void removeDeviceBindListener(in IDeviceRelationListener listener);
    void addDeviceInfoUpdateListener(in IBaseDeviceInfoUpdateListener listener);
    void removeDeviceInfoUpdateListener(in IBaseDeviceInfoUpdateListener listener);
    void addDevicesReflushListener(in IBaseDevicesReflushListener listener);
    void removeDevicesReflushListener(in IBaseDevicesReflushListener listener);

    List<Device> updateDeviceList();

    String getAccessToken();

}
