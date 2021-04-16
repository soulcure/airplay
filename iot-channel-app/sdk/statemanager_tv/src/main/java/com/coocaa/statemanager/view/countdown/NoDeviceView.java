package com.coocaa.statemanager.view.countdown;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coocaa.statemanager.R;
import com.coocaa.statemanager.view.UiUtil;

import static com.coocaa.statemanager.view.countdown.UserDisconnectView.INTERVAL;
import static com.coocaa.statemanager.view.countdown.UserDisconnectView.TOTAL_TIME;

/**
 * @ Created on: 2020/10/22
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public class NoDeviceView extends LinearLayout {
    private TextView mCountDownText;
    private TimeCount mCount;
    public TextView mBeforeCountView;

    public NoDeviceView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,  UiUtil.div(122)));
        setBackgroundResource(R.drawable.toast_bg);
        setOrientation(HORIZONTAL);


        mBeforeCountView = new TextView(getContext());
        mBeforeCountView.setText("当前未连接设备，");
        LayoutParams beforeCountParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        beforeCountParams.gravity = Gravity.CENTER_VERTICAL;
        mBeforeCountView.setLayoutParams(beforeCountParams);
        mBeforeCountView.setTextSize(UiUtil.dpi(32));
        mBeforeCountView.setTextColor(Color.parseColor("#111111"));
        mBeforeCountView.setIncludeFontPadding(false);
        addView(mBeforeCountView, beforeCountParams);


        mCountDownText = new TextView(getContext());
        mCountDownText.setTextColor(Color.parseColor("#0D0D0D"));
        LayoutParams countDownParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        countDownParams.gravity = Gravity.CENTER_VERTICAL;
        mCountDownText.setTextSize(UiUtil.dpi(32));
        mCountDownText.setIncludeFontPadding(false);

        addView(mCountDownText, countDownParams);
    }

    public void startNoDevice(final TimeOutCallBack callBack, final TimeOutCallBack timeOutCallBack) {
        Log.i("xlj", "startNoDevice: ");
        mCount = new TimeCount(TOTAL_TIME, INTERVAL, new TimeCount.TimeCountCallBack() {
            @Override
            public void onTick(int second) {
                mCountDownText.setText(second + "s后返回首页");
            }

            @Override
            public void onFinish() {
                Log.i("xlj1030", "onFinish: ");
                timeOutCallBack.onFinish();
                callBack.onFinish();
            }
        });
        mCount.start();
    }

    public void cancel() {
        mCount.cancel();
    }
}
