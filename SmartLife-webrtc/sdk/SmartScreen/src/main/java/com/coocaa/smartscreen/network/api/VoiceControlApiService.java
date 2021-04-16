package com.coocaa.smartscreen.network.api;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.QueryMap;


import static com.coocaa.smartscreen.network.api.Api.VOICE_ADVICE_NAME;
import static me.jessyan.retrofiturlmanager.RetrofitUrlManager.DOMAIN_NAME_HEADER;

public interface VoiceControlApiService {


    @Headers({DOMAIN_NAME_HEADER + VOICE_ADVICE_NAME})
    @GET("/voicebot/livechannel/commonSetting")
    Observable<ResponseBody> getAdvice(@QueryMap Map<String,Object> queryMap);

}
