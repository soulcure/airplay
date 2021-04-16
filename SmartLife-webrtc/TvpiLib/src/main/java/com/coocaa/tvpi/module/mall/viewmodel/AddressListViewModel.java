package com.coocaa.tvpi.module.mall.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartmall.data.api.HttpSubscribe;
import com.coocaa.smartmall.data.api.HttpThrowable;
import com.coocaa.smartmall.data.mobile.data.AddressResult;
import com.coocaa.smartmall.data.mobile.http.MobileRequestService;
import com.coocaa.smartscreen.repository.utils.Preferences;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class AddressListViewModel extends BaseViewModel {
    private static final String TAG = AddressListViewModel.class.getSimpleName();

    private MutableLiveData<List<AddressResult.GetAddressBean>> addressLiveData = new MutableLiveData<>();

    public AddressListViewModel() {
        Log.d(TAG, "AddressListViewModel: init");
    }

    public LiveData<List<AddressResult.GetAddressBean>> getAddressList() {
        List<AddressResult.GetAddressBean> addressFromLocal = getAddressFromLocal();
        addressLiveData.setValue(addressFromLocal);
        MobileRequestService.getInstance()
                .getAddress(new HttpSubscribe<AddressResult>() {
                    @Override
                    public void onSuccess(AddressResult result) {
                        Log.d(TAG, "onSuccess: " + result);
                        if (!result.isState()) {
                            ToastUtils.getInstance().showGlobalShort(result.getMsg());
                            return;
                        }
                        List<AddressResult.GetAddressBean> addressList = result.getGet_address();
                        addressLiveData.setValue(addressList);
                        saveAddressToLocal(addressList);
                    }

                    @Override
                    public void onError(HttpThrowable error) {
                        Log.d(TAG, "onError: " + error.getErrMsg());
                        ToastUtils.getInstance().showGlobalShort(error.getErrMsg());
                    }
                });
        return addressLiveData;
    }

    private void saveAddressToLocal(List<AddressResult.GetAddressBean> addressBeanList) {
        Gson gson = new Gson();
        Preferences.Mall.saveAddressList(gson.toJson(addressBeanList));
    }

    private List<AddressResult.GetAddressBean> getAddressFromLocal() {
        String addressJson = Preferences.Mall.getAddressList();
        Gson gson = new Gson();
        return gson.fromJson(addressJson, new TypeToken<List<AddressResult.GetAddressBean>>() {
        }.getType());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "AddressListViewModel : onCleared");
    }
}
