package com.coocaa.statemanager.common.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: ScreenApps
 * @Author: AwenZeng
 * @CreateDate: 2021/3/26 19:17
 * @Description:
 */
public class ScreenApps implements Serializable {
    public long timestamp;
    public List<AppItem> app_list;

    public static class AppItem implements Serializable {
        public String pkgname;
        public List<ScreenApps.AppWay> app_way;
    }

    public static class AppWay implements Serializable {
        public String pkgname;
        public String type;
        public String className;
        public String mediaType;
        public boolean isAutoExitNoDevice;
        public long disNetworExitTime;
        public long noDeviceExitTime;
    }
}
