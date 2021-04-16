package com.coocaa.tvpi.module.pay.api;

import android.content.Context;
import android.os.Looper;

import com.coocaa.tvpi.module.pay.bean.CCPayReq;


public class CCPayApi implements IPayApi {
    protected Context mContext;

    public CCPayApi(){

    }
    public CCPayApi(Context context) {
        mContext = context;
    }



    @Override
    public void pay(CCPayReq req) {

    }

    protected boolean isMainThread(){
        return Looper.getMainLooper() == Looper.myLooper();
    }
}
