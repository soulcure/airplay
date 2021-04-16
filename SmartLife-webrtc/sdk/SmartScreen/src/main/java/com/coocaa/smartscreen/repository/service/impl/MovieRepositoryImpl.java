package com.coocaa.smartscreen.repository.service.impl;


import android.text.TextUtils;

import com.coocaa.smartscreen.data.movie.CategoryFilterListModel;
import com.coocaa.smartscreen.data.movie.CategoryFilterModel;
import com.coocaa.smartscreen.data.movie.CategoryMainModel;
import com.coocaa.smartscreen.data.movie.CollectionModel;
import com.coocaa.smartscreen.data.movie.Episode;
import com.coocaa.smartscreen.data.movie.Keyword;
import com.coocaa.smartscreen.data.movie.LongVideoDetailModel;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.smartscreen.data.movie.LongVideoSearchResultModel;
import com.coocaa.smartscreen.data.movie.PushHistoryModel;
import com.coocaa.smartscreen.data.movie.SearchTypeModel;
import com.coocaa.smartscreen.data.movie.VideoRecommendListModel;
import com.coocaa.smartscreen.network.NetWorkManager;
import com.coocaa.smartscreen.network.ObserverAdapter;
import com.coocaa.smartscreen.network.ResponseTransformer;
import com.coocaa.smartscreen.network.exception.ApiException;
import com.coocaa.smartscreen.network.util.ParamsUtil;
import com.coocaa.smartscreen.repository.callback.RepositoryCallback;
import com.coocaa.smartscreen.repository.future.InvocateFuture;
import com.coocaa.smartscreen.repository.service.MovieRepository;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.coocaa.smartscreen.network.ResponseTransformer.SUCCESS_CODE_0;

/**
 * 影视模块数据
 * Created by songxing on 2020/7/9
 */
public class MovieRepositoryImpl implements MovieRepository {

    @Override
    public InvocateFuture<List<CategoryMainModel>> getMainCategory(final String source) {
        if (TextUtils.isEmpty(source)) {
            return new InvocateFuture<List<CategoryMainModel>>() {
                @Override
                public void setCallback(final RepositoryCallback<List<CategoryMainModel>> callback) {
                    callback.onStart();
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("page_index", "0");
                    params.put("page_size", "100");
                    NetWorkManager.getInstance()
                            .getMovieApiService()
                            .getCategoryMain(ParamsUtil.getQueryMap(params))
                            .compose(ResponseTransformer.<List<CategoryMainModel>>handleResult(SUCCESS_CODE_0))
                            .subscribe(new ObserverAdapter<List<CategoryMainModel>>() {
                                @Override
                                public void onNext(List<CategoryMainModel> categoryMainModels) {
                                    callback.onSuccess(categoryMainModels);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    callback.onFailed(e);
                                }
                            });
                }
            };
        } else {
            return new InvocateFuture<List<CategoryMainModel>>() {
                @Override
                public void setCallback(final RepositoryCallback<List<CategoryMainModel>> callback) {
                    callback.onStart();
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("page_index", "0");
                    params.put("page_size", "100");
                    params.put("tv_source", source);
                    NetWorkManager.getInstance()
                            .getMovieApiService()
                            .getCategoryMain(ParamsUtil.getQueryMap(params))
                            .compose(ResponseTransformer.<List<CategoryMainModel>>handleResult(SUCCESS_CODE_0))
                            .subscribe(new ObserverAdapter<List<CategoryMainModel>>() {
                                @Override
                                public void onNext(List<CategoryMainModel> categoryMainModels) {
                                    callback.onSuccess(categoryMainModels);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    callback.onFailed(e);
                                }
                            });
                }
            };
        }
    }

    @Override
    public InvocateFuture<List<LongVideoListModel>> getMovieList(final String classifyId,
                                                                 final List<String> filterValues,
                                                                 final List<String> sortValues,
                                                                 final List<String> extraConditions,
                                                                 final int pageIndex,
                                                                 final int pageSize) {
        return getMovieList(null, classifyId, filterValues, sortValues, extraConditions, pageIndex, pageSize);
    }

