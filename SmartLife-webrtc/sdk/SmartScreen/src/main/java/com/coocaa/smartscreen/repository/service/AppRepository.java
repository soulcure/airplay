package com.coocaa.smartscreen.repository.service;

import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.data.app.AppRecommendData;
import com.coocaa.smartscreen.data.app.TvAppModel;
import com.coocaa.smartscreen.data.movie.Keyword;
import com.coocaa.smartscreen.data.movie.LongVideoSearchResultModel;
import com.coocaa.smartscreen.repository.future.InvocateFuture;

import java.util.List;

/**
 * 应用模块仓库接口
 */
public interface AppRepository {
    //远程
    /**
     * 获取应用详情
     *
     * @param pkg 文件包名
     * @return
     */
    InvocateFuture<AppModel> getAppDetail(String pkg);


    /**
     * 获取应用商店应用列表
     *
     * @param classifyId id
     * @param pageIndex  分页索引
     * @param pageSize   分页大小
     * @return
     */
    InvocateFuture<List<AppModel>> getAppList(String classifyId, int pageIndex, int pageSize);


    /**
     * 获取应用推荐列表
     *
     * @param appId id
     * @return
     */
    InvocateFuture<List<AppRecommendData>> getAppRecommendList(int appId);

    /**
     * 搜索
     * @param keyword 关键字
     * @return
     */
    InvocateFuture<List<AppModel>> search(String keyword);



    //本地
    /**
     * 获取应用搜索记录
     */
    List<String> querySearchHistory();

    /**
     * 添加应用搜索历史
     * @param keyword 搜索关键字
     */
    void addSearchHistory(String keyword);

    /**
     * 清除所有应用搜索历史
     */
    void clearSearchHistory();

    /**
     * 保存电视应用列表
     * @param tvAppModelList
     */
    void saveTvAppList(List<TvAppModel> tvAppModelList);

    /**
     * 查询电视应用列表
     */
    List<TvAppModel> queryTvAppList();
}
