package com.coocaa.publib.network.api;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.QueryMap;

import static com.coocaa.publib.network.api.Api.WX_COOCAA_DOMAIN;
import static com.coocaa.publib.network.api.Api.WX_COOCAA_DOMAIN_NAME;
import static me.jessyan.retrofiturlmanager.RetrofitUrlManager.DOMAIN_NAME_HEADER;

/**
 * @ClassName XiaoweiApiService
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2019-11-26
 * @Version TODO (write something)
 */
public interface WXCoocaaApiService {
    
    @Headers({DOMAIN_NAME_HEADER + WX_COOCAA_DOMAIN_NAME})
    @GET("/spread/getStreams.coocaa")
    Observable<ResponseBody> queryKYPData(@QueryMap Map<String,Object> queryMap);

    @Headers({DOMAIN_NAME_HEADER + WX_COOCAA_DOMAIN_NAME})
    @GET("/articleMoviesAPI/getArticleDetail.coocaa")
    Observable<ResponseBody> getArticleDetail(@QueryMap Map<String,String> queryMap);

    @Headers({DOMAIN_NAME_HEADER + WX_COOCAA_DOMAIN_NAME})
    @GET("/articleMoviesAPI/getArticleMovies.coocaa")
    Observable<ResponseBody> getArticleMovies(@QueryMap Map<String,String> queryMap);

}