    @Override
    public InvocateFuture<List<LongVideoListModel>> getMovieList(final String source,
                                                                 final String classifyId,
                                                                 final List<String> filterValues,
                                                                 final List<String> sortValues,
                                                                 final List<String> extraConditions,
                                                                 final int pageIndex,
                                                                 final int pageSize) {
        return new InvocateFuture<List<LongVideoListModel>>() {
            @Override
            public void setCallback(final RepositoryCallback<List<LongVideoListModel>> callback) {
                callback.onStart();
                HashMap<String, Object> queryParams = new HashMap<>();
                queryParams.put("classify_id", classifyId);
                queryParams.put("page_index", pageIndex);
                queryParams.put("page_size", pageSize);
                if(!TextUtils.isEmpty(source)) {
                    queryParams.put("tv_source", source);
                }
                Map<String, Object> fieldMap = new HashMap<>();
                if (filterValues != null && filterValues.size() > 0) {
                    fieldMap.put("filter_values", ParamsUtil.getStringJsonArray(filterValues));
                }
                if (sortValues != null && sortValues.size() > 0) {
                    fieldMap.put("sort_values", ParamsUtil.getStringJsonArray(sortValues));
                }
                if (extraConditions != null && extraConditions.size() > 0) {
                    fieldMap.put("extra_conditions", ParamsUtil.getStringJsonArray(extraConditions));
                }
                String json = ParamsUtil.getJsonStringParams(fieldMap);
                RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), json);
                NetWorkManager.getInstance()
                        .getMovieApiService()
                        .getMovieList(ParamsUtil.getQueryMap(queryParams), body)
                        .compose(ResponseTransformer.<List<LongVideoListModel>>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<List<LongVideoListModel>>() {
                            @Override
                            public void onNext(List<LongVideoListModel> longVideoListModels) {
                                callback.onSuccess(longVideoListModels);
                            }

                            @Override
                            public void onError(Throwable e) {
                                //兼容将空数据当作错误返回
                                if (e instanceof ApiException && ((ApiException) e).getCode() == 22201002) {
                                    callback.onSuccess(Collections.<LongVideoListModel>emptyList());
                                } else {
                                    callback.onFailed(e);
                                }
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<List<CollectionModel>> getCollectionList(final int videoType, final int pageIndex, final int pageSize) {
        return new InvocateFuture<List<CollectionModel>>() {
            @Override
            public void setCallback(final RepositoryCallback<List<CollectionModel>> callback) {
                callback.onStart();
                HashMap<String, Object> queryParams = new HashMap<>();
                queryParams.put("page_index", pageIndex);
                queryParams.put("page_size", pageSize);
                queryParams.put("video_type", videoType);
                NetWorkManager.getInstance()
                        .getMovieApiService()
                        .getCollectionList(ParamsUtil.getQueryMap(queryParams))
                        .compose(ResponseTransformer.<List<CollectionModel>>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<List<CollectionModel>>() {
                            @Override
                            public void onNext(List<CollectionModel> collectionModels) {
                                callback.onSuccess(collectionModels);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<Void> deleteCollectionList(final List<Integer> idList) {
        return new InvocateFuture<Void>() {
            @Override
            public void setCallback(final RepositoryCallback<Void> callback) {
                callback.onStart();
                HashMap<String, Object> bodyParams = new HashMap<>();
                bodyParams.put("collect_ids", ParamsUtil.getIntegerJsonArray(idList));
                RequestBody requestBody = RequestBody.create(MediaType.parse(
                        "Content-Type, application/json"), new JSONObject(bodyParams).toString());
                NetWorkManager.getInstance()
                        .getMovieApiService()
                        .deleteCollectionList(ParamsUtil.getQueryMap(null), requestBody)
                        .compose(ResponseTransformer.<Void>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<Void>() {
                            @Override
                            public void onNext(Void aVoid) {
                                callback.onSuccess(aVoid);
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e.getMessage().contains("item is null")) {
                                    callback.onSuccess(null);
                                } else {
                                    callback.onFailed(e);
                                }
                            }
                        });

            }
        };
    }

    @Override
    public InvocateFuture<Void> addOrDeleteCollection(final int collectionType, final int videoType,
                                                      final String thirdAlbumId, final String videoTitle,
                                                      final String videoPoster) {
        return new InvocateFuture<Void>() {
            @Override
            public void setCallback(final RepositoryCallback<Void> callback) {
                callback.onStart();
                HashMap<String, Object> queryMap = new HashMap<>();
                queryMap.put("collect_type", collectionType); //收藏 1 取消2
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("video_type", videoType);//0短片1正片
                bodyMap.put("third_album_id", thirdAlbumId);
                bodyMap.put("video_title", videoTitle);
                bodyMap.put("video_poster", videoPoster);
                RequestBody requestBody = RequestBody.create(MediaType.parse(
                        "Content-Type, application/json"), new JSONObject(bodyMap).toString());
                NetWorkManager.getInstance()
                        .getMovieApiService()
                        .addOrCancelCollection(ParamsUtil.getQueryMap(queryMap), requestBody)
                        .compose(ResponseTransformer.<Void>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<Void>() {
                            @Override
                            public void onNext(Void aVoid) {
                                callback.onSuccess(aVoid);
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e.getMessage().contains("item is null")) {
                                    callback.onSuccess(null);
                                } else {
                                    callback.onFailed(e);
                                }
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<PushHistoryModel> getPushHistoryList() {
        return new InvocateFuture<PushHistoryModel>() {
            @Override
            public void setCallback(final RepositoryCallback<PushHistoryModel> callback) {
                callback.onStart();
                NetWorkManager.getInstance()
                        .getMovieApiService()
                        .getPushHistoryList(ParamsUtil.getQueryMap(null))
                        .compose(ResponseTransformer.<PushHistoryModel>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<PushHistoryModel>() {
                            @Override
                            public void onNext(PushHistoryModel pushHistoryModel) {
                                callback.onSuccess(pushHistoryModel);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<Void> addPushHistory(final String videoType, final Episode episode) {
        return new InvocateFuture<Void>() {
            @Override
            public void setCallback(final RepositoryCallback<Void> callback) {
                callback.onStart();
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("video_type", videoType);
                bodyMap.put("title", episode.video_title);//0短片1正片
                bodyMap.put("video_id", episode.video_third_id);
                bodyMap.put("album_id", episode.third_album_id);
                bodyMap.put("poster_v", episode.video_poster);
                bodyMap.put("poster_h", episode.video_poster);
                RequestBody requestBody = RequestBody.create(MediaType.parse(
                        "Content-Type, application/json"), new JSONObject(bodyMap).toString());
                NetWorkManager.getInstance()
                        .getMovieApiService()
                        .addPushHistory(ParamsUtil.getQueryMap(null), requestBody)
                        .compose(ResponseTransformer.<Void>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<Void>() {
                            @Override
                            public void onNext(Void aVoid) {
                                callback.onSuccess(aVoid);
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e.getMessage().contains("item is null")) {
                                    callback.onSuccess(null);
                                } else {
                                    callback.onFailed(e);
                                }
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<Void> deletePushHistory(final List<String> ids) {
        return new InvocateFuture<Void>() {
            @Override
            public void setCallback(final RepositoryCallback<Void> callback) {
                callback.onStart();
                HashMap<String, Object> params = new HashMap<>();
                StringBuffer stringBuffer = new StringBuffer("{");
                for (String str : ids) {
                    stringBuffer.append(str + ",");
                }
                stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                stringBuffer.append("}");
                params.put("del_ids", stringBuffer.toString());
                NetWorkManager.getInstance()
                        .getMovieApiService()
                        .deletePushHistory(ParamsUtil.getQueryMap(params))
                        .compose(ResponseTransformer.<Void>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<Void>() {
                            @Override
                            public void onNext(Void aVoid) {
                                callback.onSuccess(aVoid);
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e.getMessage().contains("item is null")) {
                                    callback.onSuccess(null);
                                } else {
                                    callback.onFailed(e);
                                }
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<List<List<CategoryFilterModel>>> getFilterConditionList(final String classifyId) {
        return new InvocateFuture<List<List<CategoryFilterModel>>>() {
            @Override
            public void setCallback(final RepositoryCallback<List<List<CategoryFilterModel>>> callback) {
                callback.onStart();
                HashMap<String, Object> queryParams = new HashMap<>();
                queryParams.put("classify_id", classifyId);
                NetWorkManager.getInstance()
                        .getMovieApiService()
                        .getFilterList(ParamsUtil.getQueryMap(queryParams))
                        .compose(ResponseTransformer.<CategoryFilterListModel>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<CategoryFilterListModel>() {
                            @Override
                            public void onNext(CategoryFilterListModel categoryFilterListModel) {
                                List<List<CategoryFilterModel>> filterConditionList = new ArrayList<>();
                                int mapSize = categoryFilterListModel.tags.size();
                                Iterator<Map.Entry<String, List<CategoryFilterModel>>> kv = categoryFilterListModel.tags.entrySet().iterator();
                                for (int i = 0; i < mapSize; i++) {
                                    Map.Entry<String, List<CategoryFilterModel>> entry = kv.next();
                                    String key = entry.getKey();
                                    List<CategoryFilterModel> list = entry.getValue();
                                    // 如果key等于排序，则这个对应的筛选数据放到最后一栏展示，并且不会展示到筛选结果条上
                                    if (!key.equals("排序")) {
                                        filterConditionList.add(list);
                                    }
                                }
                                // 把"排序"对应的数据加载最末尾
                                List<CategoryFilterModel> specialList = categoryFilterListModel.tags.get("排序");
                                filterConditionList.add(specialList);
                                callback.onSuccess(filterConditionList);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<LongVideoDetailModel> getMovieDetail(final String thirdAlbumId) {
        return new InvocateFuture<LongVideoDetailModel>() {
            @Override
            public void setCallback(final RepositoryCallback<LongVideoDetailModel> callback) {
                callback.onStart();
                HashMap<String, Object> params = new HashMap<>();
                params.put("third_album_id", thirdAlbumId);
                NetWorkManager.getInstance()
                        .getMovieApiService()
                        .getMovieDetail(ParamsUtil.getQueryMap(params))
                        .compose(ResponseTransformer.<LongVideoDetailModel>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<LongVideoDetailModel>() {
                            @Override
                            public void onNext(LongVideoDetailModel longVideoDetailModel) {
                                callback.onSuccess(longVideoDetailModel);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<List<LongVideoListModel>> getMovieRelateList(final String thirdAlbumId, final int pageIndex, final int pageSize) {
        return new InvocateFuture<List<LongVideoListModel>>() {
            @Override
            public void setCallback(final RepositoryCallback<List<LongVideoListModel>> callback) {
                callback.onStart();
                HashMap<String, Object> params = new HashMap<>();
                params.put("third_album_id", thirdAlbumId);
                params.put("page_index", pageIndex);
                params.put("page_size", pageSize);
                NetWorkManager.getInstance()
                        .getMovieApiService()
                        .getMovieDetailRelateList(ParamsUtil.getQueryMap(params))
                        .compose(ResponseTransformer.<List<LongVideoListModel>>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<List<LongVideoListModel>>() {
                            @Override
                            public void onNext(List<LongVideoListModel> longVideoListModels) {
                                callback.onSuccess(longVideoListModels);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<List<Keyword>> getSearchHistory() {
        return new InvocateFuture<List<Keyword>>() {
            @Override
            public void setCallback(final RepositoryCallback<List<Keyword>> callback) {
                callback.onStart();
                NetWorkManager.getInstance()
                        .getMovieApiService()
                        .getSearchHistory(ParamsUtil.getQueryMap(null))
                        .compose(ResponseTransformer.<List<Keyword>>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<List<Keyword>>() {
                            @Override
                            public void onNext(List<Keyword> keywords) {
                                callback.onSuccess(keywords);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<Boolean> deleteSearchHistory() {
        return new InvocateFuture<Boolean>() {
            @Override
            public void setCallback(final RepositoryCallback<Boolean> callback) {
                callback.onStart();
                NetWorkManager.getInstance()
                        .getMovieApiService()
                        .deleteSearchHistory(ParamsUtil.getQueryMap(null))
                        .compose(ResponseTransformer.<Void>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<Void>() {
                            @Override
                            public void onNext(Void aVoid) {
                                callback.onSuccess(true);
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e.getMessage().contains("item is null")) {
                                    callback.onSuccess(true);
                                } else {
                                    callback.onFailed(e);
                                }
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<List<SearchTypeModel>> getSearchType() {
        return new InvocateFuture<List<SearchTypeModel>>() {
            @Override
            public void setCallback(final RepositoryCallback<List<SearchTypeModel>> callback) {
                callback.onStart();
                NetWorkManager.getInstance()
                        .getMovieApiService()
                        .getSearchType(ParamsUtil.getQueryMap(null))
                        .compose(ResponseTransformer.<List<SearchTypeModel>>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<List<SearchTypeModel>>() {
                            @Override
                            public void onNext(List<SearchTypeModel> searchTypeModels) {
                                callback.onSuccess(searchTypeModels);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<List<Keyword>> getHotSearchListByType(final String searchType) {
        return new InvocateFuture<List<Keyword>>() {
            @Override
            public void setCallback(final RepositoryCallback<List<Keyword>> callback) {
                callback.onStart();
                HashMap<String, Object> queryParams = new HashMap<>();
                queryParams.put("search_type", searchType);
                NetWorkManager.getInstance()
                        .getMovieApiService()
                        .getHotSearchTagByType(ParamsUtil.getQueryMap(queryParams))
                        .compose(ResponseTransformer.<List<Keyword>>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<List<Keyword>>() {
                            @Override
                            public void onNext(List<Keyword> keywords) {
                                callback.onSuccess(keywords);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<List<LongVideoSearchResultModel>> search(final String keyword, final int pageIndex, final int pageSize) {
        return new InvocateFuture<List<LongVideoSearchResultModel>>() {
            @Override
            public void setCallback(final RepositoryCallback<List<LongVideoSearchResultModel>> callback) {
                callback.onStart();
                HashMap<String, Object> queryParams = new HashMap<>();
                queryParams.put("keyword", keyword);
                queryParams.put("video_type", "1");
                queryParams.put("page_index", pageIndex);
                queryParams.put("page_size", pageSize);
                NetWorkManager.getInstance()
                        .getMovieApiService()
                        .search(ParamsUtil.getQueryMap(queryParams))
                        .compose(ResponseTransformer.<List<LongVideoSearchResultModel>>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<List<LongVideoSearchResultModel>>() {
                            @Override
                            public void onNext(List<LongVideoSearchResultModel> videoSearchResultModels) {
                                callback.onSuccess(videoSearchResultModels);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<List<VideoRecommendListModel>> getRecommendList() {
        return new InvocateFuture<List<VideoRecommendListModel>>() {
            @Override
            public void setCallback(final RepositoryCallback<List<VideoRecommendListModel>> callback) {
                callback.onStart();
                NetWorkManager.getInstance()
                        .getMovieApiService()
                        .getRecommendList(ParamsUtil.getQueryMap(null))
                        .compose(ResponseTransformer.<List<VideoRecommendListModel>>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<List<VideoRecommendListModel>>() {
                            @Override
                            public void onNext(List<VideoRecommendListModel> videoRecommendListModels) {
                                callback.onSuccess(videoRecommendListModels);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }
}
