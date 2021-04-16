package com.coocaa.tvpi.module.newmovie.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.data.movie.LongVideoSearchResultModel;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.MovieRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;

import java.util.List;

/**
 * 搜索结果ViewModel
 * Created by songxing on 2020/7/15
 */
public class SearchResultViewModel extends BaseViewModel {
    private MutableLiveData<List<LongVideoSearchResultModel>> searchResultListLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> collectionLiveData = new MutableLiveData<>();

    public SearchResultViewModel() {
        Log.d(TAG, "SearchResultViewModel init");
    }

    public LiveData<List<LongVideoSearchResultModel>> search(boolean showLoading,String keyword, int pageIndex, int pageSize) {
        Repository.get(MovieRepository.class)
                .search(keyword, pageIndex, pageSize)
                .setCallback(new BaseRepositoryCallback<List<LongVideoSearchResultModel>>() {
                    @Override
                    public void onStart() {
                        if(showLoading) {
                            loadStateLiveData.postValue(LoadState.LOADING);
                        }
                    }

                    @Override
                    public void onSuccess(List<LongVideoSearchResultModel> videoSearchResultModels) {
                        if(showLoading) {
                            loadStateLiveData.postValue(LoadState.LOAD_FINISH);
                        }
                        searchResultListLiveData.setValue(videoSearchResultModels);
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        if(showLoading) {
                            loadStateLiveData.postValue(LoadState.LOAD_ERROR);
                        }
                    }
                });
        return searchResultListLiveData;
    }


    public LiveData<Boolean> collection(LongVideoSearchResultModel model) {
        Repository.get(MovieRepository.class)
                .addOrDeleteCollection(model.video_detail.is_collect == 1 ? 2 : 1, 1, model.video_detail.third_album_id,
                        model.video_detail.album_title, model.video_detail.video_poster)
                .setCallback(new BaseRepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        collectionLiveData.setValue(true);
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        collectionLiveData.setValue(false);
                    }
                });
        return collectionLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "SearchResultViewModel onCleared");
    }
}
