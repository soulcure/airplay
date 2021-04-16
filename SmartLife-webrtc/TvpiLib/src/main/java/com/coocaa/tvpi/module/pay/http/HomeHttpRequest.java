package com.coocaa.tvpi.module.pay.http;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.smartscreen.repository.http.HttpMethod;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.pay.bean.PayConstant;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class HomeHttpRequest {

    private final static String TAG = "SmartHttp";

    static OkHttpClient client;

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(getTimeOut(), TimeUnit.SECONDS);
        builder.writeTimeout(getTimeOut(), TimeUnit.SECONDS);
        builder.connectTimeout(getTimeOut(), TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);
        builder.connectionPool(new ConnectionPool(1, 5, TimeUnit.SECONDS));
//        builder.addInterceptor(new Interceptor() {
//            @Override
//            public Response intercept(Chain chain) throws IOException {
//                Request.Builder requestBuilder = chain.request().newBuilder();
//                try {
//                    Map<String, String> headers = new HashMap<>();
//                    if (headers != null && headers.size() > 0){
//                        Iterator iterator = headers.entrySet().iterator();
//                        while (iterator.hasNext()) {
//                            Map.Entry entry = (Map.Entry) iterator.next();
//                            requestBuilder.addHeader((String) entry.getKey(), (String) entry.getValue());
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                return chain.proceed(requestBuilder.build());
//            }
//        });
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpMethod.HomeHttpLogger());
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);
        client = builder.build();
    }

    public static void request(final String url, String payType,final IPayCallback subscriber) {
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Request.Builder reqBuild = new Request.Builder();
                    switch (payType){
                        case PayConstant.PAY_ALI:
                            reqBuild.addHeader("paytype","ALIPAY_APP");
                            reqBuild.addHeader("appid", SmartConstans.getBusinessInfo().APPID_ALI);

                            break;
                        case PayConstant.PAY_WE:
                            reqBuild.addHeader("appid", SmartConstans.getBusinessInfo().APPID_WECHAT);
                            reqBuild.addHeader("paytype","WECHAT_APP");
                            break;
                    }
                    HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();

                    reqBuild.url(urlBuilder.build());
                    Request request = reqBuild.build();

                    for(String headerName:request.headers().names()){
                        Log.d(TAG,"header -> "+headerName+" : "+request.header(headerName));
                    }

                    Response response = client.newCall(request).execute();
                    Log.d(TAG, "response=" + response.toString());
                    Log.d(TAG, "response.body=" + JSON.toJSONString(response.body()));

                    if (response.isSuccessful()) {
                        String body = response.body() == null ? null : response.body().string();
                        Log.d(TAG, "end http request url=" + url + "\n, body=" + body);
                        PayBaseData result = JSON.parseObject(body, PayBaseData.class);
                        subscriber.payDataSuccessCallback(result.getData());
                    } else {
                        subscriber.payDataParamsErrorCallback("data is empty for :" + url);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.payDataErrorCallback(e);
                }
            }
        });

    }

    private static int getTimeOut() {
        return 10;
    }

    private Request.Builder addHeader(Request.Builder builder,String payType){
        switch (payType){
            case PayConstant.PAY_ALI:
                builder.addHeader("paytype","ALIPAY_APP");

                builder.addHeader("appid", SmartConstans.getBusinessInfo().APPID_ALI);
                break;
            case PayConstant.PAY_WE:
                builder.addHeader("paytype","WECHAT_APP");
                builder.addHeader("appid", SmartConstans.getBusinessInfo().APPID_WECHAT);
                break;
        }
        return builder;
    }
}

