// IBindResult.aidl
package swaiotos.channel.iot.ss.device;

// Declare any non-default types here with import statements

import swaiotos.channel.iot.ss.device.Device;

interface IBaseOnDeviceChangedListener {
     void onDeviceOffLine(in Device device);
     void onDeviceOnLine(in Device device);
     void onDeviceUpdate(in Device device);
}
