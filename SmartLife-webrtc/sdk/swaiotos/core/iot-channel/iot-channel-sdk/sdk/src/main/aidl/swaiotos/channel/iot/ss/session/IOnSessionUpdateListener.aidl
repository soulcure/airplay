// IBindResult.aidl
package swaiotos.channel.iot.ss.session;

// Declare any non-default types here with import statements

import swaiotos.channel.iot.ss.session.Session;

interface IOnSessionUpdateListener {
    void onSessionConnect(in Session session);
    void onSessionUpdate(in Session session);
    void onSessionDisconnect(in Session session);
}
