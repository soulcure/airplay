package com.coocaa.tvpi.module.app.bean;

import com.coocaa.smartscreen.data.app.AppModel;

import java.util.List;

public class AppStoreWrapBean {

    public String classifyId;
    public String classifyName;
    public List<AppModel> appList;


    public String getClassifyId() {
        return classifyId;
    }

    public void setClassifyId(String classifyId) {
        this.classifyId = classifyId;
    }

    public String getClassifyName() {
        return classifyName;
    }

    public void setClassifyName(String classifyName) {
        this.classifyName = classifyName;
    }

    public List<AppModel> getAppList() {
        return appList;
    }

    public void setAppList(List<AppModel> appList) {
        this.appList = appList;
    }

    @Override
    public String toString() {
        return "AppStoreWrapBean{" +
                "classifyId='" + classifyId + '\'' +
                ", classifyName='" + classifyName + '\'' +
                ", appList=" + appList +
                '}';
    }
}
