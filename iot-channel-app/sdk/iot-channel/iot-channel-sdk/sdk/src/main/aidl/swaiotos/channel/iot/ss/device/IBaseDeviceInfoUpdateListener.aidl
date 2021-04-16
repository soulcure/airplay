// IBindResult.aidl
package swaiotos.channel.iot.ss.device;

// Declare any non-default types here with import statements
import swaiotos.channel.iot.ss.device.Device;

interface IBaseDeviceInfoUpdateListener {
     void onDeviceInfoUpdate(in List<Device> devices);
     void sseLoginSuccess();
     void loginState(int code,String info);
     void loginConnectingState(int code,String info);
}
