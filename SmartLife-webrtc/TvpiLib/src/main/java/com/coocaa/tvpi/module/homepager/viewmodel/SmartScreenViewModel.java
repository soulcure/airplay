package com.coocaa.tvpi.module.homepager.viewmodel;

import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.account.TpTokenInfo;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.smartscreen.network.exception.ApiException;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.http.home.HomeHttpMethod;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;
import com.coocaa.tvpi.module.homepager.adapter.bean.SmartScreenWrapBean;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.login.provider.ProviderClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class SmartScreenViewModel extends BaseViewModel {
    private static final String TAG = SmartScreenViewModel.class.getSimpleName();

    private final MutableLiveData<List<SmartScreenWrapBean>> smartScreenListLiveData = new MutableLiveData<>();
    private List<FunctionBean> bannerList;

    public LiveData<List<SmartScreenWrapBean>> getSmartScreenList(boolean showLoading,String deviceType) {
        if(showLoading) {
            loadStateLiveData.setValue(LoadState.LOADING);
        }
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                List<SmartScreenWrapBean> wrapBeanList = new ArrayList<>();
                bannerList = HomeHttpMethod.getInstance().getBannerList();
                if (bannerList == null) {
                    bannerList = new ArrayList<>();
                }
                //default
                if (bannerList.isEmpty()) {
                    FunctionBean localFunctionBean = new FunctionBean();
                    localFunctionBean.icon = "https://files-sit.skyworthiot.com/images/dot-operation/mobile_banner/默认.png";
                    bannerList.add(localFunctionBean);
                }
                if (bannerList != null && !bannerList.isEmpty()) {
                    SmartScreenWrapBean bannerWrapBean = new SmartScreenWrapBean();
                    bannerWrapBean.setBannerList(bannerList);
                    wrapBeanList.add(bannerWrapBean);
                }

                List<FunctionBean> functionBeanList = HomeHttpMethod.getInstance().getFunctionList(deviceType);
                if (functionBeanList != null && !functionBeanList.isEmpty()) {
                    SmartScreenWrapBean functionWrapBean = new SmartScreenWrapBean();
                    functionWrapBean.setFunctionBeanList(functionBeanList);
                    wrapBeanList.add(functionWrapBean);
                }

                HomeUIThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        //金刚区数据不为空才刷新首页
                        if (!wrapBeanList.isEmpty()
                                && bannerList != null && !bannerList.isEmpty()
                                && functionBeanList != null && !functionBeanList.isEmpty()) {
                            smartScreenListLiveData.setValue(wrapBeanList);
                            loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                        }else {
                            loadStateLiveData.setValue(LoadState.LOAD_LIST_EMPTY);
                        }
                    }
                });
            }
        });

        return smartScreenListLiveData;
    }

    public void getTpToken() {
        if (!UserInfoCenter.getInstance().isLogin()) {
            return;
        }
        Repository.get(LoginRepository.class)
                .getTpToken(UserInfoCenter.getInstance().getAccessToken())
                .setCallback(new BaseRepositoryCallback<TpTokenInfo>(ApiException.AUTH_TOKEN_EXPIRED_ACCOUNT) {
                    @Override
                    public void onSuccess(TpTokenInfo tpTokenInfo) {
                        super.onSuccess(tpTokenInfo);
                        Log.d(TAG, "onSuccess: " + tpTokenInfo.tp_token);
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                    }
                });
    }

    public void updateUserInfo() {
        if (!UserInfoCenter.getInstance().isLogin()) {
            return;
        }
        String accessToken = UserInfoCenter.getInstance().getAccessToken();
        Repository.get(LoginRepository.class)
                .getCoocaaUserInfo(accessToken)
                .setCallback(new BaseRepositoryCallback<CoocaaUserInfo>(ApiException.AUTH_TOKEN_EXPIRED_ACCOUNT) {
                    @Override
                    public void onSuccess(CoocaaUserInfo coocaaUserInfo) {
                        super.onSuccess(coocaaUserInfo);
                        Log.d(TAG, "syncLoginData: accessToken = " + accessToken);
                        Log.d(TAG, "syncLoginData: userInfo = " + coocaaUserInfo);
                        if (TextUtils.isEmpty(coocaaUserInfo.access_token) || "null".equalsIgnoreCase(coocaaUserInfo.access_token)) {
                            coocaaUserInfo.access_token = accessToken;
                        }
                        ProviderClient.getClient().saveInfo(accessToken, new Gson().toJson(coocaaUserInfo));
                        Repository.get(LoginRepository.class).saveCoocaaUserInfo(coocaaUserInfo);
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                    }
                });
    }


}
