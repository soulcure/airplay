package com.coocaa.publib.data.appstore;

import java.util.List;

public class AppModel {
    public int appId;
    public String appName;
    public String mainActivity;
    public String icon;
    public int downloads;//下载数量
    public long fileSize;//文件大小
    public String pkg;//文件包名
    public float grade;//推荐指数
    public String webAppLink;//web应用链接 为web应用时赋值，否则为空
    public int appRunType;//应用运行方式 0：Android环境运行 1：浏览器运行
    public String browser;//指定浏览器名称
    public String extra;//扩展参数
    public int status;//app的安装状态
    public String appVersion;//版本号
    public String desc;//应用简介
    public List<AppScreenShots> screenshots;//截图

    public boolean isSelected;    // 是否处于编辑模式下的选中状态
    public boolean isInEditMode;  // 是否处于编辑模式

    public AppModel(TvAppModel tvAppModel) {
        this.pkg = tvAppModel.pkgName;
        this.appName = tvAppModel.appName;
        this.mainActivity = tvAppModel.mainActivity;
    }

    public AppModel() {

    }
}
