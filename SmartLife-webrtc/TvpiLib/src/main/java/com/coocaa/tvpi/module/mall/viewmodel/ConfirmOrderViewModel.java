package com.coocaa.tvpi.module.mall.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartmall.data.api.HttpSubscribe;
import com.coocaa.smartmall.data.api.HttpThrowable;
import com.coocaa.smartmall.data.mobile.data.AddressResult;
import com.coocaa.smartmall.data.mobile.data.CreateOrderResult;
import com.coocaa.smartmall.data.mobile.data.OrderRequest;
import com.coocaa.smartmall.data.mobile.data.PaymentResult;
import com.coocaa.smartmall.data.mobile.http.MobileRequestService;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;
import com.coocaa.tvpi.module.mall.ConfirmOrderActivity;

import java.util.List;

public class ConfirmOrderViewModel extends BaseViewModel {
    private static final String TAG = ConfirmOrderActivity.class.getSimpleName();
    private MutableLiveData<CreateOrderResult> createOrderLiveData = new MutableLiveData<>();
    private MutableLiveData<PaymentResult> confirmOrderLiveData = new MutableLiveData<>();
    private MutableLiveData<AddressResult.GetAddressBean> addressLiveData = new MutableLiveData<>();

    public LiveData<CreateOrderResult> createOrder(String productId,
                                                   int productCount,
                                                   String totalPrice,
                                                   String addressId,
                                                   String payType,
                                                   int invoiceType,
                                                   String invoiceTitle,
                                                   String invoiceNum) {
        loadStateLiveData.setValue(LoadState.LOADING_DIALOG);
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setProduct_id(productId);
        orderRequest.setProduct_count(productCount);
        orderRequest.setTotal_prices(totalPrice);
        orderRequest.setAddress_id(addressId);
        orderRequest.setPayment_type(payType);
        orderRequest.setInvoice_type(invoiceType);
        orderRequest.setInvoice_title(invoiceTitle);
        orderRequest.setInvoice_tax(invoiceNum);
        MobileRequestService.getInstance()
                .newOrder(new HttpSubscribe<CreateOrderResult>() {
                    @Override
                    public void onSuccess(CreateOrderResult result) {
                        Log.d(TAG, "onSuccess: " + result);
                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                        if (!result.isState()) {
                            ToastUtils.getInstance().showGlobalShort(result.getMsg());
                            return;
                        }
                        createOrderLiveData.setValue(result);
                    }

                    @Override
                    public void onError(HttpThrowable error) {
                        Log.d(TAG, "onError: " + error.getErrMsg());
                        loadStateLiveData.setValue(LoadState.LOAD_FINISH);
                        ToastUtils.getInstance().showGlobalShort(error.getErrMsg());
                    }
                }, orderRequest);
        return createOrderLiveData;
    }


    public LiveData<PaymentResult> confirmPayment(String orderNum) {
        MobileRequestService.getInstance()
                .payment(new HttpSubscribe<PaymentResult>() {
                    @Override
                    public void onSuccess(PaymentResult result) {
                        Log.d(TAG, "onSuccess: " + result);
                        if (!result.isState()) {
                            ToastUtils.getInstance().showGlobalShort(result.getMsg());
                            return;
                        }
                        confirmOrderLiveData.setValue(result);
                    }

                    @Override
                    public void onError(HttpThrowable error) {
                        Log.d(TAG, "onError: " + error);
                        ToastUtils.getInstance().showGlobalShort(error.getErrMsg());
                    }
                }, orderNum);
        return confirmOrderLiveData;
    }

    public LiveData<AddressResult.GetAddressBean> getAddressById(String id){
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
                        AddressResult.GetAddressBean hitAddress = null;
                        if(addressList != null && !addressList.isEmpty()){
                            for (AddressResult.GetAddressBean getAddressBean : addressList) {
                                if(id.equals(getAddressBean.getAddress_id())){
                                    hitAddress = getAddressBean;
                                    break;
                                }
                            }
                        }
                        addressLiveData.setValue(hitAddress);
                    }

                    @Override
                    public void onError(HttpThrowable error) {
                        Log.d(TAG, "onError: " + error.getErrMsg());
                        ToastUtils.getInstance().showGlobalShort(error.getErrMsg());
                    }
                });
        return addressLiveData;
    }
}
