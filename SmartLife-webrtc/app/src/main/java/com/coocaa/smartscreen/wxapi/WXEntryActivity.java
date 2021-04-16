package com.coocaa.smartscreen.wxapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.coocaa.smartscreen.constant.SmartConstans;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    final static String TAG = "SmartWx";
    private IWXAPI api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, SmartConstans.getBusinessInfo().APPID_WECHAT);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.d(TAG, "onReq : " + baseReq);
        finish();
    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.d(TAG, "onResp : " + baseResp);
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                Log.d(TAG, "ERR_OK");
                SharedPreferences getdata = getSharedPreferences("YA_USERINFO", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = getdata.edit();
                editor.putString("wx_code", ((SendAuth.Resp) baseResp).code);
                editor.apply();
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                Log.d(TAG, "ERR_USER_CANCEL");
                break;
        }
        finish();
    }
}
