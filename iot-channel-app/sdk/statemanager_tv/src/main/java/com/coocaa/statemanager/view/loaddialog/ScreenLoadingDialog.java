package com.coocaa.statemanager.view.loaddialog;

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
public class ScreenLoadingDialog extends BaseDialog {
    private LoadingView mLoadingView;

    public ScreenLoadingDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    public View getLayoutId(Context context) {
        mLoadingView = new LoadingView(getContext());
        return mLoadingView;
    }


    public void show(String headUrl, String owner) {
        Log.i("xlj", "show: headUrl = " + headUrl + " owner = " + owner);
        if (isShowing()) {
            mHandler.removeCallbacks(mRunnable);
            setLoadViewCondition(headUrl, owner);
            return;
        }
        setLoadViewCondition(headUrl, owner);
        super.show();
    }

    private void setLoadViewCondition(String headUrl, String owner) {
        mLoadingView.setUserText(headUrl, owner);
        mLoadingView.postDelayed(mRunnable, 3000);
    }
}
