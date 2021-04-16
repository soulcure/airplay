package com.coocaa.statemanager.view.manager;

import android.content.Context;

import com.coocaa.statemanager.view.countdown.TimeOutCallBack;


/**
 * @ Created on: 2020/10/21
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public interface IViewManager {
    void showNoDeviceDialog(Context context, TimeOutCallBack callBack);

    void cancelDeviceDialog();

    void dismissNoDeviceDialog();

    void showDisconnectDialog(Context context, String screenNum, String userNum, TimeOutCallBack callBack);

    void cancelDisconnectDialog();

    void dismissDisconnectDialog();

    void showUserGlobalWindow(Context context, String userNum);

    void updateUserGlobalWindow(String userNum);

    void dismissUserGlobalWindow();

    void showQrCodeGlobalWindow(Context context, String numStr, String qr);

    void dismissQrCodeGlobalWindow();

    void showBigQrCodeGlobalWindow(Context context, String numStr, String qr);

    void reStartShowBigQrCodeGlobalWindow(Context context, String numStr, String qr);

    void dismissBigQrCodeGlobalWindow();

    void updateQrCodeGlobalWindow(String numStr);

    void showLoadingDialog(Context context, String userNum, String screenNum);

    void dismissLoadingDialog();

    void showUserFinishDialog(Context context, String owner);

    void dismissUserFinishDialog();

    void showInterDisconnectDialog(Context context, TimeOutCallBack callBack);

    void cancelInterDisconnectDialog();

    void release();

}
