package com.coocaa.smartscreen.repository.service;


import androidx.annotation.Nullable;

import com.coocaa.smartscreen.data.movie.CategoryFilterModel;
import com.coocaa.smartscreen.data.movie.CategoryMainModel;
import com.coocaa.smartscreen.data.movie.CollectionModel;
import com.coocaa.smartscreen.data.movie.Episode;
import com.coocaa.smartscreen.data.movie.Keyword;
import com.coocaa.smartscreen.data.movie.LongVideoDetailModel;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.smartscreen.data.movie.PushHistoryModel;
import com.coocaa.smartscreen.data.movie.SearchTypeModel;
import com.coocaa.smartscreen.data.movie.LongVideoSearchResultModel;
import com.coocaa.smartscreen.data.movie.VideoRecommendListModel;
import com.coocaa.smartscreen.repository.future.InvocateFuture;

import java.util.List;

/**
 * 影视模块数据
 * Created by songxing on 2020/7/9
 */
public interface MovieRepository {

    /**
     * 获取分类类型指定
     * @param source  指定source获取当前source的视频源，未指定获取当前绑定设备的视频源，未绑定获取默认视频源
     * @return
     */
    InvocateFuture<List<CategoryMainModel>> getMainCategory(@Nullable String source);

    /**
     * 获取影视
     * @param classifyId 分类id
     * @param filterValues 筛选条件tags
     * @param sortValues  排序条件tags
     * @param extraConditions 其他条件tags
     * @param pageIndex 当前分页
     * @param pageSize 分页大小
     * @return
     */
    InvocateFuture<List<LongVideoListModel>> getMovieList(String classifyId,
                                                          List<String> filterValues,
                                                          List<String> sortValues,
                                                          List<String> extraConditions,
                                                          int pageIndex,int pageSize);

    /**
     * 获取影视
     * @param classifyId 分类id
     * @param filterValues 筛选条件tags
     * @param sortValues  排序条件tags
     * @param extraConditions 其他条件tags
     * @param pageIndex 当前分页
     * @param pageSize 分页大小
     * @return
     */
    InvocateFuture<List<LongVideoListModel>> getMovieList(String source,
                                                          String classifyId,
                                                          List<String> filterValues,
                                                          List<String> sortValues,
                                                          List<String> extraConditions,
                                                          int pageIndex,int pageSize);

    /**
     * 获取收藏
     * @param videoType 类型 0短视频，1长视频
     * @param pageIndex 当前分页
     * @param pageSize 分页大小
     * @return
     */
    InvocateFuture<List<CollectionModel>> getCollectionList(int videoType,int pageIndex,int pageSize);

    /**
     * 删除收藏
     * @param idList 需要删除的id
     * @return
     */
    InvocateFuture<Void> deleteCollectionList(List<Integer> idList);


    /**
     * 添加获取取消收藏
     * @param collectionType  1收藏 2取消
     * @param videoType 0短片1正片
     * @param thirdAlbumId id
     * @param videoTitle title
     * @param videoPoster poster
     * @return
     */
    InvocateFuture<Void> addOrDeleteCollection(int collectionType, int videoType, String thirdAlbumId, String videoTitle, String videoPoster);

    /**
     * 获取推送影视历史
     * @return
     */
    InvocateFuture<PushHistoryModel> getPushHistoryList();

    /**
     * 上报推送历史
     * @param videoType 视频类型 0：短视频， 1：长视频
     * @param episode {@link Episode}
     * @return
     */
    InvocateFuture<Void> addPushHistory(String videoType, Episode episode);

    /**
     * 删除推送历史
     * @param ids 要删除的id
     * @return
     */
    InvocateFuture<Void> deletePushHistory(List<String> ids);


    /**
     * 获取筛选条件
     * @param classifyId
     * @return
     */
    InvocateFuture<List<List<CategoryFilterModel>>> getFilterConditionList(String classifyId);


    /**
     * 获取影视详情
     * @param thirdAlbumId id
     * @return
     */
    InvocateFuture<LongVideoDetailModel> getMovieDetail(String thirdAlbumId);


    /**
     * 获取影视详情相关影视列表
     * @param thirdAlbumId id
     * @param pageIndex 当前分页
     * @param pageSize 分页大小
     * @return
     */
    InvocateFuture<List<LongVideoListModel>>  getMovieRelateList(String thirdAlbumId,int pageIndex,int pageSize);

    /**
     * 获取搜索历史
     * @return
     */
    InvocateFuture<List<Keyword>> getSearchHistory();

    /**
     * 删除搜索历史
     * @return
     */
    InvocateFuture<Boolean> deleteSearchHistory();

    /**
     * 获取搜索类型
     * @return
     */
    InvocateFuture<List<SearchTypeModel>> getSearchType();

    /**
     * 根据搜索类型获取对应热门搜索列表
     * @param searchType
     * @return
     */
    InvocateFuture<List<Keyword>> getHotSearchListByType(String searchType);

    /**
     * 搜索
     * @param keyword 关键字
     * @param pageIndex 当前页
     * @param pageSize 分页大小
     * @return
     */
    InvocateFuture<List<LongVideoSearchResultModel>> search(String keyword, int pageIndex, int pageSize);

    /**
     * 获取推荐的影视列表
     * @return
     */
    InvocateFuture<List<VideoRecommendListModel>> getRecommendList();

}
