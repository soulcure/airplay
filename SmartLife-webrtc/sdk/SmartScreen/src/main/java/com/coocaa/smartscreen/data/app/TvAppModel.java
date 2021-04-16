package com.coocaa.smartscreen.data.app;

import java.util.Objects;

/**
 * @ClassName TvAppModel
 * @Description 电视端通过de返回的数据
 */
public class TvAppModel {
    public String mainActivity;
    public boolean isSystemApp;
    public int usedTimes;
    public int flag;
    public boolean hasUpdate;
    public String appName;
    public String pkgName;
    public String versionName;
    public String versionCode;

    public String coverUrl;
    public boolean isSelected;
    public String sortLetters;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TvAppModel)) return false;
        TvAppModel appModel = (TvAppModel) o;
        return Objects.equals(mainActivity, appModel.mainActivity) &&
                Objects.equals(appName, appModel.appName) &&
                Objects.equals(pkgName, appModel.pkgName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainActivity, appName, pkgName);
    }

    @Override
    public String toString() {
        return "TvAppModel{" +
                "appName='" + appName + '\'' +
                ", pkgName='" + pkgName + '\'' +
                '}';
    }
}
