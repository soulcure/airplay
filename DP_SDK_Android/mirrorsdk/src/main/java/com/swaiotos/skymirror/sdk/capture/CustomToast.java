package com.swaiotos.skymirror.sdk.capture;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

class CustomToast {

    private static CustomToast instance;

    private Handler mHandler;

    private boolean isFirst;

    public static CustomToast instance() {
        if (instance == null) {
            instance = new CustomToast();
        }
        return instance;
    }


    private CustomToast() {
        mHandler = new Handler(Looper.getMainLooper());
        isFirst = false;
    }


    public void popUp(Context context) {
        if (!isFirst && Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            isFirst = true;
            startAct(context);
        }
    }


    public void clear() {
        isFirst = false;
    }

    private void startAct(final Context context) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context, DialogActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }


}
