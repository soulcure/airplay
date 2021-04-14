// IIMChannel.aidl
package com.coocaa.sdk;

import com.coocaa.sdk.IResultListener;
import com.coocaa.sdk.entity.IMMessage;
import com.coocaa.sdk.entity.Session;

// Declare any non-default types here with import statements

interface IIMChannel {
    void send(in IMMessage message,in IResultListener callback);

    void sendBroadCastByHttp(in IMMessage message,in IResultListener callback);

    String fileService(in String path);

    Session getMySession();

    Session getTargetSession();


}
