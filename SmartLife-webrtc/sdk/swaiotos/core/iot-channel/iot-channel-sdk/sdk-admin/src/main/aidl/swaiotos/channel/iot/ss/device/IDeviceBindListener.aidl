// IBindResult.aidl
package swaiotos.channel.iot.ss.device;

// Declare any non-default types here with import statements

interface IDeviceBindListener {
     void onDeviceBind(String lsid);
     void onDeviceUnbind(String lsid);
}
