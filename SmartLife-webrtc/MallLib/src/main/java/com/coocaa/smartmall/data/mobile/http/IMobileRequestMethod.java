package com.coocaa.smartmall.data.mobile.http;


import com.coocaa.smartmall.data.mobile.data.AddAddressResult;
import com.coocaa.smartmall.data.mobile.data.AddressResult;
import com.coocaa.smartmall.data.mobile.data.BannerResult;
import com.coocaa.smartmall.data.mobile.data.CreateOrderResult;
import com.coocaa.smartmall.data.mobile.data.LoginResult;
import com.coocaa.smartmall.data.mobile.data.OrderResult;
import com.coocaa.smartmall.data.mobile.data.PaymentResult;
import com.coocaa.smartmall.data.mobile.data.ProductDetailResult;
import com.coocaa.smartmall.data.mobile.data.ProductRecommendResult;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface IMobileRequestMethod {
    @GET("login")//1.账户关联接口
    Call<LoginResult> login(@Query("access_token") String token);

    @POST("address")//2.添加地址
    Call<AddAddressResult> newAddress(@Body RequestBody body);

    @PUT("address")//3.修改地址
    Call<AddressResult> editAddress(@Body RequestBody body);

    @DELETE("address")//4.刪除地址
    Call<AddressResult> deleteAddress(@Query("address_id")int address_id);

    @GET("address")//5.獲取地址
    Call<AddressResult> getAddress();

    @GET("index_phone_banner")//6.手机端banner图接口
    Call<BannerResult> getBanner();

    @GET("index_phone_recommend")//7.手机端商品推荐列表接口
    Call<ProductRecommendResult> getRecommend(@Query("page") int page,@Query("pagesize") int pageSize);

    @PATCH("product")//8.手机端商品详情接口
    Call<ProductDetailResult> getDetail(@Query("product_id") String product_id);

    @GET("order")//9.手机端订单列表接口
    Call<OrderResult> getOrder();

    @POST("order")//10.手机端生成订单接口
    Call<CreateOrderResult> newOrder(@Body RequestBody body);

    @PUT("order")//11.取消订单接口
    Call<OrderResult> cancleOrder(@Query("order_id")int order_id);

    @DELETE("order")//12.确认收货接口
    Call<OrderResult> commitOrder(@Query("order_id")int order_id);

    @GET("payment")//13.支付回调确认接口
    Call<PaymentResult> payMent(@Query("order_id")String orderNum);

}
