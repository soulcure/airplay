package com.coocaa.statemanager.view.countdown;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.coocaa.statemanager.view.BaseDialog;

/**
 * @ Created on: 2020/11/17
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public class InternetDisconnectDialog extends BaseDialog {
    private static final String TAG = "InternetDisconnect";
    private InternetDisconnectView mInternetDisconnectView;

    public InternetDisconnectDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    public View getLayoutId(Context context) {
        mInternetDisconnectView = new InternetDisconnectView(context);
        return mInternetDisconnectView;
    }

    @Override
    public void cancel() {
        try {
            mInternetDisconnectView.cancel();
            super.cancel();
        } catch (Exception e) {
            Log.i(TAG, "cancel: error = " + e.getMessage());
        }
    }


    public void show(TimeOutCallBack callBack) {
        if (isShowing()) {
            return;
        }
        mInternetDisconnectView.startNoDevice(callBack, new TimeOutCallBack() {
            @Override
            public void onFinish() {
                if (isShowing()) {
                    Log.i(TAG, "dismiss");
                    dismiss();
                }
            }
        });
        super.show();
        Log.i(TAG, "show: ");
    }
}
