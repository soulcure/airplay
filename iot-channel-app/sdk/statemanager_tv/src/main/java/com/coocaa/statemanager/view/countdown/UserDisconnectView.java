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
import com.coocaa.statemanager.view.Util;

/**
 * @ Created on: 2020/4/3
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public class UserDisconnectView extends LinearLayout {
    private TextView mCountDownText;
    private TimeCount mCount;
    private TextView userNumText;
    public static final long TOTAL_TIME = 30000;
    public static final long INTERVAL = 1000;

    public UserDisconnectView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,  UiUtil.div(122)));
        setBackgroundResource(R.drawable.toast_bg);
        setOrientation(HORIZONTAL);

        TextView comeTextView = new TextView(getContext());
        comeTextView.setTextSize(UiUtil.dpi(32));
        comeTextView.setIncludeFontPadding(false);
        comeTextView.setTextColor(Color.parseColor("#0B0B0B"));
        comeTextView.setText("用户");
        comeTextView.setIncludeFontPadding(false);
        LayoutParams comeParams = new LayoutParams(UiUtil.div(64), UiUtil.div(32));
        comeParams.gravity = Gravity.CENTER_VERTICAL;
        addView(comeTextView, comeParams);

        userNumText = new TextView(getContext());
        LayoutParams userNumParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        userNumParams.gravity = Gravity.CENTER_VERTICAL;
        userNumParams.leftMargin = UiUtil.div(20);
        userNumText.setTextSize(UiUtil.dpi(32));
        userNumText.setTextColor(Color.parseColor("#FF5525"));
        userNumText.setIncludeFontPadding(false);
        addView(userNumText, userNumParams);

        TextView beforeCountView = new TextView(getContext());
        beforeCountView.setText("断开连接，");
        LayoutParams beforeCountParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        beforeCountParams.gravity = Gravity.CENTER_VERTICAL;
        beforeCountParams.leftMargin = UiUtil.div(20);
        beforeCountView.setLayoutParams(beforeCountParams);
        beforeCountView.setTextSize(UiUtil.dpi(32));
        beforeCountView.setTextColor(Color.parseColor("#0D0D0D"));
        beforeCountView.setIncludeFontPadding(false);
        addView(beforeCountView, beforeCountParams);


        mCountDownText = new TextView(getContext());
        mCountDownText.setTextColor(Color.parseColor("#0D0D0D"));
        LayoutParams countDownParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        countDownParams.gravity = Gravity.CENTER_VERTICAL;
        mCountDownText.setTextSize(UiUtil.dpi(32));
        mCountDownText.setIncludeFontPadding(false);
        addView(mCountDownText, countDownParams);
    }

    public void startDisconnect(String headUrl, String userNumStr, final TimeOutCallBack callBack, final TimeOutCallBack timeOutCallBack) {
        Log.i("xlj", "startDisconnect: headUrl = " + headUrl + "userNumStr = " + userNumStr);
        String hideNumStr = Util.hideMiddleNum(userNumStr);
        userNumText.setText("「" + hideNumStr + "」");
        mCount = new TimeCount(TOTAL_TIME, INTERVAL, new TimeCount.TimeCountCallBack() {
            @Override
            public void onTick(int second) {
                mCountDownText.setText(second + "s后返回首页");
            }

            @Override
            public void onFinish() {
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
