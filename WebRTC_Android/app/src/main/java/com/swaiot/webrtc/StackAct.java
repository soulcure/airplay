package com.swaiot.webrtc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.TextUtils;

import com.swaiot.webrtc.ui.WebRTCActivity;

public class StackAct {


    @SuppressLint("StaticFieldLeak")
    private static StackAct instance;

    private String mSid;
    private Activity mActivity;

    private StackAct() {
    }


    public static StackAct instance() {
        if (instance == null) {
            instance = new StackAct();
        }
        return instance;
    }


    public synchronized void addActivity(String sid, Activity activity) {
        mSid = sid;
        mActivity = activity;
    }


    public synchronized boolean finishActivity(String sid) {

        if (!TextUtils.isEmpty(mSid) && !mSid.equals(sid)) {
            if (mActivity instanceof WebRTCActivity) {
                ((WebRTCActivity)mActivity).unRegisterEventBus();
            }
            mActivity.finish();
            return true;
        }
        return false;

    }


}
