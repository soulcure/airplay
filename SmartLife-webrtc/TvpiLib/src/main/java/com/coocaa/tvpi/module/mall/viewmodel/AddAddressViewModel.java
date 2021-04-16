package com.coocaa.tvpi.module.mall.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartmall.data.api.HttpSubscribe;
import com.coocaa.smartmall.data.api.HttpThrowable;
import com.coocaa.smartmall.data.mobile.data.AddAddressResult;
import com.coocaa.smartmall.data.mobile.data.AddressRequest;
import com.coocaa.smartmall.data.mobile.data.AddressResult;
import com.coocaa.smartmall.data.mobile.http.MobileRequestService;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;
import com.coocaa.tvpi.event.AddressEvent;

import org.greenrobot.eventbus.EventBus;


public class AddAddressViewModel extends BaseViewModel {
    private static final String TAG = AddAddressViewModel.class.getSimpleName();
    private MutableLiveData<String> addAddressIdLiveData = new MutableLiveData<>();

    public LiveData<String> addAddress(boolean defaultAddress,
                                        String username,
                                        String userPhone,
                                        String area,
                                        String detailAddress) {
        loadStateLiveData.setValue(LoadState.LOADING_DIALOG);
        AddressRequest request = new AddressRequest();
        request.setDefault_address(defaultAddress ? 1 : 0);
        request.setUser_name(username);
        request.setUser_phone(userPhone);
        request.setArea(area);
        request.setDetailed_address(detailAddress);
        request.setFull_address(String.format("%s%s", area, detailAddress));
        MobileRequestService.getInstance()
                .newAddress(new HttpSubscribe<AddAddressResult>() {
                    @Override
                    public void onSuccess(AddAddressResult result) {
                        Log.d(TAG, "onSuccess: " + result);
                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                        if (!result.isState()) {
                            ToastUtils.getInstance().showGlobalShort(result.getMsg());
                            return;
                        }
                        AddressResult.GetAddressBean addressBean=new AddressResult.GetAddressBean();
                        addressBean.setAddress_id(result.getAddress_id());
                        addressBean.setDefault_address(request.getDefault_address());
                        addressBean.setUser_phone(request.getUser_phone());
                        addressBean.setUser_name(request.getUser_name());
                        addressBean.setFull_address(request.getFull_address());
                        addressBean.setArea(request.getArea());
                        addressBean.setDetailed_address(request.getDetailed_address());
                        EventBus.getDefault().post(new AddressEvent(addressBean,AddressEvent.ADD));
                        addAddressIdLiveData.setValue(result.getAddress_id());
                    }

                    @Override
                    public void onError(HttpThrowable error) {
                        Log.d(TAG, "onError: " + error.getErrMsg());
                        ToastUtils.getInstance().showGlobalShort(error.getErrMsg());
                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                    }
                }, request);
        return addAddressIdLiveData;
    }

}
