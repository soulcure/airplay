package com.coocaa.tvpi.module.mall.pay.weixin;

import android.app.Activity;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.module.mall.pay.Pay;
import com.coocaa.tvpi.module.mall.pay.PayParams;

public class WeiXinPay implements Pay {

    @Override
    public void payOrder(Activity activity, PayParams payParams, PayListener payListener) {
        ToastUtils.getInstance().showGlobalShort("暂不支持");
    }
}
