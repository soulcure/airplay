package com.coocaa.tvpi.module.mine.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;

public class DeviceNameViewModel extends BaseViewModel {

    private MutableLiveData<String> updateResponse = new MutableLiveData<>();

    public LiveData<String> updateDeviceName(String accessToken, String nickName, String gender,
                                           String birthday) {
        Repository.get(LoginRepository.class).updateCoocaaUserInfo(accessToken, nickName, gender,
                birthday)
                .setCallback(new BaseRepositoryCallback<String>() {
                    @Override
                    public void onSuccess(String response) {
                        updateResponse.setValue(response);
                        updateLocalDeviceName();
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        updateResponse.setValue(e.getMessage());
                    }
                });
        return updateResponse;
    }

    private void updateLocalDeviceName() {
    }

}
