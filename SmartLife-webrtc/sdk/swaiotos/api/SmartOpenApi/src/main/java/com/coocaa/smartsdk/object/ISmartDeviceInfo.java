package com.coocaa.smartsdk.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * @Author: yuzhan
 */
public class ISmartDeviceInfo implements Serializable, Parcelable {
    public String deviceName = "";
    public boolean isTempDevice;
    public String zpRegisterType = "";
    public String lsid = "";
    public String deviceId = "";
    public String deviceType = "";
    public String ssid = "";//wifi名称
    public String password = ""; //wifi密码
    public String spaceId; //空间id
    public String netType;
    public String source;//影视源

    public ISmartDeviceInfo() {

    }

    protected ISmartDeviceInfo(Parcel in) {
        deviceName = in.readString();
        isTempDevice = in.readByte() != 0;
        zpRegisterType = in.readString();
        lsid = in.readString();
        deviceId = in.readString();
        deviceType = in.readString();
        ssid = in.readString();
        password = in.readString();
        spaceId = in.readString();
        netType = in.readString();
        source = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceName);
        dest.writeByte((byte) (isTempDevice ? 1 : 0));
        dest.writeString(zpRegisterType);
        dest.writeString(lsid);
        dest.writeString(deviceId);
        dest.writeString(deviceType);
        dest.writeString(ssid);
        dest.writeString(password);
        dest.writeString(spaceId);
        dest.writeString(netType);
        dest.writeString(source);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ISmartDeviceInfo> CREATOR = new Creator<ISmartDeviceInfo>() {
        @Override
        public ISmartDeviceInfo createFromParcel(Parcel in) {
            return new ISmartDeviceInfo(in);
        }

        @Override
        public ISmartDeviceInfo[] newArray(int size) {
            return new ISmartDeviceInfo[size];
        }
    };

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ISmartDeviceInfo{");
        sb.append("deviceName='").append(deviceName).append('\'');
        sb.append(", isTempDevice=").append(isTempDevice);
        sb.append(", zpRegisterType='").append(zpRegisterType).append('\'');
        sb.append(", lsid='").append(lsid).append('\'');
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append(", deviceType='").append(deviceType).append('\'');
        sb.append(", ssid='").append(ssid).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", netType='").append(netType).append('\'');
        sb.append(", spaceId='").append(spaceId).append('\'');
        sb.append(", source='").append(source).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
