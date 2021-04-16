package com.coocaa.tvpi.base.mvvm;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel层基类
 * 提供有关加载中状态的LiveData
 * Created by songxing on 2020/7/12
 */
public class BaseViewModel extends ViewModel {
    protected static final String TAG = BaseViewModel.class.getSimpleName();

    public enum LoadState {
        LOADING,        //加载中
        LOADING_DIALOG, //加载中显示dialog
        LOAD_FINISH,    //加载完成
        LOAD_ERROR,     //加载异常
        LOAD_LIST_EMPTY //加载的列表为空
    }

    protected MutableLiveData<LoadState> loadStateLiveData = new MutableLiveData<>();


}
