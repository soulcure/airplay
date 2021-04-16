package com.coocaa.tvpi.module.pay;

import android.app.Activity;
import android.net.Uri;

import android.text.TextUtils;
import android.util.Log;


import com.alibaba.fastjson.JSON;


import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.smartsdk.pay.IPay;
import com.coocaa.smartsdk.pay.PayResultEvent;
import com.coocaa.tvpi.module.pay.api.IPayResultCallback;
import com.coocaa.tvpi.module.pay.bean.CCPayReq;
import com.coocaa.tvpi.module.pay.bean.PayConstant;
import com.coocaa.tvpi.module.pay.http.HomeHttpRequest;
import com.coocaa.tvpi.module.pay.http.IPayCallback;
import com.coocaa.tvpi.module.pay.http.PayData;
import com.coocaa.tvpi.module.pay.impl.CCPayTask;
import com.coocaa.tvpi.util.TvpiClickUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;


public class PayPresenter implements IPay {

    String TAG = "PaymentExt";
    private static PayPresenter INSTANCE;
    private PayPresenter(){}

    public static PayPresenter getInstance(){
        if(INSTANCE == null){
            synchronized (PayPresenter.class){
                if(INSTANCE == null){
                    INSTANCE = new PayPresenter();
                }
            }
        }
        return INSTANCE;
    }

    private void pay(Activity activity, String id,PayData payData,String payConstant,IPayResultNotifyAty iPayResultNotifyAty){
        if(payData == null){
            Log.d(TAG,"startPay....payData is null");
            return;
        }
        switch (payConstant){
            case PayConstant.PAY_ALI:
                requestALiPayment(activity,id,payData,iPayResultNotifyAty);
                break;
            case PayConstant.PAY_WE:
                requestWePayment(activity, id, payData);
                break;
        }
    }



    public void  requestPay(Activity activity,String id, String params,String payType,IPayResultNotifyAty iPayResultNotifyAty){
        String url = "";
        if(SmartConstans.isTestServer()){
            url = "https://beta-pay.coocaa.com/MyCoocaa/pay/order_entrustweb.action?";
        }else{
            url="http://pay.coocaa.com/MyCoocaa/pay/order_entrustweb.action?";
        }
        Log.d(TAG,"start request, url=" + (url+params) + ", payType=" + payType);
        HomeHttpRequest.request(url+params,payType, new IPayCallback() {
            @Override
            public void payDataSuccessCallback(PayData data) {
                Log.d(TAG,"payDataSuccessCallback.........data:"+data.toString());
                pay(activity,id,data,payType,iPayResultNotifyAty);
            }

            @Override
            public void payDataErrorCallback(Exception e) {
                Log.d(TAG,"payDataErrorCallback........error:"+e.toString());
                onPayResult(PayResultEvent.STATUS_FAIL,id);
                iPayResultNotifyAty.notifityActivity(PayResultEvent.STATUS_CANCEL);
            }

            @Override
            public void payDataParamsErrorCallback(String error) {
                Log.d(TAG,"payDataParamsErrorCallback.........error:"+error);
                onPayResult(PayResultEvent.STATUS_FAIL,id);
                iPayResultNotifyAty.notifityActivity(PayResultEvent.STATUS_FAIL);
            }
        });
    }



    private void requestWePayment(Activity context, String id, PayData payParams) {
        if(payParams == null || TextUtils.isEmpty(payParams.getJs_api_param())){
            Log.d(TAG,"PayData or PayParams is null");
            return;
        }
        Log.d(TAG,"requestWePayment.........PayData:"+payParams);
        CCPayReq req = new CCPayReq();
        try{
            String apiParams = payParams.getJs_api_param();
            Object jb = JSON.parse(apiParams);
            Map<String, String> map = (Map<String, String>)jb;

            req.appId = map.get("appid");
            req.extData = map.get("extData");
            req.partnerId = map.get("partnerid");
            req.prepayId = map.get("prepayid");
            req.nonceStr = map.get("noncestr");
            req.packageValue = map.get("package");
            req.timeStamp = map.get("timestamp");
            req.sign = map.get("sign");
            req.type = PayConstant.PAY_WE;
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG,"requestWePayment.....error:"+e.toString());
        }

        CCPayTask mTask = new CCPayTask(context, new IPayResultCallback() {
            @Override
            public void paySuccessed() {
                Log.d(TAG, "success...");
                onPayResult(PayResultEvent.STATUS_SUCCESS,id);
            }

            @Override
            public void payCancel() {
                Log.d(TAG, "payCancel...");
                onPayResult(PayResultEvent.STATUS_CANCEL,id);
            }

            @Override
            public void payFailed(String reason) {
                Log.d(TAG, "payFailed...reason:"+reason);
                onPayResult(PayResultEvent.STATUS_FAIL,id);
            }
        }, false);
        mTask.pay(req);
    }


    private void onPayResult(String status,String id) {
        EventBus.getDefault().post(new PayResultEvent(status, id));
    }

    private void requestALiPayment(Activity context,String id, PayData payParams,IPayResultNotifyAty iPayResultNotifyAty) {
        if(payParams == null || TextUtils.isEmpty(payParams.getJs_api_param())){
            Log.d(TAG,"PayData or PayParams is null");
            return;
        }
        Log.d(TAG,"requestALiPayment.....PayData:"+payParams.toString());
        CCPayReq req = new CCPayReq();
        req.type = PayConstant.PAY_ALI;
        req.orderInfo = payParams.getJs_api_param();

        CCPayTask mTask = new CCPayTask(context, new IPayResultCallback() {
            @Override
            public void paySuccessed() {
                Log.d(TAG, "success...");

                onPayResult(PayResultEvent.STATUS_SUCCESS,id);
                iPayResultNotifyAty.notifityActivity(PayResultEvent.STATUS_SUCCESS);
            }

            @Override
            public void payCancel() {
                Log.d(TAG, "payCancel...thread:"+Thread.currentThread().getName());

                onPayResult(PayResultEvent.STATUS_CANCEL,id);
                iPayResultNotifyAty.notifityActivity(PayResultEvent.STATUS_CANCEL);
            }

            @Override
            public void payFailed(String reason) {
                Log.d(TAG, "payFailed...reason:"+reason);
                onPayResult(PayResultEvent.STATUS_FAIL+":code_"+reason,id);
                iPayResultNotifyAty.notifityActivity(PayResultEvent.STATUS_FAIL);
            }
        }, false);
        mTask.pay(req);
    }

    String payId;
    String payJson;

    @Override
    public void startPay(Activity activity, String id, String json) {
        this.payId = id;
        this.payJson = json;
        try{
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("np").authority("com.coocaa.smart.pay").path("index");
            builder.appendQueryParameter("name", "支付中心");
            builder.appendQueryParameter("request_pay_params", json);
            builder.appendQueryParameter("id", id);
            TvpiClickUtil.onClick(activity, builder.build().toString());
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG,"placeOrder.......error:"+e.toString());
        }
    }
}
