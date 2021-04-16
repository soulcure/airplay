package com.coocaa.smartmall.data.tv.http;

import com.coocaa.smartmall.data.tv.data.DetailResult;
import com.coocaa.smartmall.data.tv.data.RecommandResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ISmartMallRequestMethod {
    @GET("index_recommend")//"/api/awesome/recommends"
    Call<RecommandResult> getRecommend();

    @GET("tv_product_detail")//"/api/awesome/product_details"
    Call<DetailResult> getDetail(@Query("product_id") String product_id);

}
