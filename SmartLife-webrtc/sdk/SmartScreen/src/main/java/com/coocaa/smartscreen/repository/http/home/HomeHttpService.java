package com.coocaa.smartscreen.repository.http.home;

import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * @Author: yuzhan
 */
interface HomeHttpService {

    @POST("datav/v1/app-event/report")
    String submitLog(@QueryMap Map<String, String> map, @Body CcLogData body);

    @GET("operation/v1/mobile/app-list")
    String getFunctionList(@QueryMap Map<String, String> map);

    @GET("operation/v1/mobile/tab-list")
    String getTabList(@QueryMap Map<String, String> map);

    @GET("operation/v1/mobile/panel-list")
    String getPanelContentList(@QueryMap Map<String, String> map);

    @GET("operation/v1/mobile/get-mobile-banner")
    String getBannerList(@QueryMap Map<String, String> queryMap);

    @GET("operation/v1/mobile/scene-contr-list")
    String getSceneControlConfig(@QueryMap Map<String, String> queryMap);

    @GET("/operation/v1/mobile/get-client-config")
    String getClientConfig(@QueryMap Map<String, String> queryMap);

    @GET("/operation/v1/general/get-operation-data")
    String getOperationData(@QueryMap Map<String, String> queryMap);
}
