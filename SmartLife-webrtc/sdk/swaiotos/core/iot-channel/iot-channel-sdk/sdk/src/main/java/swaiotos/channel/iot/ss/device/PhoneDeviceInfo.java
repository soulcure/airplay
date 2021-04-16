package swaiotos.channel.iot.ss.device;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @ClassName: PhoneDeviceInfo
 * @Author: lu
 * @CreateDate: 2020/4/22 2:46 PM
 * @Description:
 */
public class PhoneDeviceInfo extends DeviceInfo {
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

    public PhoneDeviceInfo() {
    }

    PhoneDeviceInfo(Parcel source) {
        this.imei = source.readString();
        this.mUserId = source.readString();
        this.mNickName = source.readString();
        this.mModel = source.readString();
        this.mChip = source.readString();
        this.mSize = source.readString();
    }

    @Override
    public TYPE type() {
        return TYPE.PHONE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imei);
        dest.writeString(mUserId);
        dest.writeString(mNickName);
        dest.writeString(mModel);
        dest.writeString(mChip);
        dest.writeString(mSize);
    }

//    @Override
//    public String encode() {
//        return "PhoneDeviceInfo[\n"
//                + "imei:" + imei + "\n"
//                + "userId:" + mUserId + "\n"
//                + "nickName:" + mNickName + "\n"
//                + "model:" + mModel + "\n"
//                + "chip:" + mChip + "\n"
//                + "size:" + mSize
//                + "]";
//    }

    public static final Creator<PhoneDeviceInfo> CREATOR = new Creator<PhoneDeviceInfo>() {
        @Override
        public PhoneDeviceInfo createFromParcel(Parcel source) {
            return new PhoneDeviceInfo(source);
        }

        @Override
        public PhoneDeviceInfo[] newArray(int size) {
            return new PhoneDeviceInfo[size];
        }
    };

    @Override
    public String encode() {
        JSONObject object = new JSONObject();
        try {
            object.put("imei", imei);
            object.put("mUserId", mUserId);
            object.put("mNickName", mNickName);
            object.put("mModel", mModel);
            object.put("mChip", mChip);
            object.put("mSize", mSize);
            object.put("clazzName",clazzName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public static PhoneDeviceInfo parse(String in) {
        try {
            JSONObject object = new JSONObject(in);

            String imei = object.getString("imei");
            String mUserId = object.getString("mUserId");
            String mNickName = object.getString("mNickName");
            String mModel = object.getString("mModel");
            String mChip = object.getString("mChip");
            String mSize = object.getString("mSize");

            return new PhoneDeviceInfo(imei,
                    mUserId,
                    mNickName,
                    mModel,
                    mChip,
                    mSize);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
