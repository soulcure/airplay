package swaiotos.channel.iot.tv.iothandle.data;

import android.content.pm.ApplicationInfo;

import java.io.Serializable;

public class AppInfo implements Serializable {
    public String appName;
    public String pkgName;
    public String className;
    public String versionName = "";
    public int versionCode;
    public long firstInstallTime;
    public int flag;

    public boolean isSystemApp() {
        return ((flag & ApplicationInfo.FLAG_SYSTEM) != 0);
    }
}
