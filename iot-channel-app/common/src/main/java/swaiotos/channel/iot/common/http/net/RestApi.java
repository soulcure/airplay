package swaiotos.channel.iot.common.http.net;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;


public interface RestApi {

    @GET
    Call<ResponseBody> getLSIDInfo(@Url String url, @HeaderMap Map<String, String> headers,
                                   @QueryMap Map<String, String> querys);
}
