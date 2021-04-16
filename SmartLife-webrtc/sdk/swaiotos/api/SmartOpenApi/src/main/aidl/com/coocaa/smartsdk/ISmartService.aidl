// ISmartService.aidl
package com.coocaa.smartsdk;
import com.coocaa.smartsdk.ISmartListener;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;

// Declare any non-default types here with import statements

interface ISmartService {
    void addListener(in ISmartListener listener);
    void removeListener(in ISmartListener listener);
    boolean isDeviceConnect();
    ISmartDeviceInfo getConnectDeviceInfo();
    void startConnectDevice();
    void startConnectSameWifi(in String title);
    boolean isSameWifi();
    void setMsgDispatchEnable(in String clientId, in boolean enable);
    void startWxMP(in String id, in String path);
    void startAppStore(in String pkg);
    boolean hasDevice();
    void requestBindCode(String requestId);
}
