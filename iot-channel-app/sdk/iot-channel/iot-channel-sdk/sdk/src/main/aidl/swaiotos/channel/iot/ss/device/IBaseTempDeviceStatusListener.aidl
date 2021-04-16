// IBindResult.aidl
package swaiotos.channel.iot.ss.device;

// Declare any non-default types here with import statements

interface IBaseTempDeviceStatusListener {
     void handleJoin(String sid);
     void handleLeave(String sid);
}
