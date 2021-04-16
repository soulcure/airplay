package swaiotos.runtime.h5.core.os.exts.payment;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;


import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.JSONObject;
import com.coocaa.smartsdk.pay.PayResultEvent;
import com.coocaa.smartsdk.pay.SubmitPayEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import swaiotos.runtime.h5.H5CoreExt;

/**
 * @ClassName: AccountExt
 * @Author: lu
 * @CreateDate: 11/18/20 2:33 PM
 * @Description:
 */
public class PaymentExt extends H5CoreExt {
    public static final String NAME = "payment";
    String TAG = "PaymentExt";

    private static H5CoreExt ext = null;

    public static synchronized H5CoreExt get(Context context) {
        if (ext == null) {
            ext = new PaymentExt();
        }
        return ext;
    }

    public PaymentExt() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void detach(Context context) {
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.detach(context);
    }

    @Subscribe(threadMode=ThreadMode.MAIN)
    public void onPayResultEvent(PayResultEvent event) {
        Log.d(TAG,"onPayResultEvent to js:"+event);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", String.valueOf(event.status));
        jsonObject.put("id", String.valueOf(event.id));
        native2js(event.id, RET_SUCCESS, JSON.toJSONString(jsonObject));
    }




    @JavascriptInterface
    public void startPay(String id,String json){
        Log.d(TAG,"startPay....json:"+json);
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        try{
            EventBus.getDefault().post(new SubmitPayEvent(0,id,json));
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG,"error:"+e.toString());
        }
    }
}
