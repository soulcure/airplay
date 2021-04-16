package com.coocaa.smartscreen.data.device;

import java.io.Serializable;

/**
 * @ClassName ValidCodeData
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 3/5/21
 * @Version TODO (write something)
 */
public class ValidCode implements Serializable {
    public String verificationCode;	//string 非必须验证码(push时，该参数为null)
    public String retryNumber;	//string必须 重试次数
    public String retryIntervalTime;	//string必须 重试间隔时间
}
