package com.coocaa.smartscreen.network.api;

import com.coocaa.smartscreen.data.BaseResp;
import com.coocaa.smartscreen.data.movie.CategoryFilterListModel;
import com.coocaa.smartscreen.data.movie.CategoryMainModel;
import com.coocaa.smartscreen.data.movie.CollectionModel;
import com.coocaa.smartscreen.data.movie.Keyword;
import com.coocaa.smartscreen.data.movie.LongVideoDetailModel;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.smartscreen.data.movie.PushHistoryModel;
import com.coocaa.smartscreen.data.movie.SearchTypeModel;
import com.coocaa.smartscreen.data.movie.LongVideoSearchResultModel;
import com.coocaa.smartscreen.data.movie.VideoRecommendListModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface MovieApiService {

    @GET("/video/client/longvideo/oneclassify")
    Observable<BaseResp<List<CategoryMainModel>>> getCategoryMain(@QueryMap Map<String,Object> queryMap);

    @POST("/video/client/longvideo/videolist")
    Observable<BaseResp<List<LongVideoListModel>>> getMovieList(@QueryMap Map<String,Object> queryMap, @Body RequestBody json);


    @GET("/video/client/collect/list")
    Observable<BaseResp<List<CollectionModel>>> getCollectionList(@QueryMap Map<String,Object> queryMap);

    @POST("/video/client/collect/delete")
    Observable<BaseResp<Void>> deleteCollectionList(@QueryMap Map<String,Object> queryMap,@Body RequestBody body);

    @POST("/video/client/collect/add")
    Observable<BaseResp<Void>> addOrCancelCollection(@QueryMap Map<String,Object> queryMap,@Body RequestBody body);


    @GET("/video/client/pushhistory/list")
    Observable<BaseResp<PushHistoryModel>> getPushHistoryList(@QueryMap Map<String,Object> queryMap);

    @POST("/video/client/pushhistory/add")
    Observable<BaseResp<Void>> addPushHistory(@QueryMap Map<String,Object> queryMap,@Body RequestBody body);

    @GET("/video/client/pushhistory/batchdel")
    Observable<BaseResp<Void>> deletePushHistory(@QueryMap Map<String,Object> queryMap);


    @GET("/video/client/longvideo/filterlist")
    Observable<BaseResp<CategoryFilterListModel>> getFilterList(@QueryMap Map<String,Object> queryMap);


    @GET("/video/client/longvideo/videolist")
    Observable<BaseResp<LongVideoDetailModel>> getMovieDetail(@QueryMap Map<String,Object> queryMap);

    @GET("/video/client/longvideo/relatelong")
    Observable<BaseResp<List<LongVideoListModel>>> getMovieDetailRelateList(@QueryMap Map<String,Object> queryMap);


    @GET("/video/client/search/history")
    Observable<BaseResp<List<Keyword>>> getSearchHistory(@QueryMap Map<String,Object> queryMap);

    @GET("/video/client/search/delhistory")
    Observable<BaseResp<Void>> deleteSearchHistory(@QueryMap Map<String,Object> queryMap);

    @GET("/video/client/search/search_type")
    Observable<BaseResp<List<SearchTypeModel>>> getSearchType(@QueryMap Map<String,Object> queryMap);

    @GET("/video/client/search/hot")
    Observable<BaseResp<List<Keyword>>> getHotSearchTagByType(@QueryMap Map<String,Object> queryMap);

    @GET("/video/client/search/list")
    Observable<BaseResp<List<LongVideoSearchResultModel>>> search(@QueryMap Map<String,Object> queryMap);

    @GET("/video/client/longvideo/recommendlist")
    Observable<BaseResp<List<VideoRecommendListModel>>> getRecommendList(@QueryMap HashMap<String, Object> queryMap);
}
