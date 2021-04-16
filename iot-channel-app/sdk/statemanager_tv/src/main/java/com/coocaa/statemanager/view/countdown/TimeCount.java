package com.coocaa.statemanager.view.countdown;

import android.os.CountDownTimer;
import android.util.Log;


public class TimeCount extends CountDownTimer {

    public interface TimeCountCallBack {
        void onTick(int second);

        void onFinish();
    }

    private static final String TAG = "TimeCount";
    private TimeCountCallBack mCallBack;


    public TimeCount(long millisInFuture, long countDownInterval, TimeCountCallBack callBack) {
        // 参数依次为总时长,和计时的时间间隔
        super(millisInFuture, countDownInterval);
        mCallBack = callBack;
    }

    @Override
    public void onFinish() {
        // 计时完毕时触发
        Log.i(TAG, "onFinish: ");
        if (mCallBack != null) {
            mCallBack.onFinish();
        }
    }

    @Override
    public void onTick(long millisUntilFinished) {
        // 计时过程显示
        if (mCallBack != null) {
            mCallBack.onTick((int) (millisUntilFinished / 1000));
        }
    }

}