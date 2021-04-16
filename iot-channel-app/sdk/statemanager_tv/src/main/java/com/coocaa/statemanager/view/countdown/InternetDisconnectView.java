package com.coocaa.statemanager.view.countdown;

import android.content.Context;

/**
 * @ Created on: 2020/11/17
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public class InternetDisconnectView extends NoDeviceView {
    public InternetDisconnectView(Context context) {
        super(context);
        mBeforeCountView.setText("网络已断开，");
    }
}
