// IOnMySessionUpdateListener.aidl
package swaiotos.channel.iot.ss.session;

// Declare any non-default types here with import statements

import swaiotos.channel.iot.ss.session.Session;
interface IOnMySessionUpdateListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onMySessionUpdate(in Session mySession);
}
