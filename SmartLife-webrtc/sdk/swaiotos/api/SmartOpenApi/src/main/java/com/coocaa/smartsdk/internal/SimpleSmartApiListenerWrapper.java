package com.coocaa.smartsdk.internal;

import com.coocaa.smartsdk.SimpleSmartApiListener;

/**
 * @Author: yuzhan
 */
public class SimpleSmartApiListenerWrapper {

    public SimpleSmartApiListener listener;
    public Object args;

    public SimpleSmartApiListenerWrapper(SimpleSmartApiListener listener, Object args) {
        this.listener = listener;
        this.args = args;
    }
}
