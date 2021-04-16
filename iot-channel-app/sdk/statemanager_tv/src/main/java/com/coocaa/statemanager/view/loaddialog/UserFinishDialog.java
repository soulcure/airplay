package com.coocaa.statemanager.view.loaddialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.coocaa.statemanager.view.BaseDialog;

/**
 * @ Created on: 2020/11/1
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public class UserFinishDialog extends BaseDialog {
    private static final String TAG = "UserFinishDialog";
    private UserFinishView mUserFinishView;

    public UserFinishDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    public View getLayoutId(Context context) {
        mUserFinishView = new UserFinishView(context);
        return mUserFinishView;
    }

    public void show(String owner) {
        Log.i(TAG, "show: useNameStr = " + owner);
        if (isShowing()) {
            mHandler.removeCallbacks(mRunnable);
            setLoadViewCondition(owner);
            return;
        }
        setLoadViewCondition(owner);
        super.show();
    }

    private void setLoadViewCondition(String useNameStr) {
        mUserFinishView.setUserText(useNameStr);
        mUserFinishView.postDelayed(mRunnable, 3000);
    }
}
