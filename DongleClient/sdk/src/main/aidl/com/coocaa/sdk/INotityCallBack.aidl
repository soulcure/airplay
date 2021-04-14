// INotityCallBack.aidl
package com.coocaa.sdk;
import com.coocaa.sdk.IReceiveMessage;


//设置接收消息的监听器
interface INotityCallBack {
    void registerCallback(in String targetClient,in IReceiveMessage cb);
    void unregisterCallback(in String targetClient,in IReceiveMessage cb);
}
