// IResultListener.aidl
package com.coocaa.sdk;

import com.coocaa.sdk.entity.IMMessage;

// Declare any non-default types here with import statements


//消息回调
interface IReceiveMessage {
   void OnRec(in String targetClient, in IMMessage msg);
}
