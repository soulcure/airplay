package com.coocaa.smartsdk;

import com.coocaa.smartsdk.object.ISmartDeviceInfo;

/**
 * @Author: yuzhan
 */
public class SmartApiListenerImpl implements SmartApiListener{

    @Override
    public void onDeviceConnect(ISmartDeviceInfo deviceInfo) {

    }

    @Override
    public void onDeviceDisconnect() {

    }

    @Override
    public void loginState(int code, String info) {

    }

    @Override
    public void onDispatchMessage(String clientId, String msgJson) {

    }

    @Override
    public void onBindCodeResult(String requestId, String bindCode) {

    }
}
