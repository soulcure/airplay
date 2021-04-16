package com.coocaa.smartscreen.network.api;

import java.util.HashMap;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

import static com.coocaa.smartscreen.network.api.Api.XIAOWEI_DUMAIN_NAME;
import static me.jessyan.retrofiturlmanager.RetrofitUrlManager.DOMAIN_NAME_HEADER;

/**
 * @ClassName XiaoweiApiService
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2019-11-26
 * @Version TODO (write something)
 */
public interface XiaoweiApiService {

    @Headers({DOMAIN_NAME_HEADER + XIAOWEI_DUMAIN_NAME})
    @GET("/tvpaiNew/push/checkOnline")
    Observable<ResponseBody> checkOnline(@QueryMap HashMap<String, String> queryMap);

    @Headers({DOMAIN_NAME_HEADER + XIAOWEI_DUMAIN_NAME})
    @POST("/tvpaiNew/push/pushDevice")
    Observable<ResponseBody> pushDevice(@QueryMap HashMap<String, String> queryMap, @Body RequestBody body);

}
