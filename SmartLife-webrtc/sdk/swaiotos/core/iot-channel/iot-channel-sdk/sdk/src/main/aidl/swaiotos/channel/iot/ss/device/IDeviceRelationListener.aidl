// IBindResult.aidl
package swaiotos.channel.iot.ss.device;

// Declare any non-default types here with import statements

interface IDeviceRelationListener {
     void onDeviceBind(String lsid);
     void onDeviceUnbind(String lsid);
}
