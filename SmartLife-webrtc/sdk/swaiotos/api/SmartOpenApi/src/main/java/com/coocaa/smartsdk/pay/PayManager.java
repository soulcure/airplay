package com.coocaa.smartsdk.pay;

import android.app.Activity;



public class PayManager {
    private static IPay iPay;
    public static void setiPay(IPay mPay){
        iPay= mPay;
    }




    public static void startPay(Activity activity, String id,String json) {
        if(iPay != null){
            iPay.startPay(activity,id, json);
        }
    }
}
