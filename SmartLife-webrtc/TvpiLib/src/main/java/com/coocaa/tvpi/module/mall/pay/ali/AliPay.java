package com.coocaa.tvpi.module.mall.pay.ali;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.alipay.sdk.app.PayTask;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.mall.pay.Pay;
import com.coocaa.tvpi.module.mall.pay.PayParams;

import java.util.Map;

public class AliPay implements Pay {
    private static final String TAG = AliPay.class.getSimpleName();

    @Override
    public void payOrder(Activity activity, PayParams payParams, PayListener payListener) {
        final Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    PayTask payTask = new PayTask(activity);
                    Map<String, String> result = payTask.payV2(payParams.getOrderInfo(), true);
                    AliPayResult payResult = new AliPayResult(result);
                    String resultStatus = payResult.getResultStatus();
                    Log.d(TAG, "run: payResult" + payResult);
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        if (payListener != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                                    payListener.onSuccess();
                                }
                            });
                        }
                    } else {
                        if (payListener != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                                    payListener.onFail(resultStatus, payResult.getMemo());
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        // 必须异步调用
        HomeIOThread.execute(payRunnable);
    }
}
