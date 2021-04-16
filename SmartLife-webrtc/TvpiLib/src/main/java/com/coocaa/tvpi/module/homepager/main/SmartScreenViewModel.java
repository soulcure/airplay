package com.coocaa.tvpi.module.homepager.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.smartscreen.repository.http.home.HomeHttpMethod;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;

import java.util.List;

public class SmartScreenViewModel extends BaseViewModel {
    private static final String TAG = SmartScreenViewModel.class.getSimpleName();

    private final MutableLiveData<List<FunctionBean>> functionListLiveData = new MutableLiveData<>();

    public LiveData<List<FunctionBean>> getSmartScreenList(boolean showLoading, String deviceType) {
        if(showLoading) {
            loadStateLiveData.setValue(LoadState.LOADING);
        }
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                List<FunctionBean> functionBeanList = HomeHttpMethod.getInstance().getFunctionList(deviceType);
                HomeUIThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (functionBeanList != null && !functionBeanList.isEmpty()) {
                            functionListLiveData.setValue(functionBeanList);
                            loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                        }else {
                            loadStateLiveData.setValue(LoadState.LOAD_LIST_EMPTY);
                        }
                    }
                });
            }
        });

        return functionListLiveData;
    }
}
