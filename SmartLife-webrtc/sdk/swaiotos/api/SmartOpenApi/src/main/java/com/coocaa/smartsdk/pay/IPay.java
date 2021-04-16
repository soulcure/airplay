package com.coocaa.smartsdk.pay;

import android.app.Activity;

public interface IPay {
    void startPay(Activity activity, String id, String json);
}
