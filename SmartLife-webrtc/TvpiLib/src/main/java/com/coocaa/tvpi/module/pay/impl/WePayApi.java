package com.coocaa.tvpi.module.pay.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.tvpi.module.pay.PayManager;
import com.coocaa.tvpi.module.pay.api.CCPayApi;
import com.coocaa.tvpi.module.pay.bean.CCPayReq;
import com.coocaa.tvpi.module.pay.bean.PayConstant;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public final class WePayApi extends CCPayApi {
    private IWXAPI mApi;
    private IntentFilter registerWx = new IntentFilter(PayConstant.PAY_ACTION);
    private String appId;

    private boolean isRegister = false;

    public WePayApi(Context context) {
        super(context);
    }

    @Override
    public void pay(CCPayReq ccPayReq) {
        try{
            appId = ccPayReq.appId;
            if (mApi == null)
                mApi = WXAPIFactory.createWXAPI(mContext, ccPayReq.appId);
            boolean mWXAppInstalled = mApi.isWXAppInstalled();
            if (!mWXAppInstalled) {
                return;
            }
            mContext.registerReceiver(mReceiver, registerWx);
            if (!isRegister)
                mApi.registerApp(appId);
            Log.e("CCPay","isRegister = "+isRegister + " | req = "
                    +ccPayReq);
            PayReq req = new PayReq();
            req.appId = appId;
            req.partnerId = ccPayReq.partnerId;
            req.prepayId = ccPayReq.prepayId;
            req.nonceStr = ccPayReq.nonceStr;
            req.timeStamp = ccPayReq.timeStamp;
            req.packageValue = ccPayReq.packageValue;
            req.sign = ccPayReq.sign;
            req.extData = ccPayReq.extData;
            mApi.sendReq(req);

        }catch (Exception e){
            Log.d("CCPay","error:"+e.toString());
        }

    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("CCPay","intent == "+intent.getAction());
            if (!TextUtils.isEmpty(appId)) {
                isRegister = mApi.registerApp(appId);
            }
        }
    };

    public void unregisterReceiver() {
        if (mReceiver != null && mContext != null) {
            mContext.unregisterReceiver(mReceiver);
        }
    }
}
