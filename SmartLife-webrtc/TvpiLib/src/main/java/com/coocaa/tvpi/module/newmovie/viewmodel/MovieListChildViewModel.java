package com.coocaa.tvpi.module.newmovie.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.MovieRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;

import java.util.List;

public class MovieListChildViewModel extends BaseViewModel {
    private MutableLiveData<List<LongVideoListModel>> movieListLiveData = new MutableLiveData<>();

    public MovieListChildViewModel() {
        Log.d(TAG, "MovieListChildViewModel init");
    }


    public LiveData<List<LongVideoListModel>> getMovieList(boolean showLoading, String classifyId, int pageIndex, int pageSize) {
        Repository.get(MovieRepository.class)
                .getMovieList(classifyId, null, null, null, pageIndex, pageSize)
                .setCallback(new BaseRepositoryCallback<List<LongVideoListModel>>() {
                    @Override
                    public void onStart() {
                        if (showLoading) {
                            loadStateLiveData.setValue(LoadState.LOADING);
                        }
                    }

                    @Override
                    public void onSuccess(List<LongVideoListModel> longVideoListModels) {
                        if (longVideoListModels == null || longVideoListModels.isEmpty()) {
                            if (showLoading) {
                                loadStateLiveData.setValue(LoadState.LOAD_LIST_EMPTY);
                            }
                        } else {
                            if (showLoading) {
                                loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                            }
                            movieListLiveData.setValue(longVideoListModels);
                        }
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        if (showLoading) {
                            loadStateLiveData.setValue(LoadState.LOAD_ERROR);
                        }
                    }
                });
        return movieListLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "MovieListChildViewModel onCleared");
    }
}
