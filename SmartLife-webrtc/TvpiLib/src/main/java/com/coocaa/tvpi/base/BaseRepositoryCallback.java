package com.coocaa.tvpi.base;

import android.util.Log;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.network.exception.ApiException;
import com.coocaa.smartscreen.repository.callback.RepositoryCallback;
import com.coocaa.tvpi.util.LogoutHelp;

/**
 * 数据回调接口基类
 * 1.增加token过期校验支持，需要指定token过期值{@link #BaseRepositoryCallback(int)} ()}.
 * 2.增加异常土司提示
 * Created by songxing on 2020/7/1
 */
public class BaseRepositoryCallback<T> implements RepositoryCallback<T> {
    private static final String TAG = BaseRepositoryCallback.class.getSimpleName();
    private int assignTokenExpiredCode = -1;

    public BaseRepositoryCallback() {
    }

    //需要校验token过期的接口传入token过期的值，不指定不进行token过期校验
    public BaseRepositoryCallback(int assignTokenExpiredCode) {
        this.assignTokenExpiredCode = assignTokenExpiredCode;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onSuccess(T t) {
        Log.d(TAG, "onSuccess: t->" + t);
    }

    @Override
    public void onFailed(Throwable e) {
        Log.e(TAG, "onFailed: e->" + e.getMessage());

        //token失效
        if (assignTokenExpiredCode != -1) {
            if (e instanceof ApiException && ((ApiException) e).getCode() == assignTokenExpiredCode) {
                LogoutHelp.LogoutAndReLogin();
            }
        }

        //这里直接显示出对应异常信息即可
        ToastUtils.getInstance().showGlobalLong(e.getMessage());
    }
}
