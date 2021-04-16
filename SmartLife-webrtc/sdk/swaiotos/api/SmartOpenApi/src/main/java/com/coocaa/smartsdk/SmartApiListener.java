package com.coocaa.smartsdk;

import com.coocaa.smartsdk.object.ISmartDeviceInfo;

/**
 * @Author: yuzhan
 */
public interface SmartApiListener {
    void onDeviceConnect(ISmartDeviceInfo deviceInfo);
    void onDeviceDisconnect();
    void loginState(int code, String info);
    void onDispatchMessage(String clientId, String msgJson);
    void onBindCodeResult(String requestId, String bindCode);
}
