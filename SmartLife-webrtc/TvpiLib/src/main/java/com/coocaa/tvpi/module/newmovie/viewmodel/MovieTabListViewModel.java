package com.coocaa.tvpi.module.newmovie.viewmodel;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.data.movie.CategoryMainModel;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.MovieRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;

import java.util.List;

/**
 * 影视-影视投屏Tab ViewModel
 * Created by songxing on 2020/7/15
 */
public class MovieTabListViewModel extends BaseViewModel {
    private MutableLiveData<List<CategoryMainModel>> categoryListLiveData = new MutableLiveData<>();

    public MovieTabListViewModel() {
        Log.d(TAG, "MovieTabListViewModel init");
    }

    public LiveData<List<CategoryMainModel>> getMainCategoryList(@Nullable String source) {
        Repository.get(MovieRepository.class)
                .getMainCategory(source)
                .setCallback(new BaseRepositoryCallback<List<CategoryMainModel>>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        loadStateLiveData.setValue(LoadState.LOADING);
                    }

                    @Override
                    public void onSuccess(List<CategoryMainModel> categoryMainModels) {
                        categoryListLiveData.setValue(categoryMainModels);
                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        loadStateLiveData.setValue(LoadState.LOAD_ERROR);
                    }
                });
        return categoryListLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "MovieTabListViewModel onCleared");
    }

}
