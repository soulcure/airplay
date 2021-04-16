package com.coocaa.tvpi.module.homepager.main.vy21m4;

import android.text.TextUtils;

import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.smartscreen.data.banner.BannerHttpData;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.smartscreen.repository.http.home.HomeHttpMethod;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;
import com.coocaa.tvpi.module.homepager.adapter.bean.SmartScreenWrapBean;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.login.UserInfoCenter;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SharedSpaceModel extends BaseViewModel {
    private static final String TAG = SharedSpaceModel.class.getSimpleName();

    private final MutableLiveData<List<FunctionBean>> functionListLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<SmartScreenWrapBean>> smartScreenListLiveData = new MutableLiveData<>();

    public LiveData<List<SmartScreenWrapBean>> getSmartScreenList(boolean showLoading, String deviceType) {
        if (showLoading) {
            loadStateLiveData.setValue(LoadState.LOADING);
        }
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                List<SmartScreenWrapBean> wrapBeanList = new ArrayList<>();
                List<FunctionBean> bannerList = null;
                String uid = null;
                String ak = null;
                if (UserInfoCenter.getInstance().getCoocaaUserInfo() != null) {
                    uid = UserInfoCenter.getInstance().getCoocaaUserInfo().open_id;
                    ak = UserInfoCenter.getInstance().getCoocaaUserInfo().access_token;
                }
                BannerHttpData.FunctionContent bannerData = HomeHttpMethod.getInstance().getOperationData("home_banner", ak, uid);
                if (bannerData != null && bannerData.style != 0) {
                    bannerList = bannerData.content;
                }
                if (bannerList == null) {
                    bannerList = new ArrayList<>();
                }
                if (!bannerList.isEmpty()) {
                    SmartScreenWrapBean bannerWrapBean = new SmartScreenWrapBean();
                    bannerWrapBean.setBannerList(bannerList);
                    if (!TextUtils.isEmpty(bannerData.bg)) {
                        bannerWrapBean.setBg(bannerData.bg);
                    }
                    if (!TextUtils.isEmpty(bannerData.theme)) {
                        bannerWrapBean.setTheme(bannerData.theme);
                    }
                    bannerWrapBean.setStyle(bannerData.style);
                    wrapBeanList.add(bannerWrapBean);
                }

                List<FunctionBean> functionBeanList;
                if (SmartConstans.isTestServer()) {
                    functionBeanList = HomeHttpMethod.getInstance().getFunctionList(ak, uid);
                } else {
                    functionBeanList = HomeHttpMethod.getInstance().getFunctionList(deviceType);
                }

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
                                && functionBeanList != null && !functionBeanList.isEmpty()) {
                            smartScreenListLiveData.setValue(wrapBeanList);
                            loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                        } else {
                            loadStateLiveData.setValue(LoadState.LOAD_LIST_EMPTY);
                        }
                    }
                });
            }
        });

        return smartScreenListLiveData;
    }
}