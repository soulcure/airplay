// IUserService.aidl
package com.coocaa.smartsdk;
import com.coocaa.smartsdk.object.IUserInfo;
import com.coocaa.smartsdk.IUserListener;

// Declare any non-default types here with import statements

interface IUserService {
    IUserInfo getUserInfo();
    void addObserveUserInfo(in IUserListener listener);
    void removeUserObserver(in IUserListener listener);
    void showLoginUser();
    void updateAccessToken(in String token);
}
