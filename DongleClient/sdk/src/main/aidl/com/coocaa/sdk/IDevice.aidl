// IIMChannel.aidl
package com.coocaa.sdk;

import com.coocaa.sdk.entity.Session;
import com.coocaa.sdk.IBindListener;

// Declare any non-default types here with import statements

interface IDevice {

    void bindDevice(in String bindCode,in IBindListener callback);

    Session connect(in String sid,in long timeout);

    String getCurrentDevice();
}
