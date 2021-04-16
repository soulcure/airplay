package com.coocaa.tvpi.module.app.viewmodel.share;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
/**
 * 应用首页Activity共享的状态
 * Created by songxing on 2020/9/23
 */
public class AppHomeShareViewModel extends ViewModel {
    private static final String TAG = AppHomeShareViewModel.class.getSimpleName();

    //电视应用界面是否处于编辑状态
    private final MutableLiveData<Boolean> isEditStateLiveData = new MutableLiveData<>();

    public AppHomeShareViewModel() {
        Log.d(TAG, "AppHomeShareViewModel init");
    }

    public LiveData<Boolean> isEditState() {
        return isEditStateLiveData;
    }

    public void setEditState(boolean isEdit) {
        isEditStateLiveData.setValue(isEdit);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "AppHomeShareViewModel onCleared");
    }
}
