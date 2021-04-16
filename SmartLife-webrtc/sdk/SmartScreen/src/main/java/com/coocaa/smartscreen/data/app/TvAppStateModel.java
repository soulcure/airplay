package com.coocaa.smartscreen.data.app;

/**
 * 电视apk状态
 * Created by songxing on 2020/9/9
 */
public class TvAppStateModel {
    public boolean installed;
    public int progrss;
    public int downloadStatus;
    public int status;

    public TvAppModel appinfo;


    //构造正在下载的初始bean
    public TvAppStateModel(AppModel appModel) {
        this.installed = false;
        this.progrss = 0;
        this.downloadStatus = 1;
        this.status = 0;

        TvAppModel tvAppModel = new TvAppModel();
        tvAppModel.mainActivity = appModel.mainActivity;
        tvAppModel.appName = appModel.appName;
        tvAppModel.pkgName = appModel.pkg;

        this.appinfo = tvAppModel;
    }

    @Override
    public String toString() {
        return "TvAppStateModel{" +
                "installed=" + installed +
                ", progrss=" + progrss +
                ", downloadStatus=" + downloadStatus +
                ", status=" + status +
                ", appinfo=" + appinfo +
                '}';
    }
}
