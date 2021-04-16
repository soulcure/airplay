package com.coocaa.tvpi.module.mall.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartmall.data.api.HttpSubscribe;
import com.coocaa.smartmall.data.api.HttpThrowable;
import com.coocaa.smartmall.data.mobile.data.AddressRequest;
import com.coocaa.smartmall.data.mobile.data.AddressResult;
import com.coocaa.smartmall.data.mobile.http.MobileRequestService;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;
import com.coocaa.tvpi.event.AddressEvent;

import org.greenrobot.eventbus.EventBus;

public class UpdateAddressViewModel extends BaseViewModel {
    private static final String TAG = UpdateAddressViewModel.class.getSimpleName();
    private MutableLiveData<Boolean> updateAddressLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> deleteAddressLiveData = new MutableLiveData<>();

    public LiveData<Boolean> updateAddress(boolean defaultAddress,
                                           String addressId,
                                           String username,
                                           String userPhone,
                                           String area,
                                           String detailAddress) {
        loadStateLiveData.setValue(LoadState.LOADING_DIALOG);
       final AddressRequest request = new AddressRequest();
        request.setAddress_id(addressId);
        request.setDefault_address(defaultAddress ? 1 : 0);
        request.setUser_name(username);
        request.setUser_phone(userPhone);
        request.setArea(area);
        request.setDetailed_address(detailAddress);
        request.setFull_address(String.format("%s%s", area, detailAddress));
        MobileRequestService.getInstance()
                .editAddress(new HttpSubscribe<AddressResult>() {
                    @Override
                    public void onSuccess(AddressResult result) {
                        Log.d(TAG, "onSuccess: " + result);
                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                        if (!result.isState()) {
                            ToastUtils.getInstance().showGlobalShort(result.getMsg());
                            return;
                        }
                        updateAddressLiveData.setValue(true);
                        AddressResult.GetAddressBean addressBean=new AddressResult.GetAddressBean();
                        addressBean.setAddress_id(request.getAddress_id());
                        addressBean.setDefault_address(request.getDefault_address());
                        addressBean.setUser_phone(request.getUser_phone());
                        addressBean.setUser_name(request.getUser_name());
                        addressBean.setFull_address(request.getFull_address());
                        addressBean.setArea(request.getArea());
                        addressBean.setDetailed_address(request.getDetailed_address());
                        EventBus.getDefault().post(new AddressEvent(addressBean,AddressEvent.UPDATE));
                    }

                    @Override
                    public void onError(HttpThrowable error) {
                        Log.d(TAG, "onError: " + error.getErrMsg());
                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                        ToastUtils.getInstance().showGlobalShort(error.getErrMsg());
                    }
                }, request);

        return updateAddressLiveData;
    }


    public LiveData<Boolean> deleteAddress(final String addressId) {
        loadStateLiveData.setValue(LoadState.LOADING_DIALOG);
        MobileRequestService.getInstance()
                .deleteAddress(new HttpSubscribe<AddressResult>() {
                    @Override
                    public void onSuccess(AddressResult result) {
                        Log.d(TAG, "onSuccess: " + result);
                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                        if (!result.isState()) {
                            ToastUtils.getInstance().showGlobalShort(result.getMsg());
                            return;
                        }
                        AddressResult.GetAddressBean addressBean=new AddressResult.GetAddressBean();
                        addressBean.setAddress_id(addressId);
                        EventBus.getDefault().post(new AddressEvent(addressBean,AddressEvent.DELETE));
                        deleteAddressLiveData.setValue(true);
                    }

                    @Override
                    public void onError(HttpThrowable error) {
                        Log.d(TAG, "onError: " + error.getErrMsg());
                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                        ToastUtils.getInstance().showGlobalShort(error.getErrMsg());
                    }
                }, Integer.parseInt(addressId));
        return deleteAddressLiveData;
    }
}
