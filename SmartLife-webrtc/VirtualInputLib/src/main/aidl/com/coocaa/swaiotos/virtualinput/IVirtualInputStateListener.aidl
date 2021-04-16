// IVirtualInputState.aidl
package com.coocaa.swaiotos.virtualinput;

import com.coocaa.smartscreen.data.channel.AppInfo;

// Declare any non-default types here with import statements

interface IVirtualInputStateListener {
    void onStateChanged(in String businessStateJson);
    void onProgressLoading(in String json);
    void onProgressResult(in String json);
    void onAppInfoLoaded(in List<AppInfo> appInfos);
}