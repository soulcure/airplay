package com.coocaa.smartscreen.repository.service.impl;

import com.coocaa.smartscreen.data.store.AddressResp;
import com.coocaa.smartscreen.data.store.CityResp;
import com.coocaa.smartscreen.data.store.ConfirmOrderResp;
import com.coocaa.smartscreen.data.store.OrderResp;
import com.coocaa.smartscreen.data.store.ProvinceResp;
import com.coocaa.smartscreen.data.store.StoreBannerResp;
import com.coocaa.smartscreen.data.store.StoreProductDetailResp;
import com.coocaa.smartscreen.data.store.StoreProductResp;
import com.coocaa.smartscreen.repository.future.InvocateFuture;
import com.coocaa.smartscreen.repository.service.MallRepository;

import java.util.List;

public class MallRepositoryImpl implements MallRepository {

    @Override
    public InvocateFuture<List<AddressResp>> getAddressList() {
        return null;
    }

    @Override
    public InvocateFuture<Void> addAddress(String userName, String phone, String area, String detailedArea, boolean defaultAddress) {
        return null;
    }

    @Override
    public InvocateFuture<Void> updateAddress(int addressId, String userName, String phone, String area, String detailAddress, boolean defaultAddress) {
        return null;
    }

    @Override
    public InvocateFuture<Void> deleteAddress(int addressId) {
        return null;
    }

    @Override
    public InvocateFuture<List<ProvinceResp>> getProvinceList() {
        return null;
    }

    @Override
    public InvocateFuture<List<CityResp>> getCityList(int provinceId) {
        return null;
    }

    @Override
    public InvocateFuture<List<StoreBannerResp>> getStoreBannerList() {
        return null;
    }

    @Override
    public InvocateFuture<List<StoreProductResp>> getStoreProductList(int page, int pageSize) {
        return null;
    }

    @Override
    public InvocateFuture<List<StoreProductDetailResp>> getStoreProductDetail(String productId) {
        return null;
    }

    @Override
    public InvocateFuture<List<OrderResp>> getOrderList() {
        return null;
    }

    @Override
    public InvocateFuture<Void> createOrder(int addressId, String productId, int productCount, String paymentType, String invoiceType, float totalPrices) {
        return null;
    }

    @Override
    public InvocateFuture<Void> cancelOrder(String orderId) {
        return null;
    }

    @Override
    public InvocateFuture<ConfirmOrderResp> confirmOrder(String orderId) {
        return null;
    }
}
