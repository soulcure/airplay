package com.coocaa.statemanager.view.loaddialog;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coocaa.statemanager.R;
import com.coocaa.statemanager.common.bean.User;
import com.coocaa.statemanager.view.UiUtil;
import com.coocaa.statemanager.view.Util;

import swaiotos.channel.iot.utils.EmptyUtils;


/**
 * @ Created on: 2020/10/21
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public class LoadingView extends LinearLayout {
    private TextView mNameView;

    public LoadingView(@NonNull Context context) {
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
        comeTextView.setText("来自");
        LayoutParams comeParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, UiUtil.div(32));
        comeParams.gravity = Gravity.CENTER_VERTICAL;
        addView(comeTextView, comeParams);

        mNameView = new TextView(getContext());
        LayoutParams nameParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nameParams.gravity = Gravity.CENTER_VERTICAL;
        nameParams.leftMargin = UiUtil.div(20);
        mNameView.setTextSize(UiUtil.dpi(32));
        mNameView.setTextColor(Color.parseColor("#FF5525"));
        addView(mNameView, nameParams);

        TextView endTextView = new TextView(getContext());
        endTextView.setTextSize(UiUtil.dpi(32));
        endTextView.setTextColor(Color.parseColor("#0D0D0D"));
        endTextView.setText("的共享");
        LayoutParams endTextParams = new LayoutParams(UiUtil.div(96), ViewGroup.LayoutParams.WRAP_CONTENT);
        endTextParams.gravity = Gravity.CENTER_VERTICAL;
        endTextParams.leftMargin = UiUtil.div(20);
        addView(endTextView, endTextParams);
    }


    public void setUserText(String url, String owner) {
        User user = User.decode(owner);
        String content = "";
        if(EmptyUtils.isNotEmpty(user)){
            if(EmptyUtils.isNotEmpty(user.nickName)){
                content = user.nickName;
            }else{
                content = Util.hideMiddleNum(user.mobile);
            }
            mNameView.setText("「" + content + "」");
        }
    }
}
