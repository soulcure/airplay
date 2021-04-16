package com.tianci.user.api;

import com.tianci.user.api.listener.OnAccountChangedListener;

import java.io.Serializable;
import java.util.Map;

public interface IUser extends Serializable {

    /**
     * 判断当前电视是否有账号登录<br/>
     *
     * @return boolean
     */
    boolean hasLogin();

    /**
     * 获取当前登录的账号信息<br/>
     * 具体数据参考：https://beta.passport.coocaa.com/html2/user-guide.html# 中的“获取用户信息”
     */
    // String getInfo();
    Map<String, Object> getInfo();

    /**
     * 获取登录账号的access token<br/>
     *
     * @return String
     */
    String getToken();

    String getToken(String type);

    /**
     * 概述：获取当前用户session<br/>
     *
     * @return String
     */
    String getSession();

    /**
     * 退出账号登录
     */
    boolean logout();

    /**
     * 2020/7/10 新增，为影视提供登录接口
     *
     * @param accessToken 酷开账户的access token
     */
    boolean loginByToken(String accessToken);

    /**
     * @return 是否公网环境，区分广电机器
     */
    boolean isPublicAddress();

    /**
     * 触发账户的刷新信息的动作
     */
    boolean refreshAccountInfo();

    /**
     * 注册账户变化广播
     *
     * @param listener
     * @return
     */
    boolean registerAccountChanged(OnAccountChangedListener listener);

    /**
     * 注销账户变化广播
     *
     * @param listener
     * @return
     */
    boolean unregisterAccountChanged(OnAccountChangedListener listener);
}
