package com.coocaa.tvpi.base.mvvm.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.tvpi.base.mvvm.BaseViewModel;

import java.util.List;
/**
 * 用于简单列表界面的ViewModel
 * Created by songxing on 2020/9/25
 */
public abstract class AbsListViewModel<ItemBean> extends BaseViewModel {
    private static final String TAG = AbsListViewModel.class.getSimpleName();

    protected MutableLiveData<List<ItemBean>> listDataLiveData = new MutableLiveData<>();

    public abstract LiveData<List<ItemBean>> getListData(boolean showLoading,String key,int pageIndex,int pageSize);

}
