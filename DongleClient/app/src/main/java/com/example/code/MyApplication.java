package com.example.code;

import android.app.Application;
import android.util.Log;

import swaiotos.channel.iot.SdkManager;
import swaiotos.channel.iot.callback.InitListener;


public class MyApplication extends Application {


    private static final String TAG = "login";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("yao", "Application onCreate pid=" + android.os.Process.myPid());

        SdkManager.instance().init(this, new InitListener() {
            @Override
            public void success() {
                Log.d("yao", "sdk init success");
            }

            @Override
            public void fail() {
                Log.e("yao", "sdk init fail");
            }
        });
    }


}
