package com.coocaa.smartscreen.network.api;

import com.coocaa.smartscreen.data.app.AppDetailRecommendResp;
import com.coocaa.smartscreen.data.app.AppDetailResp;
import com.coocaa.smartscreen.data.app.AppListResp;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.QueryMap;

import static com.coocaa.smartscreen.network.api.Api.APP_STORE_DOMAIN_NAME;
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
    Observable<AppListResp> getAppList(@QueryMap Map<String, Object> queryMap);

    //详情接口
    @Headers({DOMAIN_NAME_HEADER + APP_STORE_DOMAIN_NAME})
    @GET("/appDetail.html")
    Observable<AppDetailResp> getAppDetail(@QueryMap Map<String, Object> queryMap);

    //详情页下的猜你喜欢：应用推荐
    @Headers({DOMAIN_NAME_HEADER + APP_STORE_DOMAIN_NAME})
    @GET("/recommendAppInDetail.html")
    Observable<AppDetailRecommendResp> getRecommendApp(@QueryMap Map<String, Object> queryMap);



    @Headers({DOMAIN_NAME_HEADER + APP_STORE_DOMAIN_NAME})
    @GET("/searchAppEx.html")
    Observable<AppListResp> search(@QueryMap Map<String, Object> queryMap);

    // pad 这边还在使用。。。
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
