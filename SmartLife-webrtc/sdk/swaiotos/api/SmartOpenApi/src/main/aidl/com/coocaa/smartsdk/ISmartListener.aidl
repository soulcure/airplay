// ISmartListener.aidl
package com.coocaa.smartsdk;

import com.coocaa.smartsdk.object.ISmartDeviceInfo;
// Declare any non-default types here with import statements

interface ISmartListener {
    void onDeviceConnect(in ISmartDeviceInfo deviceInfo);
    void onDeviceDisconnect();
    void loginState(in int code, in String info);
    void onDispatchMessage(in String clientId, in String msgJson);
    void onBindCodeResult(String requestId, String bindCode);
}
