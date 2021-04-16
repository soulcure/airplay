package com.coocaa.tvpi.module.mall.pay;

import android.app.Activity;

/**
 * 支付接口
 * Created by songxing on 2020/8/26
 */
public interface Pay {

    void payOrder(Activity activity, PayParams payParams, PayListener payListener);

    public static interface PayListener {

        void onSuccess();

        void onFail(String code, String message);
    }
}
