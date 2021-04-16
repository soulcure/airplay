package swaiotos.channel.iot.ss.server.http.api;

import android.content.Context;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.fastjson.FastJsonConverterFactory;
import swaiotos.channel.iot.ss.server.utils.Constants;

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
    private OkHttpClient okHttpClient;


    private T httpService;
    private T httpAppStore;
    private T httpLog;

    public HttpManager() {

    }

    private synchronized OkHttpClient getClient() {
        if (okHttpClient == null) {
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
            okHttpClient =  builder.build();
        }
        return okHttpClient;
    }


    protected T getHttpService() {
        synchronized (HttpManager.class) {
            if (httpService == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .client(getClient())
                        .addConverterFactory(FastJsonConverterFactory.create())
                        .callbackExecutor(Executors.newFixedThreadPool(10))
                        .baseUrl(getBaseUrl())
                        .build();
                httpService = retrofit.create(getServiceClass());
            }
            return (T) httpService;
        }
    }


    protected T httpAppStore(Context context) {
        synchronized (HttpManager.class) {
            if (httpAppStore == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .client(getClient())
                        .addConverterFactory(FastJsonConverterFactory.create())
                        .callbackExecutor(Executors.newFixedThreadPool(2))
                        .baseUrl(Constants.getIotAppStoreServer(context))
                        //.baseUrl("http://beta-tc.skysrt.com")
                        .build();
                httpAppStore = retrofit.create(getServiceClass());
            }
            return (T) httpAppStore;
        }

    }


    protected T httpLog(Context context) {
        synchronized (HttpManager.class) {
            if (httpLog == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .client(getClient())
                        .addConverterFactory(FastJsonConverterFactory.create())
                        .callbackExecutor(Executors.newFixedThreadPool(2))
                        .baseUrl(Constants.getIOTLOGServer(context))
                        .build();
                httpLog = retrofit.create(getServiceClass());
            }
            return (T) httpLog;
        }

    }

}
