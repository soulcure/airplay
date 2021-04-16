package com.coocaa.movie.web.product;

import com.coocaa.movie.web.base.HttpResult;

import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PayHttpService {

    @GET("/v3/source/getSourceList.html")
    HttpResult<String> getSourceList(@Query("data") String data);

    @GET("/v3/product/getFullProductList.html")
    HttpResult<String> getProductList(@Query("data") String data);

    @GET("/v3/order/genOrderByPreviewQrcode.html")
    HttpResult<String> getOrderUrl(@Query("data") String data);

    @GET("/v1/qrcode/getFullQrcode.html")
    HttpResult<String> getQrcode(@Query("data") String data, @Query("product_id") int product_id);
}
