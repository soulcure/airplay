package com.coocaa.smartscreen.data.channel.events;

import com.coocaa.smartscreen.data.channel.AppInfo;

import java.util.List;

/**
 * @ClassName AppInfoEvent
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 1/11/21
 * @Version TODO (write something)
 */
public class AppInfoEvent {
    public List<AppInfo> appInfos;

    public AppInfoEvent(List<AppInfo> appInfos) {
        this.appInfos = appInfos;
    }
}
