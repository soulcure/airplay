package com.coocaa.tvpi.module.homepager.adapter.bean;

import com.coocaa.smartscreen.data.function.FunctionBean;

import java.util.List;

public class SmartScreenWrapBean {
    public static final int TYPE_BANNER_LIST = 0;
    public static final int TYPE_FUNCTION_LIST = 1;

    private List<FunctionBean> bannerList;
    private List<FunctionBean> functionBeanList;
    public String bg;
    public String theme;
    public int style;

    public List<FunctionBean> getBannerList() {
        return bannerList;
    }

    public void setBannerList(List<FunctionBean> bannerList) {
        this.bannerList = bannerList;
    }

    public List<FunctionBean> getFunctionBeanList() {
        return functionBeanList;
    }

    public void setFunctionBeanList(List<FunctionBean> functionBeanList) {
        this.functionBeanList = functionBeanList;
    }

    public String getBg() {
        return bg;
    }

    public void setBg(String bg) {
        this.bg = bg;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    @Override
    public String toString() {
        return "SmartScreenWrapBean{" +
                "bannerList=" + bannerList +
                ", functionBeanList=" + functionBeanList +
                ", bg='" + bg + '\'' +
                ", theme='" + theme + '\'' +
                ", style=" + style +
                '}';
    }
}
