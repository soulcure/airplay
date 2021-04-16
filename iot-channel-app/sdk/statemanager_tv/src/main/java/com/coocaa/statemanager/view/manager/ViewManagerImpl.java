package com.coocaa.statemanager.view.manager;

import android.content.Context;
import android.util.Log;

import com.coocaa.statemanager.common.utils.SystemInfoUtil;
import com.coocaa.statemanager.view.countdown.InternetDisconnectDialog;
import com.coocaa.statemanager.view.countdown.NoDeviceDialog;
import com.coocaa.statemanager.view.countdown.TimeOutCallBack;
import com.coocaa.statemanager.view.countdown.UserDisconnectDialog;
import com.coocaa.statemanager.view.loaddialog.ScreenLoadingDialog;
import com.coocaa.statemanager.view.loaddialog.UserFinishDialog;
import com.coocaa.statemanager.view.windowview.BigQRCodeGlobalWindow;
import com.coocaa.statemanager.view.windowview.ConnectCodeGlobalWindow;
import com.coocaa.statemanager.view.windowview.QRCodeGlobalWindow;
import com.coocaa.statemanager.view.windowview.UserGlobalWindow;

import swaiotos.channel.iot.utils.EmptyUtils;


/**
 * @ Created on: 2020/10/21
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public class ViewManagerImpl implements IViewManager {
    private static final String TAG = "ViewManagerImpl";

    private volatile static ViewManagerImpl singleton;
    private NoDeviceDialog mNoDeviceDialog;
    private UserDisconnectDialog mUserDisconnectDialog;
    private UserGlobalWindow mUserGlobalWindow;
    private ScreenLoadingDialog mScreenLoadingDialog;
    private QRCodeGlobalWindow mQrCodeGlobalWindow;
    private ConnectCodeGlobalWindow mConnectCodeGlobalWindow;
    private BigQRCodeGlobalWindow mBigQrCodeGlobalWindow;
    private UserFinishDialog mUserFinishDialog;
    private InternetDisconnectDialog mInternetDisconnectDialog;

    private ViewManagerImpl() {

    }

    public static ViewManagerImpl getSingleton() {
        if (singleton == null) {
            synchronized (ViewManagerImpl.class) {
                if (singleton == null) {
                    singleton = new ViewManagerImpl();
                }
            }
        }
        return singleton;
    }

    @Override
    public void showNoDeviceDialog(Context context, TimeOutCallBack callBack) {
        Log.i(TAG, "showNoDeviceDialog: ");
        if (mNoDeviceDialog == null) {
            mNoDeviceDialog = new NoDeviceDialog(context);
        }
        if (!mNoDeviceDialog.isShowing()) {
            mNoDeviceDialog.show(callBack);
        }
    }

    @Override
    public void cancelDeviceDialog() {
        Log.i(TAG, "cancelDeviceDialog: ");
        if (mNoDeviceDialog != null && mNoDeviceDialog.isShowing()) {
            mNoDeviceDialog.cancel();
        } else {
            Log.i(TAG, "cancelDeviceDialog is null");
        }
    }


    @Override
    public void dismissNoDeviceDialog() {
        Log.i(TAG, "dismissNoDeviceDialog: ");
        if (mNoDeviceDialog != null) {
            mNoDeviceDialog.dismiss();
        } else {
            Log.i(TAG, "dismissNoDeviceDialog: is null");
        }
    }

    @Override
    public void showDisconnectDialog(Context context, String headUrl, String userNumStr, TimeOutCallBack callBack) {
        Log.i(TAG, "showDisconnectDialog: ");
        if (mUserDisconnectDialog == null) {
            mUserDisconnectDialog = new UserDisconnectDialog(context);
        }
        mUserDisconnectDialog.show(headUrl, userNumStr, callBack);
    }

    @Override
    public void cancelDisconnectDialog() {
        Log.i(TAG, "cancelDisconnectDialog: ");
        if (mUserDisconnectDialog != null) {
            mUserDisconnectDialog.cancel();
        } else {
            Log.i(TAG, "cancelDisconnectDialog: is null");
        }
    }


    @Override
    public void dismissDisconnectDialog() {
        Log.i(TAG, "dismissDisconnectDialog: ");
        if (mUserDisconnectDialog != null) {
            mUserDisconnectDialog.dismiss();
        } else {
            Log.i(TAG, "dismissDisconnectDialog: is null");
        }
    }

    @Override
    public void showUserGlobalWindow(Context context, String owner) {
        Log.i(TAG, "showUserGlobalWindow: ");
//产品需求去掉此功能(dangle右上角：xxx正在投屏)
//        if (mUserGlobalWindow == null) {
//            mUserGlobalWindow = new UserGlobalWindow(context);
//        }
//        mUserGlobalWindow.showWindow(owner);
    }

    @Override
    public void updateUserGlobalWindow(String userNum) {
        Log.i(TAG, "updateUserGlobalWindow: ");
//产品需求去掉此功能(dangle右上角：xxx正在投屏)
//        if (mUserGlobalWindow != null) {
//            mUserGlobalWindow.updateWindow(userNum);
//        } else {
//            Log.i(TAG, "updateUserGlobalWindow: is null");
//        }
    }

    @Override
    public void dismissUserGlobalWindow() {
        Log.i(TAG, "dismissUserGlobalWindow: ");
//产品需求去掉此功能(dangle右上角：xxx正在投屏)
//        if (mUserGlobalWindow != null) {
//            mUserGlobalWindow.dimissWindow();
//        } else {
//            Log.i(TAG, "dismissUserGlobalWindow: is null");
//        }
    }

    @Override
    public void showQrCodeGlobalWindow(Context context, String numStr, String qr) {
        Log.i(TAG, "showQrCodeGlobalWindow: ");
        //Dangle业务才展示连屏码
        if(!SystemInfoUtil.isDangle())
            return;
        //没有连接码就不显示
        if(EmptyUtils.isEmpty(numStr)){
            return;
        }
        if (mConnectCodeGlobalWindow == null) {
            mConnectCodeGlobalWindow = new ConnectCodeGlobalWindow(context);
        }
        mConnectCodeGlobalWindow.showWindow(numStr, qr);
    }

    @Override
    public void dismissQrCodeGlobalWindow() {
        Log.i(TAG, "dismissQrCodeGlobalWindow: ");
        if (mConnectCodeGlobalWindow != null) {
            mConnectCodeGlobalWindow.dismissWindow();
        } else {
            Log.i(TAG, "dismissQrCodeGlobalWindow: is null");
        }
    }

    @Override
    public void showBigQrCodeGlobalWindow(Context context, String numStr, String qr) {
        Log.i(TAG, "showBigQrCodeGlobalWindow: ");
        if (mBigQrCodeGlobalWindow == null) {
            mBigQrCodeGlobalWindow = new BigQRCodeGlobalWindow(context);
        }
        mBigQrCodeGlobalWindow.showWindow(numStr, qr);
    }

    @Override
    public void reStartShowBigQrCodeGlobalWindow(Context context, String numStr, String qr) {
        if (mBigQrCodeGlobalWindow == null) {
            mBigQrCodeGlobalWindow = new BigQRCodeGlobalWindow(context);
        }
        mBigQrCodeGlobalWindow.restartShowWindow(numStr, qr);
    }

    @Override
    public void dismissBigQrCodeGlobalWindow() {
        Log.i(TAG, "dismissBigQrCodeGlobalWindow: ");
        if (mBigQrCodeGlobalWindow != null) {
            mBigQrCodeGlobalWindow.dismissWindow();
        } else {
            Log.i(TAG, "dismissBigQrCodeGlobalWindow: is null");
        }
    }

    @Override
    public void updateQrCodeGlobalWindow(String numStr) {
        Log.i(TAG, "updateQrCodeGlobalWindow: ");
        if (mQrCodeGlobalWindow != null) {
            mQrCodeGlobalWindow.updateWinodow(numStr);
        } else {
            Log.i(TAG, "updateQrCodeGlobalWindow: is null");
        }
    }

    @Override
    public void showLoadingDialog(Context context, String headUrl, String owner) {
        Log.i(TAG, "showLoadingDialog: ");
        if (mScreenLoadingDialog == null) {
            mScreenLoadingDialog = new ScreenLoadingDialog(context);
        }
        mScreenLoadingDialog.show(headUrl, owner);
    }

    @Override
    public void dismissLoadingDialog() {
        Log.i(TAG, "dismissLoadingDialog: ");
        if (mScreenLoadingDialog != null) {
            mScreenLoadingDialog.dismiss();
        } else {
            Log.i(TAG, "dismissLoadingDialog: is null");
        }
    }

    @Override
    public void showUserFinishDialog(Context context, String owner) {
        Log.i(TAG, "showUserFinishDialog: ");
        if (mUserFinishDialog == null) {
            mUserFinishDialog = new UserFinishDialog(context);
        }
        mUserFinishDialog.show(owner);
    }

    @Override
    public void dismissUserFinishDialog() {
        Log.i(TAG, "dismissUserFinishDialog: ");
        if (mUserFinishDialog != null) {
            mUserFinishDialog.dismiss();
        } else {
            Log.i(TAG, "dismissUserFinishDialog: is null");
        }
    }

    @Override
    public void showInterDisconnectDialog(Context context, TimeOutCallBack callBack) {
        Log.i(TAG, "showInterDisconnectDialog: ");
        if (mInternetDisconnectDialog == null) {
            mInternetDisconnectDialog = new InternetDisconnectDialog(context);
        }
        if (!mInternetDisconnectDialog.isShowing()) {
            mInternetDisconnectDialog.show(callBack);
        }
    }

    @Override
    public void cancelInterDisconnectDialog() {
        Log.i(TAG, "cancelInterDisconnectDialog: ");
        if (mInternetDisconnectDialog != null) {
            mInternetDisconnectDialog.cancel();
        }
    }

    @Override
    public void release() {
        mScreenLoadingDialog = null;
        mUserGlobalWindow = null;
        mUserDisconnectDialog = null;
        mNoDeviceDialog = null;
        mUserFinishDialog = null;
    }
}
