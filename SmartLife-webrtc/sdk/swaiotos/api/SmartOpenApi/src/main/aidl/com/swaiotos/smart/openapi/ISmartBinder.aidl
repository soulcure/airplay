// ISmartBinder.aidl
package com.swaiotos.smart.openapi;

// Declare any non-default types here with import statements

interface ISmartBinder {
    IBinder getBinder(in int type);
}