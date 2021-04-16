package com.coocaa.tvpi.module.newmovie.viewmodel.share;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * 影视搜索模块共享的ViewModel
 * Created by songxing on 2020/7/15
 */
public class MovieSearchShareViewModel extends ViewModel {
    private static final String TAG = MovieSearchShareViewModel.class.getSimpleName();
    //是否显示搜索前的界面
    private final MutableLiveData<Boolean> isShowSearchBeforeLiveData = new MutableLiveData<>();

    //搜索关键字
    private final MutableLiveData<String> keywordLiveData = new MutableLiveData<>();


    public MovieSearchShareViewModel() {
        Log.d(TAG, "SearchShareViewModel init");
    }

    public LiveData<Boolean> isShowSearchBeforeLiveData() {
        return isShowSearchBeforeLiveData;
    }

    public LiveData<String> getKeywordLiveData() {
        return keywordLiveData;
    }

    public void setShowSearchBefore(boolean isShowSearchBefore){
        isShowSearchBeforeLiveData.setValue(isShowSearchBefore);
    }

    public void setSearchKeyword(String keyword){
        keywordLiveData.setValue(keyword);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "SearchShareViewModel onCleared");
    }
}
