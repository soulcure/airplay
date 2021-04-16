package com.coocaa.smartscreen.repository.service;

import com.coocaa.smartscreen.data.store.AddressResp;
import com.coocaa.smartscreen.data.store.CityResp;
import com.coocaa.smartscreen.data.store.ConfirmOrderResp;
import com.coocaa.smartscreen.data.store.OrderResp;
import com.coocaa.smartscreen.data.store.ProvinceResp;
import com.coocaa.smartscreen.data.store.StoreBannerResp;
import com.coocaa.smartscreen.data.store.StoreProductDetailResp;
import com.coocaa.smartscreen.data.store.StoreProductResp;
import com.coocaa.smartscreen.repository.future.InvocateFuture;

import java.util.List;

/**
 * 商城数据仓库
 * Created by songxing on 2020/8/5
 */
public interface MallRepository {
    InvocateFuture<List<AddressResp>> getAddressList();

    InvocateFuture<Void> addAddress(String userName, String phone, String area,
                                    String detailedArea, boolean defaultAddress);

    InvocateFuture<Void> updateAddress(int addressId, String userName, String phone,
                                       String area, String detailAddress, boolean defaultAddress);

    InvocateFuture<Void> deleteAddress(int addressId);

    InvocateFuture<List<ProvinceResp>> getProvinceList();

    InvocateFuture<List<CityResp>> getCityList(int provinceId);

    InvocateFuture<List<StoreBannerResp>> getStoreBannerList();

    InvocateFuture<List<StoreProductResp>> getStoreProductList(int page, int pageSize);

    InvocateFuture<List<StoreProductDetailResp>> getStoreProductDetail(String productId);

    InvocateFuture<List<OrderResp>> getOrderList();

    InvocateFuture<Void> createOrder(int addressId, String productId, int productCount,
                                     String paymentType,String invoiceType, float totalPrices);

    InvocateFuture<Void> cancelOrder(String orderId);

    InvocateFuture<ConfirmOrderResp> confirmOrder(String orderId);
}
