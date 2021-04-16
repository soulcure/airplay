package com.coocaa.tvpi.module.homepager.main.vy21m4;

import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.smartscreen.data.BaseData;
import com.coocaa.smartscreen.data.BaseResp;
import com.coocaa.smartscreen.data.banner.BannerHttpData;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.smartscreen.data.function.homepage.SSHomePageData;
import com.coocaa.smartscreen.data.function.homepage.SSHomePageResp;
import com.coocaa.smartscreen.repository.http.home.HomeHttpMethod;
import com.coocaa.smartscreen.utils.AndroidUtil;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;
import com.coocaa.tvpi.module.homepager.adapter.bean.SmartScreenWrapBean;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.login.UserInfoCenter;

import java.util.ArrayList;
import java.util.List;

public class SmartScreenViewModel extends BaseViewModel {
    private static final String TAG = SmartScreenViewModel.class.getSimpleName();

    private final MutableLiveData<BannerHttpData.FunctionContent> bannerLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<SSHomePageData>> ssHomePageLiveData = new MutableLiveData<>();

    public LiveData<BannerHttpData.FunctionContent> getBannerData(boolean showLoading, String deviceType) {
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

                HomeUIThread.execute(new Runnable() {
                    @Override
                    public void run() {

                        if (bannerData != null) {
                            bannerLiveData.setValue(bannerData);
                        }
                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                    }
                });
            }
        });

        return bannerLiveData;
    }

    public LiveData<List<SSHomePageData>> getFunctionListAppAreaV2(boolean showLoading, String deviceType) {
        if (showLoading) {
            loadStateLiveData.setValue(LoadState.LOADING);
        }
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                String uid = null;
                String ak = null;
                if (UserInfoCenter.getInstance().getCoocaaUserInfo() != null) {
                    uid = UserInfoCenter.getInstance().getCoocaaUserInfo().open_id;
                    ak = UserInfoCenter.getInstance().getCoocaaUserInfo().access_token;
                }

                List<SSHomePageData> ssHomePageDataList = HomeHttpMethod.getInstance().getFunctionListAppAreaV2(ak, uid);

                HomeUIThread.execute(new Runnable() {
                    @Override
                    public void run() {

                        if (ssHomePageDataList != null) {
                            ssHomePageLiveData.setValue(ssHomePageDataList);
                        }
                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                    }
                });
            }
        });

        return ssHomePageLiveData;
    }
}