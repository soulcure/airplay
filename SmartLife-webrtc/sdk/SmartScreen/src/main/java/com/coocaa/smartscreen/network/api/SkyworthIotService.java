package com.coocaa.smartscreen.network.api;

import java.util.HashMap;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

import static com.coocaa.smartscreen.network.api.Api.SKYWORTH_IOT_DOMAIN_NAME;
import static com.coocaa.smartscreen.network.api.Api.XIAOWEI_DUMAIN_NAME;
import static me.jessyan.retrofiturlmanager.RetrofitUrlManager.DOMAIN_NAME_HEADER;

/**
 * @ClassName XiaoweiApiService
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2019-11-26
 * @Version TODO (write something)
 */
public interface SkyworthIotService {

    @Headers({DOMAIN_NAME_HEADER + SKYWORTH_IOT_DOMAIN_NAME})
    @GET("/otaupdate/app/upgrade/latest")
    Observable<ResponseBody> upgradeLatest(@QueryMap HashMap<String, String> queryMap);

    @GET()
    Observable<ResponseBody> queryShorQR(@Url String urlString);

    @Headers({DOMAIN_NAME_HEADER + SKYWORTH_IOT_DOMAIN_NAME})
    @POST("/dotmanager/v1/merchant/check")
    Observable<ResponseBody> checkMerchantPwd(@QueryMap HashMap<String, String> queryMap, @Body RequestBody body);
}
