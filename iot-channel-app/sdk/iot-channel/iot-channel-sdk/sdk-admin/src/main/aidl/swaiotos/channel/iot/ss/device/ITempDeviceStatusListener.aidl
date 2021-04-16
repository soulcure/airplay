// IBindResult.aidl
package swaiotos.channel.iot.ss.device;

// Declare any non-default types here with import statements

interface ITempDeviceStatusListener {
     void handleJoin(String sid);
     void handleLeave(String sid);
}
