package com.coocaa.smartsdk;

import com.coocaa.smartsdk.object.IUserInfo;

/**
 * @Author: yuzhan
 */
public interface UserChangeListener {
    void onUserChanged(boolean login, IUserInfo userInfo);
}
