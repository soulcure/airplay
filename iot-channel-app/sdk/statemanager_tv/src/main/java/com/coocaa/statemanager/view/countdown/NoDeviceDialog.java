package com.coocaa.statemanager.view.countdown;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.coocaa.statemanager.view.BaseDialog;


/**
 * @ Created on: 2020/10/22
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public class NoDeviceDialog extends BaseDialog {
    private static final String TAG = "NoDeviceDialog";
    private NoDeviceView mNoDeviceView;

    public NoDeviceDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    public View getLayoutId(Context context) {
        mNoDeviceView = new NoDeviceView(context);
        return mNoDeviceView;
    }

    @Override
    public void cancel() {
        try {
            mNoDeviceView.cancel();
            super.cancel();
        } catch (Exception e) {
            Log.i(TAG, "cancel: error = " + e.getMessage());
        }
    }


    public void show(TimeOutCallBack callBack) {
        if (isShowing()) {
            return;
        }
        mNoDeviceView.startNoDevice(callBack, new TimeOutCallBack() {
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
