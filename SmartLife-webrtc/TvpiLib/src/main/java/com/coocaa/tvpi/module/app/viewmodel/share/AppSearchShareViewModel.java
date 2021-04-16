package com.coocaa.tvpi.module.app.viewmodel.share;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.AppRepository;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;

/**
 * 应用搜索Activity共享的状态
 * Created by songxing on 2020/7/15
 */
public class AppSearchShareViewModel extends BaseViewModel {
    private static final String TAG = AppSearchShareViewModel.class.getSimpleName();

    //是否显示搜索前的界面
    private final MutableLiveData<Boolean> isShowSearchBeforeLiveData = new MutableLiveData<>();
    //搜索关键字
    private final MutableLiveData<String> keywordLiveData = new MutableLiveData<>();

    public AppSearchShareViewModel() {
        Log.d(TAG, "AppSearchShareViewModel init");
    }

    public LiveData<Boolean> isShowSearchBeforeLiveData() {
        return isShowSearchBeforeLiveData;
    }

    public LiveData<String> getKeywordLiveData() {
        return keywordLiveData;
    }

    public void setShowSearchBefore(boolean isShowSearchBefore) {
        isShowSearchBeforeLiveData.setValue(isShowSearchBefore);
    }

    public void setSearchKeyword(String keyword) {
        keywordLiveData.setValue(keyword);
        Repository.get(AppRepository.class).addSearchHistory(keyword);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "AppShareViewModel onCleared");
    }
}
