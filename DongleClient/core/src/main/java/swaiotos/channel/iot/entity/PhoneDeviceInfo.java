package swaiotos.channel.iot.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class PhoneDeviceInfo {
    public String imei;             //串号 例如：*#06#
    public String mUserId;         //酷开账号
    public String mNickName;        //酷开账号昵称
    public String mModel;           //型号
    public String mChip;            //机芯
    public String mSize;            //大小

    public PhoneDeviceInfo(String imei,
                           String userId,
                           String nickName,
                           String model,
                           String chip,
                           String size) {
        this.imei = imei;
        this.mUserId = userId;
        this.mNickName = nickName;
        this.mModel = model;
        this.mChip = chip;
        this.mSize = size;
    }

    public PhoneDeviceInfo(String in) {
        try {
            JSONObject object = new JSONObject(in);

            String imei = object.optString("imei");
            String mUserId = object.optString("mUserId");
            String mNickName = object.optString("mNickName");
            String mModel = object.optString("mModel");
            String mChip = object.optString("mChip");
            String mSize = object.optString("mSize");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
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

    public String getModel() {
        return mModel;
    }

    public void setModel(String model) {
        this.mModel = model;
    }

    public String getChip() {
        return mChip;
    }

    public void setChip(String chip) {
        this.mChip = chip;
    }

    public String getSize() {
        return mSize;
    }

    public void setSize(String size) {
        this.mSize = size;
    }
}
