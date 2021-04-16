package com.coocaa.smartscreen.data.app;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class AppModel implements Serializable{
    //0:未安装；1:安装；2:正在安装
    public static final int STATE_UNINSTALL = 0;
    public static final int STATE_INSTALLED = 1;
    public static final int STATE_INSTALLING = 2;

    public int appId;
    public String appName;
    public String mainActivity;
    public String icon;
    public String appNewTvIcon;
    public int downloads;//下载数量
    public long fileSize;//文件大小
    public String pkg;//文件包名
    public float grade;//推荐指数
    public String webAppLink;//web应用链接 为web应用时赋值，否则为空
    public int appRunType;//应用运行方式 0：Android环境运行 1：浏览器运行
    public String browser;//指定浏览器名称
    public String extra;//扩展参数
    public int status;//app的安装状态    //0:未安装；1:安装；2:正在安装
    public String appVersion;//版本号
    public String desc;//应用简介
    public List<AppScreenShots> screenshots;//截图

    public boolean isSelected;    // 是否处于编辑模式下的选中状态
    public boolean isInEditMode;  // 是否处于编辑模式


    public AppModel() {
    }

    public AppModel(TvAppModel tvAppModel) {
        this.pkg = tvAppModel.pkgName;
        this.appName = tvAppModel.appName;
        this.mainActivity = tvAppModel.mainActivity;
        this.status = 1;
    }

    public class AppScreenShots implements Serializable {
        public String small_shot;
        public String shot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppModel)) return false;
        AppModel appModel = (AppModel) o;
        return appId == appModel.appId &&
                Objects.equals(pkg, appModel.pkg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, pkg);
    }

    @Override
    public String toString() {
        return "AppModel{" +
                " appName='" + appName + '\'' +
                '}';
    }
}
