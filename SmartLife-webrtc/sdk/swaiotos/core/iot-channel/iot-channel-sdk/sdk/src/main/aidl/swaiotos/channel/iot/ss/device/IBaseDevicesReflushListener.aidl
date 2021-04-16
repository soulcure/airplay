// IBindResult.aidl
package swaiotos.channel.iot.ss.device;

// Declare any non-default types here with import statements
import swaiotos.channel.iot.ss.device.Device;

interface IBaseDevicesReflushListener {
     void onDeviceReflushUpdate(in List<Device> devices);
}
