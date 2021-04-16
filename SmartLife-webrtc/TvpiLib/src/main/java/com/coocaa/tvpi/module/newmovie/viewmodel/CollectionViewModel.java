package com.coocaa.tvpi.module.newmovie.viewmodel;

import androidx.lifecycle.LiveData;

import com.coocaa.smartscreen.data.movie.CollectionModel;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.MovieRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.base.mvvm.list.AbsListViewModel;

import java.util.List;

public class CollectionViewModel extends AbsListViewModel<CollectionModel> {

    @Override
    public LiveData<List<CollectionModel>> getListData(boolean showLoading, String id, int pageIndex, int pageSize) {
        Repository.get(MovieRepository.class)
                .getCollectionList(Integer.parseInt(id), pageIndex, pageSize)
                .setCallback(new BaseRepositoryCallback<List<CollectionModel>>() {
                    @Override
                    public void onStart() {
                        if (showLoading) {
                            loadStateLiveData.setValue(LoadState.LOADING);
                        }
                    }

                    @Override
                    public void onSuccess(List<CollectionModel> collectionModels) {
                        listDataLiveData.setValue(collectionModels);
                        if (showLoading) {
                            loadStateLiveData.setValue(LoadState.LOAD_FINISH);
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
        return listDataLiveData;
    }
}
