package com.coocaa.tvpi.module.mall.pay;

import com.coocaa.tvpi.module.mall.pay.ali.AliPay;
import com.coocaa.tvpi.module.mall.pay.cloud.CloudPay;
import com.coocaa.tvpi.module.mall.pay.weixin.WeiXinPay;


/**
 * 支付类简单工厂
 * Created by songxing on 2020/8/28
 */
public class PayFactory {
    public static final String PAY_TYPE_ALI = "alipay";
    public static final String PAY_TYPE_WEIXIN = "weixinpay";
    public static final String PAY_TYPE_CLOUD = "cloudpay";

    public static Pay createPay(String payType) {
        Pay pay = null;
        switch (payType) {
            case PAY_TYPE_WEIXIN:
                pay = new WeiXinPay();
                break;
            case PAY_TYPE_CLOUD:
                pay = new CloudPay();
                break;
            case PAY_TYPE_ALI:
                pay = new AliPay();
                break;
        }
        return pay;
    }
}
