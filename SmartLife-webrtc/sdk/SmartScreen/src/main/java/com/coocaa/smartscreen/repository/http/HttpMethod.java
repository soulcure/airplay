package com.coocaa.smartscreen.repository.http;

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
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class HttpMethod<T> {
    private T mService = null;

    protected abstract String getBaseUrl();

    protected abstract int getTimeOut();

    protected abstract Class<T> getServiceClazz();

    protected abstract Map<String, String> getHeaders();


    protected boolean printLog(){
        return true;
    }

    public HttpMethod() {
        OkHttpClient client = getClient();
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(getBaseUrl())
                .addConverterFactory(FastJsonConverterFactory.create())
                .addCallAdapterFactory(CustomCallAdapterFactory.create())
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
        if (printLog() ) {
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
            android.util.Log.println(android.util.Log.DEBUG, "TvpiHttp", message);
        }
    }

    protected T getService() {
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
