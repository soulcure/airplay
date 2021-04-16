package com.coocaa.smartscreen.repository.http;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @Author: yuzhan
 */
public class HttpRequest {

    private final static String TAG = "TvpiHttp";

    public static class RequestResult implements Serializable {
        public int code = 0;
        public String msg = "";
        public String data;
    }

    static OkHttpClient client;

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(getTimeOut(), TimeUnit.SECONDS);
        builder.writeTimeout(getTimeOut(), TimeUnit.SECONDS);
        builder.connectTimeout(getTimeOut(), TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);
        builder.addInterceptor(new HttpLoggingInterceptor(new HttpMethod.HomeHttpLogger()).setLevel(HttpLoggingInterceptor.Level.HEADERS));
        builder.connectionPool(new ConnectionPool(1, 5, TimeUnit.SECONDS));
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder requestBuilder = chain.request().newBuilder();
                try {
                    Map<String, String> headers = new HashMap<>();
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
        client = builder.build();
    }

    public static void request(final String url, final HttpCallback<String> subscriber) {
        HttpExecutors.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "start http request url=" + url);
                    Request request = new Request.Builder().url(url).build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String body = response.body() == null ? null : response.body().string();
                        Log.d(TAG, "end http request url=" + url + "\n, body=" + body);
                        RequestResult result = new Gson().fromJson(body, RequestResult.class);
                        subscriber.callback(result.data);
                    } else {
                        subscriber.error(new Exception("data is empty for :" + url));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.error(e);
                }
            }
        });
    }

    public static String requestSync(final String url) {
        Log.d(TAG, "start http request url=" + url);
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String body = response.body() == null ? null : response.body().string();
                Log.d(TAG, "end http request url=" + url + "\n, body=" + body);
                RequestResult result = JSON.parseObject(body, RequestResult.class);
                return result.data;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String requestBodySync(final String url) {
        Log.d(TAG, "start http request url=" + url);
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String body = response.body() == null ? null : response.body().string();
                Log.d(TAG, "end http request url=" + url + "\n, body=" + body);
                return body;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int getTimeOut() {
        return 10;
    }
}
