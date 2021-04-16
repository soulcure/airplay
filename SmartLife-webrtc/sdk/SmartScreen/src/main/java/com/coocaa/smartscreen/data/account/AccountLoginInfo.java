package com.coocaa.smartscreen.data.account;


import java.io.Serializable;

/**
 * @ClassName UserInfo
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2019-12-05
 * @Version TODO (write something)
 * 一键登录或者验证码登录后获取到的登录信息
 */
public class AccountLoginInfo implements Serializable {
    public String access_token;
    public String refresh_token;
    public String session_id;
    public String expires_in;

}
