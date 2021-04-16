package com.coocaa.smartscreen.network.api;

import com.coocaa.smartscreen.data.function.FunctionHttpData;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

import static com.coocaa.smartscreen.network.api.Api.COOCAA_ACCOUNT_DOMAIN_NAME;
import static me.jessyan.retrofiturlmanager.RetrofitUrlManager.DOMAIN_NAME_HEADER;

/**
 * @Author: yuzhan
 */
public interface HomeContentApiService {

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @POST("/skyapi/common/getContent")
    @FormUrlEncoded
    Observable<FunctionHttpData> getFunction(@QueryMap Map<String, Object> queryMap,
                                             @FieldMap Map<String, Object> fieldMap);
}
