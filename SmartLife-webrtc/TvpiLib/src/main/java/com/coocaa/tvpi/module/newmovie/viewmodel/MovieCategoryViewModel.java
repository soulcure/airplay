package com.coocaa.tvpi.module.newmovie.viewmodel;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.data.movie.CategoryMainModel;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.MovieRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;

import java.util.List;

public class MovieCategoryViewModel extends BaseViewModel {
    private MutableLiveData<List<CategoryMainModel>> categoryListLiveData = new MutableLiveData<>();
    private MutableLiveData<List<LongVideoListModel>> movieListLiveData = new MutableLiveData<>();

    public LiveData<List<CategoryMainModel>> getMainCategoryList(@Nullable String source) {
        Repository.get(MovieRepository.class)
                .getMainCategory(source)
                .setCallback(new BaseRepositoryCallback<List<CategoryMainModel>>() {
                    @Override
                    public void onSuccess(List<CategoryMainModel> categoryMainModels) {
                        categoryListLiveData.setValue(categoryMainModels);
                    }
                });
        return categoryListLiveData;
    }

    public LiveData<List<LongVideoListModel>> getMovieList(boolean showLoading,String classifyId, int pageIndex, int pageSize) {
        return getMovieList(showLoading, null, classifyId, pageIndex, pageSize);
    }

    public LiveData<List<LongVideoListModel>> getMovieList(boolean showLoading, String source, String classifyId, int pageIndex, int pageSize) {
        Repository.get(MovieRepository.class)
                .getMovieList(source, classifyId, null, null, null, pageIndex, pageSize)
                .setCallback(new BaseRepositoryCallback<List<LongVideoListModel>>() {
                    @Override
                    public void onStart() {
                        if(showLoading) {
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
}
