package com.coocaa.tvpi.module.newmovie.bean;

import com.coocaa.smartscreen.data.movie.Keyword;

import java.util.ArrayList;
import java.util.List;

public class MovieSearchBeforeWrapBean {
    public static final int SEARCH_BEFORE_HISTORY = 0;
    public static final int SEARCH_BEFORE_HOT = 1;

    public List<Keyword> historyList = new ArrayList<>();
    public List<Keyword> hotList = new ArrayList<>();

    public List<Keyword> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<Keyword> historyList) {
        this.historyList = historyList;
    }

    public List<Keyword> getHotList() {
        return hotList;
    }

    public void setHotList(List<Keyword> hotList) {
        this.hotList = hotList;
    }

    @Override
    public String toString() {
        return "MovieSearchBeforeWrapBean{" +
                "historyList=" + historyList +
                ", hotList=" + hotList +
                '}';
    }
}
