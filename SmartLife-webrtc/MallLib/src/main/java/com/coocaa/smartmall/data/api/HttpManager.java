package com.coocaa.smartmall.data.api;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

/**
 * Description:
 * Create by wzh on 2019-11-13
 */
public abstract class HttpManager<T> {

    //超时时间 30s
    private static final int DEFAULT_TIME_OUT = 30;
    private static final int DEFAULT_READ_TIME_OUT = 10;

    protected abstract Class<T> getServiceClass();

    protected abstract Map<String, String> getHeaders();

    protected abstract String getBaseUrl();

    public HttpManager() {

    }

    private OkHttpClient getClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpCommonInterceptor httpCommonInterceptor = new HttpCommonInterceptor.Builder()
                .addHeaderParams(getHeaders())
                .builder();
        builder.addInterceptor(httpCommonInterceptor);
        builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        //连接超时时间
        builder.connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);
        //写操作 超时时间
        builder.writeTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);
        //读操作超时时间
        builder.readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);
        // 添加公共参数拦截器
        return builder.build();
    }

    protected T getHttpService() {
        Retrofit retrofit = new Retrofit.Builder()
                .client(getClient())
                .addConverterFactory(FastJsonConverterFactory.create())
                .baseUrl(getBaseUrl())
                .build();
        return retrofit.create(getServiceClass());
    }
}
