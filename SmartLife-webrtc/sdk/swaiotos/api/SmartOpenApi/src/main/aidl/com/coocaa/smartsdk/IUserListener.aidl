// IUserListener.aidl
package com.coocaa.smartsdk;
import com.coocaa.smartsdk.object.IUserInfo;
// Declare any non-default types here with import statements

interface IUserListener {
    void onUserChanged(in boolean login, in IUserInfo userInfo);
}