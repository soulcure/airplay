package com.coocaa.tvpi.module.mine.viewmodel;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.smartscreen.data.banner.BannerHttpData;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.smartscreen.repository.http.home.HomeHttpMethod;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;
import com.coocaa.tvpi.module.homepager.adapter.bean.PlayMethodBean;
import com.coocaa.tvpi.module.homepager.adapter.bean.SmartScreenWrapBean;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;

import java.util.ArrayList;
import java.util.List;

public class MineFragmentViewModel extends BaseViewModel {
    private final MutableLiveData<List<PlayMethodBean>> playListLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<FunctionBean>> bannerListLiveData = new MutableLiveData<>();


    public LiveData<List<FunctionBean>> getDiscoverBanner() {
       // loadStateLiveData.setValue(LoadState.LOADING);

        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                BannerHttpData.FunctionContent bannerData = HomeHttpMethod.getInstance().getOperationData("play_banner",null,null);
                if (bannerData == null || bannerData.style != 1) {
                    return;
                }
                List<FunctionBean> bannerList  = bannerData.content;
                HomeUIThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        if(!bannerList.isEmpty()) {
                            bannerListLiveData.setValue(bannerList);
                        }
                    }
                });
            }
        });
        return bannerListLiveData;
    }

    public LiveData<List<PlayMethodBean>> getPlayList(boolean showLoading,String deviceType) {
        if(showLoading) {
            loadStateLiveData.setValue(LoadState.LOADING);
        }
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                List<PlayMethodBean> list;
                list = HomeHttpMethod.getInstance().getPanelContentList();
                HomeUIThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (list != null && !list.isEmpty()) {
                            if (showLoading) {
                                loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                            }
                            playListLiveData.setValue(list);
                        } else {
                            if(showLoading) {
                                loadStateLiveData.setValue(LoadState.LOAD_LIST_EMPTY);
                            }
                        }
                    }
                });
            }
        });
        return playListLiveData;
    }
}
