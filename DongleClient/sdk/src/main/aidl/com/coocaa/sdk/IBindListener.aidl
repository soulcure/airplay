// IResultListener.aidl
package com.coocaa.sdk;

// Declare any non-default types here with import statements

interface IBindListener {
    void onSuccess(in String device);

    void onFail(in int code,in String message);
}
