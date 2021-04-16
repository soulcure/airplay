package com.coocaa.tvpi.module.app.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.AppRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.module.app.bean.AppStoreWrapBean;

import java.util.ArrayList;
import java.util.List;

public class AppTabStoreViewModel extends BaseAppViewModel {
    private static final String TAG = AppTabStoreViewModel.class.getSimpleName();
    private MutableLiveData<List<AppStoreWrapBean>> appStoreLiveData = new MutableLiveData<>();

    public AppTabStoreViewModel() {
        super(TAG);
        Log.d(TAG, "AppTabStoreViewModel: init");
    }

    public LiveData<List<AppStoreWrapBean>> getAppStoreList() {
        List<AppStoreWrapBean> appStoreWrapBeanList = new ArrayList<>();
        Repository.get(AppRepository.class)
                .getAppList("409", 1, 15)
                .setCallback(new BaseRepositoryCallback<List<AppModel>>() {
                    @Override
                    public void onStart() {
                        loadStateLiveData.setValue(LoadState.LOADING);
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        loadStateLiveData.setValue(LoadState.LOAD_ERROR);
                    }

                    @Override
                    public void onSuccess(List<AppModel> appModels) {
                        AppStoreWrapBean wrapBean = new AppStoreWrapBean();
                        wrapBean.setAppList(appModels);
                        wrapBean.setClassifyId("409");
                        wrapBean.setClassifyName("推荐");
                        appStoreWrapBeanList.add(wrapBean);

                        Repository.get(AppRepository.class)
                                .getAppList("358", 1, 15)
                                .setCallback(new BaseRepositoryCallback<List<AppModel>>() {

                                    @Override
                                    public void onFailed(Throwable e) {
                                        super.onFailed(e);
                                        appStoreLiveData.setValue(appStoreWrapBeanList);
                                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                                    }

                                    @Override
                                    public void onSuccess(List<AppModel> appModels) {
                                        AppStoreWrapBean wrapBean = new AppStoreWrapBean();
                                        wrapBean.setAppList(appModels);
                                        wrapBean.setClassifyId("358");
                                        wrapBean.setClassifyName("影音");
                                        appStoreWrapBeanList.add(wrapBean);


                                        Repository.get(AppRepository.class)
                                                .getAppList("361", 1, 15)
                                                .setCallback(new BaseRepositoryCallback<List<AppModel>>() {
                                                    @Override
                                                    public void onFailed(Throwable e) {
                                                        super.onFailed(e);
                                                        appStoreLiveData.setValue(appStoreWrapBeanList);
                                                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                                                    }

                                                    @Override
                                                    public void onSuccess(List<AppModel> appModels) {
                                                        AppStoreWrapBean wrapBean = new AppStoreWrapBean();
                                                        wrapBean.setAppList(appModels);
                                                        wrapBean.setClassifyId("361");
                                                        wrapBean.setClassifyName("游戏");
                                                        appStoreWrapBeanList.add(wrapBean);
                                                        appStoreLiveData.setValue(appStoreWrapBeanList);
                                                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                                                    }
                                                });
                                    }
                                });
                    }
                });
        return appStoreLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "AppTabStoreViewModel: onCleared ");
    }
}
