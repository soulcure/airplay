// IControllerService.aidl
package swaiotos.channel.iot.ss.controller;

// Declare any non-default types here with import statements

import swaiotos.channel.iot.utils.ipc.ParcelableObject;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.ss.device.IConnectResult;

interface IControllerService {
     ParcelableObject connect(String lsid,long timeout);

     void disconnect(in Session session);

     ParcelableObject getClientVersion(in Session target, String client,long timeout);

     ParcelableObject getDeviceInfo();

     ParcelableObject join(String roomId,String sid,long timeout);

     void leave(String userQuit);

     void connectSSETest(String lsid, in IConnectResult result);

     void connectLocalTest(String ip, in IConnectResult result);

}
