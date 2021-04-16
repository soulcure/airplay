package com.coocaa.smartscreen.repository.service.impl;

import com.coocaa.smartscreen.data.app.AppDetailRecommendResp;
import com.coocaa.smartscreen.data.app.AppDetailResp;
import com.coocaa.smartscreen.data.app.AppListResp;
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.data.app.AppRecommendData;
import com.coocaa.smartscreen.data.app.TvAppModel;
import com.coocaa.smartscreen.data.movie.LongVideoSearchResultModel;
import com.coocaa.smartscreen.data.videocall.ContactsResp;
import com.coocaa.smartscreen.network.NetWorkManager;
import com.coocaa.smartscreen.network.ObserverAdapter;
import com.coocaa.smartscreen.network.ResponseTransformer;
import com.coocaa.smartscreen.network.util.ParamsUtil;
import com.coocaa.smartscreen.repository.callback.RepositoryCallback;
import com.coocaa.smartscreen.repository.future.InvocateFuture;
import com.coocaa.smartscreen.repository.service.AppRepository;
import com.coocaa.smartscreen.repository.utils.Preferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coocaa.smartscreen.network.ResponseTransformer.SUCCESS_CODE_0;

public class AppRepositoryImpl implements AppRepository {
    @Override
    public InvocateFuture<AppModel> getAppDetail(final String pkg) {
        return new InvocateFuture<AppModel>() {
            @Override
            public void setCallback(final RepositoryCallback<AppModel> callback) {
                callback.onStart();
                HashMap<String, Object> map = new HashMap<>();
                map.put("pkg", pkg);
                NetWorkManager.getInstance()
                        .getAppStoreApiService()
                        .getAppDetail(map)
                        .compose(ResponseTransformer.<AppDetailResp>handException())
                        .subscribe(new ObserverAdapter<AppDetailResp>() {
                            @Override
                            public void onNext(AppDetailResp appDetailResp) {
                                if (appDetailResp != null && appDetailResp.data != null) {
                                    callback.onSuccess(appDetailResp.data);
                                } else {
                                    callback.onFailed(new Exception("app detail is empty"));
                                }
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
    public InvocateFuture<List<AppModel>> getAppList(final String classifyId, final int pageIndex, final int pageSize) {
        return new InvocateFuture<List<AppModel>>() {
            @Override
            public void setCallback(final RepositoryCallback<List<AppModel>> callback) {
                callback.onStart();
                HashMap<String, Object> map = new HashMap<>();
                map.put("classId", classifyId);
                map.put("seqType", "1");
                map.put("page", pageIndex);
                map.put("count", pageSize);
                NetWorkManager.getInstance()
                        .getAppStoreApiService()
                        .getAppList(map)
                        .compose(ResponseTransformer.<AppListResp>handException())
                        .subscribe(new ObserverAdapter<AppListResp>() {
                            @Override
                            public void onNext(AppListResp appListResp) {
                                if (appListResp != null && appListResp.data != null
                                        && appListResp.data.appList != null) {
                                    callback.onSuccess(appListResp.data.appList);
                                } else {
                                    callback.onFailed(new Exception("app list is empty"));
                                }
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
    public InvocateFuture<List<AppRecommendData>> getAppRecommendList(final int appId) {
        return new InvocateFuture<List<AppRecommendData>>() {
            @Override
            public void setCallback(final RepositoryCallback<List<AppRecommendData>> callback) {
                callback.onStart();
                Map<String, Object> map = new HashMap<>();
                map.put("appId", appId);
                NetWorkManager.getInstance()
                        .getAppStoreApiService()
                        .getRecommendApp(map)
                        .compose(ResponseTransformer.<AppDetailRecommendResp>handException())
                        .subscribe(new ObserverAdapter<AppDetailRecommendResp>() {
                            @Override
                            public void onNext(AppDetailRecommendResp appDetailRecommendResp) {
                                if (appDetailRecommendResp != null && appDetailRecommendResp.data != null) {
                                    callback.onSuccess(appDetailRecommendResp.data);
                                } else {
                                    callback.onFailed(new Exception("app recommend list is empty"));
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                super.onError(e);
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<List<AppModel>> search(final String keyword) {
        return new InvocateFuture<List<AppModel>>() {
            @Override
            public void setCallback(final RepositoryCallback<List<AppModel>> callback) {
                Map<String, Object> map = new HashMap<>();
                map.put("appName", keyword);
                map.put("page", 1);
                map.put("count", 100);
                NetWorkManager.getInstance()
                        .getAppStoreApiService()
                        .search(map)
                        .compose(ResponseTransformer.<AppListResp>handException())
                        .subscribe(new ObserverAdapter<AppListResp>() {
                            @Override
                            public void onNext(AppListResp appListResp) {
                                if (appListResp != null && appListResp.data != null
                                        && appListResp.data.appList != null) {
                                    callback.onSuccess(appListResp.data.appList);
                                } else {
                                    callback.onFailed(new Exception("search list is empty"));
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                super.onError(e);
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public List<String> querySearchHistory() {
        String searchList = Preferences.App.getSearchList();
        Gson gson = new Gson();
        return gson.fromJson(searchList, new TypeToken<List<String>>() {
        }.getType());
    }

    @Override
    public void addSearchHistory(String keyword) {
        Gson gson = new Gson();
        List<String> searchList = new ArrayList<>();
        String searchListJson = Preferences.App.getSearchList();
        if (searchListJson != null && !searchListJson.isEmpty()) {
            searchList = gson.fromJson(searchListJson, new TypeToken<List<String>>() {
            }.getType());
        }
        if(!searchList.contains(keyword)) {
            searchList.add(0, keyword);
        }
        Preferences.App.saveSearchList(gson.toJson(searchList));
    }

    @Override
    public void clearSearchHistory() {
        Preferences.App.saveSearchList("");
    }

    @Override
    public void saveTvAppList(List<TvAppModel> tvAppModelList) {
        Gson gson = new Gson();
        Preferences.App.saveTvAppList(gson.toJson(tvAppModelList));
    }

    @Override
    public List<TvAppModel> queryTvAppList() {
        String tvAppList = Preferences.App.getTvAppList();
        Gson gson = new Gson();
        return gson.fromJson(tvAppList, new TypeToken<List<TvAppModel>>() {}.getType());
    }
}
