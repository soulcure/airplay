package com.coocaa.tvpi.module.app.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.AppRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;

import java.util.List;

/**
 * 应用商城列表
 * Created by songxing on 2020/7/16
 */
public class AppStoreListViewModel extends BaseAppViewModel {
    private static final String TAG = AppStoreListViewModel.class.getSimpleName();
    private MutableLiveData<List<AppModel>> appListLiveData = new MutableLiveData<>();

    public AppStoreListViewModel() {
        super(TAG);
        Log.d(TAG, "AppStoreListViewModel: init");
    }

    public LiveData<List<AppModel>> getAppStoreList(boolean showLoading, String classId, int pageIndex, int pageSize) {

        Repository.get(AppRepository.class)
                .getAppList(classId, pageIndex, pageSize)
                .setCallback(new BaseRepositoryCallback<List<AppModel>>() {
                    @Override
                    public void onStart() {
                        if (showLoading)
                            loadStateLiveData.setValue(LoadState.LOADING);
                    }

                    @Override
                    public void onSuccess(List<AppModel> appModels) {
                        appListLiveData.setValue(appModels);
                        if (showLoading)
                            loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        if (showLoading)
                            loadStateLiveData.setValue(LoadState.LOAD_ERROR);
                    }
                });
        return appListLiveData;
    }



    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "AppStoreListViewModel: onCleared ");
    }
}
