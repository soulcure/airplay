package com.coocaa.tvpi.module.app.bean;

import com.coocaa.smartscreen.data.app.AppModel;

import java.util.List;

public class AppDetailWrapBean {
    public static final int APP_DETAIL = 0;
    public static final int APP_DETAIL_RECOMMEND = 1;

    private AppModel appModel;
    private List<AppModel> recommendDataList;

    public AppModel getAppModel() {
        return appModel;
    }

    public void setAppModel(AppModel appModel) {
        this.appModel = appModel;
    }

    public List<AppModel> getRecommendDataList() {
        return recommendDataList;
    }

    public void setRecommendDataList(List<AppModel> recommendDataList) {
        this.recommendDataList = recommendDataList;
    }

    @Override
    public String toString() {
        return "AppDetailWrapBean{" +
                "appModel=" + appModel +
                ", recommendDataList=" + recommendDataList +
                '}';
    }
}
