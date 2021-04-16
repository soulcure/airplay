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
package com.coocaa.publib.network.api;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * ================================================
 * Created by wuhaiyuan on 28/11/2019 11:49
 * ================================================
 */
public interface ApiService {

    @GET("/video/client/longvideo/recommendlist")
    Observable<ResponseBody> getRecommentList(@QueryMap HashMap<String, Object> queryMap);

    // 首页推荐更多
    @GET("/video/client/longvideo/recommendmorelist")
    Observable<ResponseBody> getRecommendMoreList(@QueryMap HashMap<String, Object> queryMap);

    //长视频详情
    @GET("/video/client/longvideo/videodetail")
    Observable<ResponseBody> getVideoDetail(@QueryMap HashMap<String, Object> queryMap);

    @GET("/video/client/longvideo/episodeslist")
    Observable<ResponseBody> getVideoEpisodesList(@QueryMap HashMap<String, Object> queryMap);

    @GET("/vip/client/source/getbyactiveid")
    Observable<ResponseBody> getTvSource(@QueryMap HashMap<String, Object> queryMap);

    @GET("/video/client/homevideo/voicelist")
    Observable<ResponseBody> getVoiceTips(@QueryMap HashMap<String, Object> queryMap);

    @GET
    @Streaming        //使用Streaming 方式 Retrofit 不会一次性将ResponseBody 读取进入内存，否则文件很多容易OOM
    Observable<ResponseBody> downPic(@Url String fileUrl );

    @GET("https://dzpb.coocaa.com/picsaver/atpapis/themelist/")
    Observable<ResponseBody> getThemelist();

    // 赞转踩模块
    @POST("/video/client/collect/add")
    Observable<ResponseBody> addOrCancelCollect(@QueryMap Map<String,Object> queryMap,
                                                @Body RequestBody body);

    @GET("/video/client/longvideo/relatelong")
    Observable<ResponseBody> getRelateLong(@QueryMap Map<String,Object> queryMap);

    @GET("/video/client/longvideo/oneclassify")
    Observable<ResponseBody> getCategoryMain(@QueryMap Map<String,Object> queryMap);

    @GET("/app/client/oneclassify")
    Observable<ResponseBody> getHomeRecommendApp(@QueryMap Map<String,Object> queryMap);

    @GET("/adop/banner/getbannerad")
    Observable<ResponseBody> getBannerAd(@QueryMap Map<String,Object> queryMap);

    @GET("/video/client/longvideo/subclassify")
    Observable<ResponseBody> getSubClassifyList(@QueryMap Map<String,Object> queryMap);

    @GET("/video/client/longvideo/filterlist")
    Observable<ResponseBody> getFilterList(@QueryMap Map<String,Object> queryMap);


    @POST("/video/client/longvideo/videolist")
    Observable<ResponseBody> getVideoList(@QueryMap Map<String,Object> queryMap, @Body RequestBody json);

    //直播分类
    @GET("/video/client/tvlive/channel/class")
    Observable<ResponseBody> getTVLiveCategory(@QueryMap Map<String,Object> queryMap);

    //直播频道列表
    @GET("/video/client/tvlive/channellist")
    Observable<ResponseBody> getTVLiveChannelList(@QueryMap Map<String,Object> queryMap);
}
