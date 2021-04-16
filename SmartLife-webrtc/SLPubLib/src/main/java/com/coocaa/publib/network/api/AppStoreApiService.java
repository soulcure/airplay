package com.coocaa.publib.network.api;

import java.util.HashMap;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

import static com.coocaa.publib.network.api.Api.APP_STORE_DOMAIN_NAME;
import static com.coocaa.publib.network.api.Api.XIAOWEI_DUMAIN_NAME;
import static me.jessyan.retrofiturlmanager.RetrofitUrlManager.DOMAIN_NAME_HEADER;

/**
 * @ClassName XiaoweiApiService
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2019-11-26
 * @Version TODO (write something)
 */
public interface AppStoreApiService {

    //获取应用列表
    @Headers({DOMAIN_NAME_HEADER + APP_STORE_DOMAIN_NAME})
    @GET("/appList.html")
    Observable<ResponseBody> getAppList(@QueryMap HashMap<String, String> queryMap);

    //详情接口
    @Headers({DOMAIN_NAME_HEADER + APP_STORE_DOMAIN_NAME})
    @GET("/appDetail.html")
    Observable<ResponseBody> getAppDetail(@QueryMap HashMap<String, String> queryMap);

    //详情页下的猜你喜欢：应用推荐
    @Headers({DOMAIN_NAME_HEADER + APP_STORE_DOMAIN_NAME})
    @GET("/recommendAppInDetail.html")
    Observable<ResponseBody> getRecommendApp(@QueryMap HashMap<String, String> queryMap);

}
