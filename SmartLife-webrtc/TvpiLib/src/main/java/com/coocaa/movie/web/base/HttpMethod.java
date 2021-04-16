package com.coocaa.movie.web.base;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.coocaa.movie.web.base.fastjson.FastJsonConverterFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

/**
 * Created by luwei on 16-9-7.
 */
public abstract class HttpMethod<T> {
    private T mService = null;

    public abstract String getBaseUrl();

    public abstract int getTimeOut();

    public abstract Class<T> getServiceClazz();

    public abstract Map<String, String> getHeaders();

    public boolean printLog(){
        return true;
    }

    public HttpMethod() {
        OkHttpClient client = getClient();
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(getBaseUrl())
                .addConverterFactory(FastJsonConverterFactory.create())
                .addCallAdapterFactory(CustomCallAdapterFactory.create())
//                .callFactory(new CoocaaHttpCallFactory(client, defaultDomainName()))
                .build();
        mService = retrofit.create(getServiceClazz());
    }

    public HttpMethod(String baseUrl) {
        if(TextUtils.isEmpty(baseUrl))
            throw new RuntimeException("baseUrl cannot be empty.");
        OkHttpClient client = getClient();
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/")
                .addConverterFactory(FastJsonConverterFactory.create())
                .addCallAdapterFactory(CustomCallAdapterFactory.create())
//                .callFactory(new CoocaaHttpCallFactory(client, defaultDomainName()))
                .build();
        mService = retrofit.create(getServiceClazz());
    }

    public HttpMethod(Map<String, String> headersMap) {
        OkHttpClient client = getClient(headersMap);
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(getBaseUrl())
                .addConverterFactory(FastJsonConverterFactory.create())
                .addCallAdapterFactory(CustomCallAdapterFactory.create())
//                .callFactory(new CoocaaHttpCallFactory(client, defaultDomainName()))
                .build();
        mService = retrofit.create(getServiceClazz());
    }

    protected OkHttpClient getClient() {
        return getClient(getHeaders());
    }

    private OkHttpClient getClient(final Map<String, String> headers) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder requestBuilder = chain.request().newBuilder();
                try {
                    if (headers != null && headers.size() > 0){
                        Iterator iterator = headers.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry entry = (Map.Entry) iterator.next();
                            requestBuilder.addHeader((String) entry.getKey(), (String) entry.getValue());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return chain.proceed(requestBuilder.build());
            }
        });
        httpClientBuilder.connectTimeout(getTimeOut(), TimeUnit.SECONDS);
        if (printLog()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HomeHttpLogger());
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            httpClientBuilder.addInterceptor(loggingInterceptor);
        }
//        httpClientBuilder.addNetworkInterceptor(new GHttpNetworkInterceptor("home","http://cl-dl.cc0808.com/home/"));
//        httpClientBuilder.connectionPool(new ConnectionPool(5, 15, TimeUnit.SECONDS));
        return httpClientBuilder.build();
    }

    public static class HomeHttpLogger implements HttpLoggingInterceptor.Logger {

        @Override
        public void log(String message) {
            android.util.Log.println(android.util.Log.DEBUG, "HomeHttp", message);
        }
    }

    public T getService() {
        return mService;
    }

    public <E> E map(HttpResult<E> httpResult) {
        if (httpResult != null) {
            if (httpResult.data != null)
                return httpResult.data;
        }
        return null;
    }

    protected <E> List<E> mapList(HttpListResult<E> httpResult) {
        if (httpResult != null) {
            if (httpResult.data != null)
                return httpResult.data;
        }
        return null;
    }
}
