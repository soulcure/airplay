package com.swaiotos.skymirror.sdk.data;


import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.UnsupportedEncodingException;

/**
 * @ClassName: DeviceInfo
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2020/3/20 10:53
 */
public class DeviceInfo {
    private RemoteDevice device;
    private boolean bSupportMir;
    private int status;
    private String uuid;
    private String name;
    private String deviceID;
    private String deviceIP;
    private String deviceModel;
    private String deviceMac;//MAC地址
    private String deviceChip;//机芯
    private String deviceSkymid;//平台型号
    private String deviceVersion;//酷开系统版本

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public DeviceInfo(RemoteDevice device, boolean bSupportMir) {
        this.device = device;
        this.bSupportMir = bSupportMir;
    }

    public DeviceInfo(RemoteDevice device) {
        this.device = device;
    }

    public DeviceInfo() {

    }

    public void setDeviceInfo(DeviceInfo info) {
        setDeviceID(info.deviceID);
        setDeviceMac(info.deviceMac);
        setDeviceChip(info.deviceChip);
        setDeviceModel(info.deviceModel);
        setDeviceSkymid(info.deviceSkymid);
        setDeviceVersion(info.deviceVersion);
    }

    public String getDeviceIP() {
        return formatIp(device);
    }

    public void setDeviceIP(String deviceIP) {
        this.deviceIP = deviceIP;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceId) {
        this.deviceID = deviceId;
    }

    public String getUuid() {
        return formatId(device);
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getName() {
        return formatName(device);
    }

    public void setName(String name) {
        this.name = name;
    }

    public RemoteDevice getDevice() {
        return device;
    }

    public void setDevice(RemoteDevice device) {
        this.device = device;
    }

    public boolean isbSupportMir() {
        return bSupportMir;
    }

    public void setbSupportMir(boolean bSupportMir) {
        this.bSupportMir = bSupportMir;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getDeviceChip() {
        return deviceChip;
    }

    public void setDeviceChip(String deviceChip) {
        this.deviceChip = deviceChip;
    }

    public String getDeviceSkymid() {
        return deviceSkymid;
    }

    public void setDeviceSkymid(String deviceSkymid) {
        this.deviceSkymid = deviceSkymid;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String formatName(RemoteDevice device) {
        if (null == device)
            return "未知";
        String title = device.getDetails()
                .getFriendlyName();
        byte[] bs = new byte[title.length()];
        for (int i = 0; i < title.length(); i++) {
            bs[i] = (byte) title.charAt(i);
            if (Character.toString(title.charAt(i)).getBytes().length == 3) {
                bs = title.getBytes();
                break;
            }
        }
        try {
            return new String(bs, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "未知";
    }

    private String formatId(RemoteDevice device) {
        if (device == null)
            return "";
        try {
            return device.getIdentity().toString();
        }catch (NullPointerException exception) {
            return "";
        }
    }

    private String formatIp(RemoteDevice device) {
        try {
            return device.getIdentity().toString();
            //return device.getDetails().getPresentationURI().getHost();
        }catch (NullPointerException exception) {
            return "0.0.0.0";
        }
    }

    private String formatModel(RemoteDevice device) {
        try {
            return deviceModel;
            //return device.getDetails().getModelDetails().getModelName();
        } catch (NullPointerException exception) {
            return "未知";
        }
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "device=" + device +
                ", bSupportMir=" + bSupportMir +
                ", status=" + status +
                ", name='" + name + '\'' +
                ", deviceID='" + deviceID + '\'' +
                ", deviceIP='" + deviceIP + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", deviceMac='" + deviceMac + '\'' +
                ", deviceChip='" + deviceChip + '\'' +
                ", deviceSkymid='" + deviceSkymid + '\'' +
                ", deviceVersion='" + deviceVersion + '\'' +
                '}';
    }
}
