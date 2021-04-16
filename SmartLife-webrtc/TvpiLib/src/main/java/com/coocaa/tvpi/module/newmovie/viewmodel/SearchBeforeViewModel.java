package com.coocaa.tvpi.module.newmovie.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.data.movie.Keyword;
import com.coocaa.smartscreen.data.movie.SearchTypeModel;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.MovieRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;
import com.coocaa.tvpi.module.newmovie.bean.MovieSearchBeforeWrapBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索前ViewModel
 * Created by songxing on 2020/7/15
 */
public class SearchBeforeViewModel extends BaseViewModel {
    private MutableLiveData<List<MovieSearchBeforeWrapBean>> searchBeforeListLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> deleteHistoryLiveData = new MutableLiveData<>();

    public SearchBeforeViewModel() {
        Log.d(TAG, "SearchBeforeViewModel init");
    }


    public LiveData<List<MovieSearchBeforeWrapBean>> getSearchBeforeList() {
        loadStateLiveData.setValue(LoadState.LOADING);
        List<MovieSearchBeforeWrapBean> searchBeforeWrapBeanList = new ArrayList<>();
        Repository.get(MovieRepository.class)
                .getSearchHistory()
                .setCallback(new BaseRepositoryCallback<List<Keyword>>() {

                    @Override
                    public void onFailed(Throwable e) {
                        loadStateLiveData.setValue(LoadState.LOAD_ERROR);
                    }

                    @Override
                    public void onSuccess(List<Keyword> keywords) {
                        if (keywords != null && !keywords.isEmpty()) {
                            MovieSearchBeforeWrapBean historyWrapBean = new MovieSearchBeforeWrapBean();
                            historyWrapBean.setHistoryList(keywords);
                            searchBeforeWrapBeanList.add(historyWrapBean);
                        }

                        Repository.get(MovieRepository.class)
                                .getSearchType()
                                .setCallback(new BaseRepositoryCallback<List<SearchTypeModel>>() {
                                    @Override
                                    public void onFailed(Throwable e) {
                                        loadStateLiveData.setValue(LoadState.LOAD_ERROR);
                                    }

                                    @Override
                                    public void onSuccess(List<SearchTypeModel> searchTypeModels) {
                                        Repository.get(MovieRepository.class)
                                                .getHotSearchListByType(searchTypeModels.get(0).search_type)
                                                .setCallback(new BaseRepositoryCallback<List<Keyword>>() {
                                                    @Override
                                                    public void onFailed(Throwable e) {
                                                        loadStateLiveData.setValue(LoadState.LOAD_ERROR);
                                                    }

                                                    @Override
                                                    public void onSuccess(List<Keyword> keywords) {
                                                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                                                        if (keywords != null && !keywords.isEmpty()) {
                                                            MovieSearchBeforeWrapBean hotWrapBean = new MovieSearchBeforeWrapBean();
                                                            hotWrapBean.setHotList(keywords);
                                                            searchBeforeWrapBeanList.add(hotWrapBean);
                                                            searchBeforeListLiveData.setValue(searchBeforeWrapBeanList);
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                });

        return searchBeforeListLiveData;
    }

    public LiveData<Boolean> deleteSearchHistory() {
        loadStateLiveData.setValue(LoadState.LOADING_DIALOG);
        Repository.get(MovieRepository.class)
                .deleteSearchHistory()
                .setCallback(new BaseRepositoryCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        Log.d(TAG, "deleteSearchHistory " + aBoolean);
                        deleteHistoryLiveData.setValue(true);
                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        deleteHistoryLiveData.setValue(false);
                        loadStateLiveData.setValue(LoadState.LOAD_ERROR);
                    }
                });
        return deleteHistoryLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "SearchBeforeViewModel onCleared");
    }
}
