package com.coocaa.tvpi.module.app.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.data.BaseData;
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.data.app.TvAppListResp;
import com.coocaa.smartscreen.data.app.TvAppModel;
import com.coocaa.smartscreen.data.channel.events.GetInstallApkEvent;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.AppRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class AppTabTvViewModel extends BaseAppViewModel {
    private static final String TAG = AppTabTvViewModel.class.getSimpleName();
    private MutableLiveData<List<TvAppModel>> installedAppLiveData = new MutableLiveData<>();
    private int count;

    public AppTabTvViewModel() {
        Log.d(TAG, "AppTabTvViewModel: init");
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void getInstalledAppWithLoading() {
        loadStateLiveData.setValue(LoadState.LOADING);
        super.getInstalledApp();
    }


    //从电视获取到了已经安装的第三方apk
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GetInstallApkEvent getInstallApkEvent) {
        Log.d(TAG, "onEvent: GetInstallApkEvent" + getInstallApkEvent.result);
        TvAppListResp tvAppListResp = BaseData.load(getInstallApkEvent.result, TvAppListResp.class);
        List<TvAppModel> thirdAppList = new ArrayList<>();
        if (tvAppListResp != null && tvAppListResp.result != null) {
            for (TvAppModel tvAppModel : tvAppListResp.result) {
                if (!tvAppModel.isSystemApp) {
                    thirdAppList.add(tvAppModel);
                }
            }

            count = 0;
            for (TvAppModel tvAppModel : thirdAppList) {
                Repository.get(AppRepository.class)
                        .getAppDetail(tvAppModel.pkgName)
                        .setCallback(new BaseRepositoryCallback<AppModel>() {
                            @Override
                            public void onSuccess(AppModel appModel) {
                                Log.d(TAG, "onSuccess: " + appModel);
                                tvAppModel.coverUrl = appModel.appNewTvIcon;
                                count++;
                                if (count == thirdAppList.size()) {
                                    installedAppLiveData.setValue(thirdAppList);
                                    loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                                }
                            }

                            @Override
                            public void onFailed(Throwable e) {
                                Log.d(TAG, "onFailed: " + e.toString());
                                count++;
                                if (count == thirdAppList.size()) {
                                    installedAppLiveData.setValue(thirdAppList);
                                    loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                                }
                            }
                        });
            }
        }
    }

    public MutableLiveData<List<TvAppModel>> getInstalledAppLiveData() {
        return installedAppLiveData;
    }

    @Override
    protected void onCleared() {
        Log.d(TAG, "AppTabTvViewModel: onCleared ");
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
