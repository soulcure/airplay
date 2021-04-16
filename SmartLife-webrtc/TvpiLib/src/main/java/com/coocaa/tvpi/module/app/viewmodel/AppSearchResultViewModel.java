package com.coocaa.tvpi.module.app.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.AppRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class AppSearchResultViewModel extends BaseAppViewModel {
    private static final String TAG = AppSearchResultViewModel.class.getSimpleName();
    private MutableLiveData<List<AppModel>> searchResultListLiveData = new MutableLiveData<>();
    private int searchResultSize;

    public AppSearchResultViewModel() {
        super(TAG);
        Log.d(TAG, "AppSearchResultViewModel: init");
    }

    public LiveData<List<AppModel>> search(String keyword) {
        Repository.get(AppRepository.class)
                .search(keyword)
                .setCallback(new BaseRepositoryCallback<List<AppModel>>() {
                    @Override
                    public void onStart() {
                        loadStateLiveData.setValue(LoadState.LOADING);
                    }

                    @Override
                    public void onSuccess(List<AppModel> appModels) {
                        searchResultSize = 0;
                        //只显示上架了的应用
                        List<AppModel> inStoreAppModelList = new ArrayList<>();
                        for (AppModel appModel : appModels) {
                            Repository.get(AppRepository.class)
                                    .getAppDetail(appModel.pkg)
                                    .setCallback(new BaseRepositoryCallback<AppModel>() {
                                        @Override
                                        public void onSuccess(AppModel appModel) {
                                            Log.d(TAG, "onSuccess: " + appModel);
                                            inStoreAppModelList.add(appModel);
                                            searchResultSize++;
                                            if (searchResultSize == appModels.size()) {
                                                Log.d(TAG, "finish");
                                                searchResultListLiveData.setValue(inStoreAppModelList);
                                                loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                                            }
                                        }

                                        @Override
                                        public void onFailed(Throwable e) {
                                            Log.d(TAG, "onFailed: " + e.toString());
                                            searchResultSize++;
                                            if (searchResultSize == appModels.size()) {
                                                Log.d(TAG, "finish");
                                                searchResultListLiveData.setValue(inStoreAppModelList);
                                                loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        loadStateLiveData.setValue(LoadState.LOAD_ERROR);
                    }
                });
        return searchResultListLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "AppSearchResultViewModel: onCleared ");
    }
}
