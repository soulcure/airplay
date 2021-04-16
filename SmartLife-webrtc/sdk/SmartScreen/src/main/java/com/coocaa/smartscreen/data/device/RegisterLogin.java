package com.coocaa.smartscreen.data.device;

import java.io.Serializable;

/**
 * @ClassName RegisterLogin
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/4/26
 * @Version TODO (write something)
 */
public class RegisterLogin implements Serializable {
    public String access_token;
    public String zpLsid;//如果zpRegisterType是openid会返回，其他的不会
    public String redirect_url;
}
