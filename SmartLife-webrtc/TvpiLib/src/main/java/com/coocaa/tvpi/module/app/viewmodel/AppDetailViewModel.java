package com.coocaa.tvpi.module.app.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.data.app.AppRecommendData;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.AppRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.module.app.bean.AppDetailWrapBean;

import java.util.ArrayList;
import java.util.List;

public class AppDetailViewModel extends BaseAppViewModel {
    private static final String TAG = AppDetailViewModel.class.getSimpleName();
    private MutableLiveData<List<AppDetailWrapBean>> appDetailLiveData = new MutableLiveData<>();

    public AppDetailViewModel() {
        super(TAG);
        Log.d(TAG, "AppDetailViewModel: init");
    }

    public LiveData<List<AppDetailWrapBean>> getAppDetail(AppModel appModel) {
        loadStateLiveData.setValue(LoadState.LOADING);
        List<AppDetailWrapBean> result = new ArrayList<>();
        Repository.get(AppRepository.class)
                .getAppDetail(appModel.pkg)
                .setCallback(new BaseRepositoryCallback<AppModel>() {
                    @Override
                    public void onSuccess(AppModel appModel) {
                        AppDetailWrapBean detailWrapBean = new AppDetailWrapBean();
                        detailWrapBean.setAppModel(appModel);
                        result.add(detailWrapBean);

                        Repository.get(AppRepository.class)
                                .getAppRecommendList(appModel.appId)
                                .setCallback(new BaseRepositoryCallback<List<AppRecommendData>>() {
                                    @Override
                                    public void onSuccess(List<AppRecommendData> appRecommendData) {
                                        if(appRecommendData != null && !appRecommendData.isEmpty()) {
                                            AppDetailWrapBean recommendWrapBean = new AppDetailWrapBean();
                                            recommendWrapBean.setRecommendDataList(appModelListAdapter(appRecommendData));
                                            result.add(recommendWrapBean);
                                        }
                                        appDetailLiveData.setValue(result);
                                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                                    }

                                    @Override
                                    public void onFailed(Throwable e) {
                                        super.onFailed(e);
                                        appDetailLiveData.setValue(result);
                                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                                    }
                                });

                    }

                    @Override
                    public void onFailed(Throwable e) {
//                        super.onFailed(e);
                        AppDetailWrapBean detailWrapBean = new AppDetailWrapBean();
                        detailWrapBean.setAppModel(appModel);
                        result.add(detailWrapBean);
                        appDetailLiveData.setValue(result);
                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                    }
                });
        return appDetailLiveData;
    }

    private List<AppModel> appModelListAdapter(List<AppRecommendData> recommendDatas) {
        List<AppModel> appModelList = new ArrayList<>();
        for (AppRecommendData data : recommendDatas) {
            AppModel appModel = new AppModel();
            if (null != data.showInfo) {
                appModel.appName = data.showInfo.title;
                appModel.icon = data.showInfo.icon;
                appModel.downloads = data.showInfo.appDownloadCount;
            }
            if (null != data.onclick) {
                appModel.appId = data.onclick.appId;
                appModel.pkg = data.onclick.packagename;
            }
            appModelList.add(appModel);
        }
        return appModelList;
    }
    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "AppDetailViewModel: onCleared");
    }
}
