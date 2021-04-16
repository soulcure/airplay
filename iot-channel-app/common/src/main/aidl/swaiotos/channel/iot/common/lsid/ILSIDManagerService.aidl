// ILSIDManagerService.aidl
package swaiotos.channel.iot.common.lsid;

// Declare any non-default types here with import statements

import swaiotos.channel.iot.common.lsid.IRemoteServiceCallback;
import swaiotos.channel.iot.common.lsid.LSID;

interface ILSIDManagerService {

    LSID getLSID();

//    LSID registerUser();

    void reset();

    void registerCallback(IRemoteServiceCallback cb);
//
    void unregisterCallback(IRemoteServiceCallback cb);

}
