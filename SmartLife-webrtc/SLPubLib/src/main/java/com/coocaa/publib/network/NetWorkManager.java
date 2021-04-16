/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.coocaa.publib.network;

import com.coocaa.publib.BuildConfig;
import com.coocaa.publib.network.api.ApiService;
import com.coocaa.publib.network.api.AppStoreApiService;
import com.coocaa.publib.network.api.WXCoocaaApiService;
import com.coocaa.publib.network.api.XiaoweiApiService;
import com.coocaa.smartscreen.network.api.CoocaaAccountApiService;

import java.util.concurrent.TimeUnit;

import me.jessyan.retrofiturlmanager.RetrofitUrlManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.coocaa.publib.network.api.Api.APP_DEFAULT_DOMAIN;

/**
 * ================================================
 * Created by wuhaiyuan on 28/11/2019 11:49
 * ================================================
 */
public class NetWorkManager {
    private OkHttpClient mOkHttpClient;
    private Retrofit mRetrofit;
    private ApiService mApiService;
    private XiaoweiApiService mXiaoweiApiService;
    private CoocaaAccountApiService mCoocaaAccountApiService;
    private AppStoreApiService mAppStoreApiService;
    private WXCoocaaApiService mWXCoocaaApiService;

    private static class NetWorkManagerHolder {
        private static final NetWorkManager INSTANCE = new NetWorkManager();
    }

    public static final NetWorkManager getInstance() {
        return NetWorkManagerHolder.INSTANCE;
    }

    private NetWorkManager() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        if (BuildConfig.DEBUG) {
            // development build
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            // production build
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        }

        httpClient.addInterceptor(logging);

        this.mOkHttpClient = RetrofitUrlManager.getInstance().with(httpClient) //RetrofitUrlManager 初始化
                .readTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

        this.mRetrofit = new Retrofit.Builder()
                .baseUrl(APP_DEFAULT_DOMAIN)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//使用rxjava
                .addConverterFactory(GsonConverterFactory.create())//使用Gson
                .client(mOkHttpClient)
                .build();

        this.mApiService = mRetrofit.create(ApiService.class);
        this.mXiaoweiApiService = mRetrofit.create(XiaoweiApiService.class);
        this.mCoocaaAccountApiService = mRetrofit.create(CoocaaAccountApiService.class);
        this.mAppStoreApiService = mRetrofit.create(AppStoreApiService.class);
        this.mWXCoocaaApiService = mRetrofit.create(WXCoocaaApiService.class);
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public Retrofit getRetrofit() {
        return mRetrofit;
    }

    public ApiService getApiService() {
        return mApiService;
    }

    public XiaoweiApiService getXiaoweiApiService() {
        return mXiaoweiApiService;
    }

    public CoocaaAccountApiService getVideoCallApiService() {
        return mCoocaaAccountApiService;
    }

    public CoocaaAccountApiService getCoocaaAccountApiService() {
        return mCoocaaAccountApiService;
    }

    public AppStoreApiService getAppStoreApiService () {
        return mAppStoreApiService;
    }

    public WXCoocaaApiService getWXCoocaaApiService() {
        return mWXCoocaaApiService;
    }

}
