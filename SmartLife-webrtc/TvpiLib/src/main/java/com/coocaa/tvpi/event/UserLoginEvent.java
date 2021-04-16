package com.coocaa.tvpi.event;

/**
 * Created by IceStorm on 2017/12/13.
 */

public class UserLoginEvent {
    public boolean isLogin;
    public UserLoginEvent(boolean isLogin) {
        this.isLogin = isLogin;
    }
}
