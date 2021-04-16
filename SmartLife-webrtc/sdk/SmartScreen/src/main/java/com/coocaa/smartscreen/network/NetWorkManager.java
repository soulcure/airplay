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
package com.coocaa.smartscreen.network;

import com.coocaa.smartscreen.network.api.ApiService;
import com.coocaa.smartscreen.network.api.AppStoreApiService;
import com.coocaa.smartscreen.network.api.CoocaaAccountApiService;
import com.coocaa.smartscreen.network.api.HomeContentApiService;
import com.coocaa.smartscreen.network.api.MovieApiService;
import com.coocaa.smartscreen.network.api.SkyworthIotService;
import com.coocaa.smartscreen.network.api.VideoCallApiService;
import com.coocaa.smartscreen.network.api.VoiceControlApiService;
import com.coocaa.smartscreen.network.api.WXCoocaaApiService;
import com.coocaa.smartscreen.network.api.XiaoweiApiService;
import com.coocaa.smartscreen.network.api.Api;

import java.util.concurrent.TimeUnit;

import me.jessyan.retrofiturlmanager.RetrofitUrlManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

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
    private VideoCallApiService mVideoCallApiService;
    private MovieApiService mMovieApiService;
    private HomeContentApiService homeContentApiService;
    private VoiceControlApiService voiceControlApiService;
    private SkyworthIotService skyworthIotService;

    private static class NetWorkManagerHolder {
        private static final NetWorkManager INSTANCE = new NetWorkManager();
    }

    public static final NetWorkManager getInstance() {
        return NetWorkManagerHolder.INSTANCE;
    }



    private NetWorkManager() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        this.mOkHttpClient = RetrofitUrlManager.getInstance().with(new OkHttpClient.Builder()) //RetrofitUrlManager 初始化
                .readTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build();

        this.mRetrofit = new Retrofit.Builder()
                .baseUrl(Api.APP_DEFAULT_DOMAIN)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//使用rxjava
                .addConverterFactory(GsonConverterFactory.create())//使用Gson
                .client(mOkHttpClient)
                .build();

        this.mApiService = mRetrofit.create(ApiService.class);
        this.mXiaoweiApiService = mRetrofit.create(XiaoweiApiService.class);
        this.mCoocaaAccountApiService = mRetrofit.create(CoocaaAccountApiService.class);
        this.mAppStoreApiService = mRetrofit.create(AppStoreApiService.class);
        this.mWXCoocaaApiService = mRetrofit.create(WXCoocaaApiService.class);
        this.mVideoCallApiService = mRetrofit.create(VideoCallApiService.class);
        this.mMovieApiService = mRetrofit.create(MovieApiService.class);
        this.homeContentApiService = mRetrofit.create(HomeContentApiService.class);
        this.voiceControlApiService = mRetrofit.create(VoiceControlApiService.class);
        this.skyworthIotService = mRetrofit.create(SkyworthIotService.class);
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

    public VideoCallApiService getVideoCallApiService() {
        return mVideoCallApiService;
    }

    public CoocaaAccountApiService getCoocaaAccountApiService() {
        return mCoocaaAccountApiService;
    }

    public VoiceControlApiService getVoiceControlApiService() {
        return voiceControlApiService;
    }

    public MovieApiService getMovieApiService(){
        return mMovieApiService;
    }

    public AppStoreApiService getAppStoreApiService () {
        return mAppStoreApiService;
    }

    public WXCoocaaApiService getWXCoocaaApiService() {
        return mWXCoocaaApiService;
    }

    public HomeContentApiService getHomeContentApiService() {
        return homeContentApiService;
    }

    public SkyworthIotService getSkyworthIotService() {
        return skyworthIotService;
    }
}
