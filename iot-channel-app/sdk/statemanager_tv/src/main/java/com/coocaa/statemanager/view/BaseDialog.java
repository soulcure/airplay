package com.coocaa.statemanager.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.coocaa.statemanager.R;


/**
 * @ Created on: 2020/10/21
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public abstract class BaseDialog extends Dialog {
    private Window mWindow;
    protected Handler mHandler = new Handler(Looper.getMainLooper());

    public BaseDialog(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getLayoutId(context));
        init();
    }

    private void init() {
        mWindow = getWindow();
        WindowManager.LayoutParams wl = mWindow.getAttributes();
        wl.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        wl.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wl.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wl.gravity = Gravity.CENTER_HORIZONTAL;
        wl.dimAmount = 0f;
        mWindow.setGravity(Gravity.TOP);
        mWindow.setWindowAnimations(R.style.dialogWindowAnim);
        mWindow.setBackgroundDrawableResource(android.R.color.transparent);
        mWindow.setAttributes(wl);
    }


    public abstract View getLayoutId(Context context);

    protected Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i("xlj1030", "close  dialog");
            if (isShowing()) {
                dismiss();
            }
        }
    };
}
