package com.coocaa.statemanager.common.http;


import com.coocaa.statemanager.common.bean.MiracastAppInfo;
import com.coocaa.statemanager.common.bean.ScreenApps;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import swaiotos.channel.iot.ss.server.http.api.HttpResult;

/**
 * @ClassName: IAppStoreHttpMethod
 * @Author: AwenZeng
 * @CreateDate: 2019/12/20 15:05
 * @Description: 请求方法
 */
public interface IStateHttpMethod {

    @GET("/operation/v1/dongle/cast-list")
    Call<HttpResult<ScreenApps>> getScreenApps(@Query("appkey") String appkey,
                                               @Query("time") String time,
                                               @Query("sign") String sign);

    @GET("/dotmanager/v1/screen/dongle-get-config")
    Call<HttpResult<List<MiracastAppInfo>>> getDongleConfig(@Query("appkey") String appkey,
                                                            @Query("time") String time,
                                                            @Query("activation_id") String activation_id,
                                                            @Query("config_type") String config_type,
                                                            @Query("sign") String sign);

}
