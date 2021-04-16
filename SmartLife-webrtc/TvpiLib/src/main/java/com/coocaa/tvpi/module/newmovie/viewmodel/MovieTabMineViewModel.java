package com.coocaa.tvpi.module.newmovie.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.data.movie.CollectionModel;
import com.coocaa.smartscreen.data.movie.PushHistoryModel;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.MovieRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 影视我的Tab ViewModel
 * Created by songxing on 2020/7/15
 */
public class MovieTabMineViewModel extends BaseViewModel {
    private MutableLiveData<List<CollectionModel>> collectionLiveData = new MutableLiveData<>();
    private MutableLiveData<List<PushHistoryModel.PushHistoryVideoModel>> pushHistoryLiveData = new MutableLiveData<>();

    public MovieTabMineViewModel() {
        Log.d(TAG, "MovieTabMineViewModel init");
    }

    public LiveData<List<CollectionModel>> getCollectionList() {
        Repository.get(MovieRepository.class)
                .getCollectionList(1, 0, 10)
                .setCallback(new BaseRepositoryCallback<List<CollectionModel>>() {
                    @Override
                    public void onSuccess(List<CollectionModel> collectionModels) {
                        collectionLiveData.setValue(collectionModels);
                    }
                });
        return collectionLiveData;
    }

    public LiveData<List<PushHistoryModel.PushHistoryVideoModel>> getPushHistoryModel() {
        Repository.get(MovieRepository.class)
                .getPushHistoryList()
                .setCallback(new BaseRepositoryCallback<PushHistoryModel>() {
                    @Override
                    public void onSuccess(PushHistoryModel pushHistoryModel) {
                        if (pushHistoryModel != null) {
                            List<PushHistoryModel.PushHistoryVideoModel> history = new ArrayList<>();
                            history.addAll(pushHistoryModel.movies_within_serven_days);
                            history.addAll(pushHistoryModel.movies_over_serven_days);
                            pushHistoryLiveData.setValue(history);
                        }
                    }
                });
        return pushHistoryLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "MovieTabMineViewModel onCleared");
    }
}
