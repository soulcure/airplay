// ISSChannelService.aidl
 package swaiotos.channel.iot.ss;

 // Declare any non-default types here with import statements

 import swaiotos.channel.iot.ss.session.ISessionManagerService;
 import swaiotos.channel.iot.ss.channel.im.IIMChannelService;
 import swaiotos.channel.iot.ss.channel.stream.IStreamChannelService;
 import swaiotos.channel.iot.ss.device.IDeviceManagerService;

 interface ISSChannelService {
     ISessionManagerService getSessionManager();

     IIMChannelService getIMChannel();

     IStreamChannelService getStreamChannel();

     IDeviceManagerService getDeviceManager();

     IBinder getBinder(String name);
 }
