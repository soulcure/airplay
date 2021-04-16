package com.coocaa.smartscreen.data.channel;

import android.content.pm.ApplicationInfo;
import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * @ClassName AppInfo
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 1/11/21
 * @Version TODO (write something)
 */
public class AppInfo implements Serializable, Parcelable {
    public String appName;
    public String pkgName;
    public String className;
    public String versionName = "";
    public int versionCode;
    public long firstInstallTime;
    public int flag;

    public AppInfo() {

    }

    public AppInfo(Parcel in) {
        appName = in.readString();
        pkgName = in.readString();
        className = in.readString();
        versionName = in.readString();
        versionCode = in.readInt();
        firstInstallTime = in.readLong();
        flag = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appName);
        dest.writeString(pkgName);
        dest.writeString(className);
        dest.writeString(versionName);
        dest.writeInt(versionCode);
        dest.writeLong(firstInstallTime);
        dest.writeInt(flag);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };

    public boolean isSystemApp() {
        return ((flag & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
