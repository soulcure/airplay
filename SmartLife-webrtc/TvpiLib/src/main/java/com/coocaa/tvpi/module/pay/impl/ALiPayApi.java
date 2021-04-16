package com.coocaa.tvpi.module.pay.impl;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;
import com.coocaa.tvpi.module.pay.PayManager;
import com.coocaa.tvpi.module.pay.PayUtil;
import com.coocaa.tvpi.module.pay.api.CCPayApi;
import com.coocaa.tvpi.module.pay.bean.ALiPayResult;
import com.coocaa.tvpi.module.pay.bean.CCPayReq;

import java.util.Map;


public class ALiPayApi extends CCPayApi {

    public ALiPayApi(Context context) {
        super(context);

    }

    @Override
    public void pay(final CCPayReq req) throws RuntimeException{

        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    PayTask task  = new PayTask((Activity) mContext);
                    Map<String, String> map = task.payV2(req.orderInfo, true);
                    ALiPayResult payResult = new ALiPayResult(map);
//                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    if (TextUtils.equals(resultStatus, "9000")) {
                        PayManager.getInstance().notifyResult(true, resultStatus);
                    } else {
                        PayManager.getInstance().notifyResult(false, resultStatus);
                    }

                } catch (Exception e) {
                    PayManager.getInstance().notifyResult(false, e.toString());
                }
            }
        };
        PayUtil.mService.execute(payRunnable);
    }

}
