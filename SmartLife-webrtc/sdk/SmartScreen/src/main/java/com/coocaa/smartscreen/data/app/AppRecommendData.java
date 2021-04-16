package com.coocaa.smartscreen.data.app;

/**
 * @ClassName AppRecommendData
 * @Description 猜你喜欢app结构
 */
public class AppRecommendData {
    public ShowInfo showInfo;
    public OnClick onclick;

    public static class OnClick {
        public String packagename; //应用包名
        public String versioncode; //应用版本号
        public String dowhat; //startActivity
        public String bywhat; //action
        public String byvalue;
        public int appId;
    }

    public static class ShowInfo {
        public int appDownloadCount; //应用下载量
        public String icon; //应用图标
        public String title; //应用名称
        public float grade; // 推荐指数
    }

}
