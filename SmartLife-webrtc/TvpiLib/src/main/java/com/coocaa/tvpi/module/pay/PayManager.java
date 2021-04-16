package com.coocaa.tvpi.module.pay;



import android.util.Log;

import com.coocaa.tvpi.module.pay.api.IPayResultCallback;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PayManager {
    private static final List<IPayResultCallback> listener =
            new CopyOnWriteArrayList<>();

    private PayManager() {
    }

    private static class Holer {
        private static PayManager INSTANCE = new PayManager();
    }

    public static PayManager getInstance() {
        return Holer.INSTANCE;
    }

    public void addCallback(IPayResultCallback callback) {
        Log.d("PaymentExt","callback:"+callback);
        if (!listener.contains(callback) &&callback != null) {
            listener.add(callback);
        }
    }

    public void removeCallback(IPayResultCallback callback) {
        listener.remove(callback);
    }

    public void notifyResult(boolean succ,String reason){
        Log.d("PaymentExt","COME IN .......NOTIFY   listener:"+listener);
        for (IPayResultCallback callback : listener) {
            if (succ)
            {
                callback.paySuccessed();
            }else{
                if("6001".equals(reason)){
                    callback.payCancel();
                }else{
                    callback.payFailed(reason);
                }
            }
        }
    }

    public void notifyWxResult(String reason){
        Log.d("PaymentExt","COME IN .......NOTIFY   listener:"+listener);
        for (IPayResultCallback callback : listener) {
            Log.d("PaymentExt","notify callback:"+callback);
            if ("0".equals(reason))
            {
                callback.paySuccessed();
            }else{
                if("-2".equals(reason)){
                    callback.payCancel();
                }else{
                    callback.payFailed(reason);
                }

            }
        }
    }

    public static List<IPayResultCallback> getListeners() {
        return listener;
    }

    public int size(){
        return listener.size();
    }
}
