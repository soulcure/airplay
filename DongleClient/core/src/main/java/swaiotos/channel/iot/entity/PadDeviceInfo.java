package swaiotos.channel.iot.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class PadDeviceInfo {
    public String activeId;             //激活ID
    public String deviceName;           //设备名称
    public String mChip, mModel, mSize; //机芯、机型、大小
    public String mUserId;             //酷开账号
    public String mNickName;            //酷开账号昵称


    public PadDeviceInfo(String in) {
        try {
            JSONObject object = new JSONObject(in);
            String activeId = object.optString("activeId");
            String deviceName = object.optString("deviceName");
            String mChip = object.optString("mChip");
            String mModel = object.optString("mModel");
            String mSize = object.optString("mSize");
            String mUserId = object.optString("mUserId");
            String mNickName = object.optString("mNickName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getActiveId() {
        return activeId;
    }

    public void setActiveId(String activeId) {
        this.activeId = activeId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getChip() {
        return mChip;
    }

    public void setChip(String chip) {
        this.mChip = chip;
    }

    public String getModel() {
        return mModel;
    }

    public void setModel(String model) {
        this.mModel = model;
    }

    public String getSize() {
        return mSize;
    }

    public void setSize(String size) {
        this.mSize = size;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        this.mUserId = userId;
    }

    public String getNickName() {
        return mNickName;
    }

    public void setNickName(String nickName) {
        this.mNickName = nickName;
    }
}
