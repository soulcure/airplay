package com.coocaa.statemanager.view.countdown;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.coocaa.statemanager.view.BaseDialog;


/**
 * @ Created on: 2020/10/21
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public class UserDisconnectDialog extends BaseDialog {
    private static final String TAG = "UserDisconnectDialog";
    private UserDisconnectView mCountDownView;

    public UserDisconnectDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    public View getLayoutId(Context context) {
        mCountDownView = new UserDisconnectView(context);
        return mCountDownView;
    }

    public void show(String headUrl, String useNumStr, TimeOutCallBack callBack) {
        if (isShowing()) {
            return;
        }
        super.show();
        mCountDownView.startDisconnect(headUrl, useNumStr, callBack, new TimeOutCallBack() {
            @Override
            public void onFinish() {
                if (isShowing()) {
                    Log.i(TAG, "onFinish: dismiss");
                    dismiss();
                }
            }
        });
    }


    @Override
    public void cancel() {
        try {
            mCountDownView.cancel();
            super.cancel();
        } catch (Exception e) {
            Log.i("xlj", "cancel: error = " + e.getMessage());
        }
    }

}
