package com.coocaa.tvpi.module.app.bean;

import com.coocaa.smartscreen.data.app.AppModel;

import java.util.ArrayList;
import java.util.List;

public class AppSearchBeforeWrapBean {
    public static final int SEARCH_BEFORE_HISTORY = 0;
    public static final int SEARCH_BEFORE_RECOMMEND = 1;

    public List<String> historyList = new ArrayList<>();
    public List<AppModel> recommend = new ArrayList<>();


    public List<String> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<String> historyList) {
        this.historyList = historyList;
    }

    public List<AppModel> getRecommend() {
        return recommend;
    }

    public void setRecommend(List<AppModel> recommend) {
        this.recommend = recommend;
    }
}
