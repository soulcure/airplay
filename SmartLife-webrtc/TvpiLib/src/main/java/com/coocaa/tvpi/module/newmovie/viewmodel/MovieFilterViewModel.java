package com.coocaa.tvpi.module.newmovie.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.data.movie.CategoryFilterModel;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.MovieRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;

import java.util.List;

/**
 * 影视筛选的viewModel
 * Created by songxing on 2020/7/15
 */
public class MovieFilterViewModel extends BaseViewModel {
    private MutableLiveData<List<List<CategoryFilterModel>>> filterConditionLiveData = new MutableLiveData<>();
    private MutableLiveData<List<LongVideoListModel>> movieListLiveData = new MutableLiveData<>();


    public MovieFilterViewModel() {
        Log.d(TAG, "MovieFilterViewModel init");
    }

    public LiveData<List<List<CategoryFilterModel>>> getFilterCondition(String classifyId) {
        Repository.get(MovieRepository.class)
                .getFilterConditionList(classifyId)
                .setCallback(new BaseRepositoryCallback<List<List<CategoryFilterModel>>>() {
                    @Override
                    public void onSuccess(List<List<CategoryFilterModel>> lists) {
                        filterConditionLiveData.setValue(lists);
                    }
                });
        return filterConditionLiveData;
    }

    public LiveData<List<LongVideoListModel>> getMovieList(boolean showLoading, String classifyId, int pageIndex, int pageSize, List<String> filterValues,
                                                           List<String> sortValues, List<String> extraConditions) {
        Repository.get(MovieRepository.class)
                .getMovieList(classifyId, filterValues, sortValues, extraConditions, pageIndex, pageSize)
                .setCallback(new BaseRepositoryCallback<List<LongVideoListModel>>() {
                    @Override
                    public void onStart() {
                        if (showLoading) {
                            loadStateLiveData.setValue(LoadState.LOADING);
                        }
                    }

                    @Override
                    public void onSuccess(List<LongVideoListModel> longVideoListModels) {
                        movieListLiveData.setValue(longVideoListModels);
                        if(showLoading) {
                            loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                        }
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        if(showLoading) {
                            loadStateLiveData.setValue(LoadState.LOAD_ERROR);
                        }
                    }
                });
        return movieListLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "MovieFilterViewModel onCleared");
    }
}

