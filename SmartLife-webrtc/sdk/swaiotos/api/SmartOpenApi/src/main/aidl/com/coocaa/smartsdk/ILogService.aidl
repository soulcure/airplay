// ILogService.aidl
package com.coocaa.smartsdk;

// Declare any non-default types here with import statements

interface ILogService {
    void submitLog(in String name, in Map params);
    void submitLogWithTag(in String tag, in String name, in Map params);
}
