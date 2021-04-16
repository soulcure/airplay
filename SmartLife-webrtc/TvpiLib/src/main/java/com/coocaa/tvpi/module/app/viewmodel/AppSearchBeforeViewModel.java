package com.coocaa.tvpi.module.app.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.AppRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.module.app.bean.AppSearchBeforeWrapBean;

import java.util.ArrayList;
import java.util.List;

public class AppSearchBeforeViewModel extends BaseAppViewModel {
    private static final String TAG = AppSearchBeforeViewModel.class.getSimpleName();
    private MutableLiveData<List<AppSearchBeforeWrapBean>> searchBeforeLiveData = new MutableLiveData<>();

    public AppSearchBeforeViewModel() {
        super(TAG);
        Log.d(TAG, "AppSearchBeforeViewModel: init");
    }

    public void clearHistoryList() {
        Repository.get(AppRepository.class).clearSearchHistory();
    }

    public LiveData<List<AppSearchBeforeWrapBean>> getSearchBeforeList() {
        loadStateLiveData.setValue(LoadState.LOADING);
        List<AppSearchBeforeWrapBean> searchBeforeWrapBeanList = new ArrayList<>();
        List<String> historyList = Repository.get(AppRepository.class).querySearchHistory();
        if (historyList != null && !historyList.isEmpty()) {
            AppSearchBeforeWrapBean historyBean = new AppSearchBeforeWrapBean();
            historyBean.setHistoryList(historyList);
            searchBeforeWrapBeanList.add(historyBean);
        }
        Repository.get(AppRepository.class)
                .getAppList("409", 1, 15)
                .setCallback(new BaseRepositoryCallback<List<AppModel>>() {

                    @Override
                    public void onSuccess(List<AppModel> appModels) {
                        AppSearchBeforeWrapBean recommendBean = new AppSearchBeforeWrapBean();
                        recommendBean.setRecommend(appModels);
                        searchBeforeWrapBeanList.add(recommendBean);
                        searchBeforeLiveData.setValue(searchBeforeWrapBeanList);
                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        loadStateLiveData.setValue(LoadState.LOAD_ERROR);
                    }
                });
        return searchBeforeLiveData;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "AppSearchBeforeViewModel: onCleared ");
    }
}
