package com.coocaa.smartscreen.connect.service;

import com.coocaa.smartscreen.data.channel.AppInfo;

import java.util.List;

/**
 * @Author: yuzhan
 */
public class MsgAppInfoEventObserver {

    public interface IAppInfoEventObserver {
        void onAppInfoLoaded(List<AppInfo> appInfo);
    }

    private static IAppInfoEventObserver observer;

    public static void setObserver(IAppInfoEventObserver o) {
        observer = o;
    }

    public static void onAppInfoLoaded(List<AppInfo> appInfo) {
        if(observer != null) {
            observer.onAppInfoLoaded(appInfo);
        }
    }
}
