package com.coocaa.statemanager.view.windowview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.coocaa.statemanager.common.bean.User;
import com.coocaa.statemanager.view.UiUtil;
import com.coocaa.statemanager.view.Util;

import swaiotos.channel.iot.utils.EmptyUtils;


/**
 * @ Created on: 2020/10/21
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public class UserGlobalWindow {
    private static final String TAG = "GlobalWindow";
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private TextView mScreenInfoText;


    public UserGlobalWindow(Context context) {
        init(context);
    }

    private void init(Context context) {
        mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.gravity = Gravity.TOP | Gravity.END;
        mLayoutParams.x = UiUtil.div(20);
        mLayoutParams.y = UiUtil.div(8);
        mLayoutParams.height = UiUtil.div(40);
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        mScreenInfoText = new TextView(context);
        mScreenInfoText.setTextSize(UiUtil.dpi(20));
        mScreenInfoText.setTextColor(Color.parseColor("#CCFFFFFF"));
    }

    public void showWindow(String userNum) {
        Log.i(TAG, "showWindow: userNum = " + userNum);
        User user = User.decode(userNum);
        if(EmptyUtils.isNotEmpty(user)){
            if(EmptyUtils.isNotEmpty(user.nickName)){
                mScreenInfoText.setText("「"  + user.nickName + "」" + " " + "正在共享");
            }else{
                String hideNumText = Util.hideMiddleNum(userNum);
                mScreenInfoText.setText("「" + "用户" + " " + hideNumText + "」" + " " + "正在共享");
            }
        }

        try {
            mWindowManager.addView(mScreenInfoText, mLayoutParams);
        } catch (Exception e) {
            Log.e(TAG, "showWindow: error = " + e.getMessage());
        }
    }

    public void updateWindow(String userNum) {
        Log.i(TAG, "updateWindow: userNum = " + userNum);
        String hideNumText = Util.hideMiddleNum(userNum);
        try {
            mScreenInfoText.setText("「" + "用户" + hideNumText + "」" + " " + "正在共享");
        } catch (Exception e) {
            Log.e(TAG, "updateWindow: error = " + e.getMessage());
        }
    }

    public void dimissWindow() {
        Log.i(TAG, "dimissWindow: ");
        try {
            mWindowManager.removeView(mScreenInfoText);
        } catch (Exception e) {
            Log.e(TAG, "dimissWindow: error = " + e.getMessage());
        }
    }
}
