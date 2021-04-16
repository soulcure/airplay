package swaiotos.channel.iot.ss.device;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @ClassName: TVDeviceInfo
 * @Author: lu
 * @CreateDate: 2020/4/22 2:44 PM
 * @Description:z
 */
public class TVDeviceInfo extends DeviceInfo {
    public String activeId;
    public String deviceName;
    public String mMovieSource;
    public String mChip, mModel, mSize;
    public String cHomepageVersion;
    public String MAC;
    public String cFMode;
    public String cTcVersion;
    public String cPattern;
    public String Resolution;
    public String aSdk;
    public String cEmmcCID;
    public String cBrand;
    public String mNickName;        //电视的昵称
    public int blueSupport;      //是否支持蓝牙


    public TVDeviceInfo(String activeId,
                        String deviceName,
                        String movieSource,
                        String chip,
                        String model,
                        String size,
                        String cHomepageVersion,
                        String MAC,
                        String cFMode,
                        String cTcVersion,
                        String cPattern,
                        String Resolution,
                        String aSdk,
                        String cEmmcCID,
                        String cBrand,
                        String nickName) {
        this.activeId = activeId;
        this.deviceName = deviceName;
        this.mMovieSource = movieSource;
        this.mChip = chip;
        this.mModel = model;
        this.mSize = size;
        this.cHomepageVersion = cHomepageVersion;
        this.MAC = MAC;
        this.cFMode = cFMode;
        this.cTcVersion = cTcVersion;
        this.cPattern = cPattern;
        this.Resolution = Resolution;
        this.aSdk = aSdk;
        this.cEmmcCID = cEmmcCID;
        this.cBrand = cBrand;
        this.mNickName = nickName;
    }

    public TVDeviceInfo(String activeId,
                        String deviceName,
                        String movieSource,
                        String chip,
                        String model,
                        String size,
                        String cHomepageVersion,
                        String MAC,
                        String cFMode,
                        String cTcVersion,
                        String cPattern,
                        String Resolution,
                        String aSdk,
                        String cEmmcCID,
                        String cBrand,
                        String nickName,
                        int blueSupport) {
        this.activeId = activeId;
        this.deviceName = deviceName;
        this.mMovieSource = movieSource;
        this.mChip = chip;
        this.mModel = model;
        this.mSize = size;
        this.cHomepageVersion = cHomepageVersion;
        this.MAC = MAC;
        this.cFMode = cFMode;
        this.cTcVersion = cTcVersion;
        this.cPattern = cPattern;
        this.Resolution = Resolution;
        this.aSdk = aSdk;
        this.cEmmcCID = cEmmcCID;
        this.cBrand = cBrand;
        this.mNickName = nickName;
        this.blueSupport = blueSupport;
    }

    public TVDeviceInfo() {
    }

    TVDeviceInfo(Parcel source) {
        this.activeId = source.readString();
        this.deviceName = source.readString();
        this.mMovieSource = source.readString();
        this.mChip = source.readString();
        this.mModel = source.readString();
        this.mSize = source.readString();
        this.cHomepageVersion = source.readString();
        this.MAC = source.readString();
        this.cFMode = source.readString();
        this.cTcVersion = source.readString();
        this.cPattern = source.readString();
        this.Resolution = source.readString();
        this.aSdk = source.readString();
        this.cEmmcCID = source.readString();
        this.cBrand = source.readString();
        this.mNickName = source.readString();
        this.blueSupport = source.readInt();
    }

    @Override
    public TYPE type() {
        return TYPE.TV;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(activeId);
        dest.writeString(deviceName);
        dest.writeString(mMovieSource);
        dest.writeString(mChip);
        dest.writeString(mModel);
        dest.writeString(mSize);
        dest.writeString(cHomepageVersion);
        dest.writeString(MAC);
        dest.writeString(cFMode);
        dest.writeString(cTcVersion);
        dest.writeString(cPattern);
        dest.writeString(Resolution);
        dest.writeString(aSdk);
        dest.writeString(cEmmcCID);
        dest.writeString(cBrand);
        dest.writeString(mNickName);
        dest.writeInt(blueSupport);
    }


    public static final Creator<TVDeviceInfo> CREATOR = new Creator<TVDeviceInfo>() {
        @Override
        public TVDeviceInfo createFromParcel(Parcel source) {
            return new TVDeviceInfo(source);
        }

        @Override
        public TVDeviceInfo[] newArray(int size) {
            return new TVDeviceInfo[size];
        }
    };

    @Override
    public String encode() {
        JSONObject object = new JSONObject();
        try {
            object.put("activeId", activeId);
            object.put("deviceName", deviceName);
            object.put("mMovieSource", mMovieSource);
            object.put("mChip", mChip);
            object.put("mModel", mModel);
            object.put("mSize", mSize);
            object.put("cHomepageVersion", cHomepageVersion);
            object.put("MAC", MAC);
            object.put("cFMode", cFMode);
            object.put("cTcVersion", cTcVersion);
            object.put("cPattern", cPattern);
            object.put("Resolution", Resolution);
            object.put("aSdk", aSdk);
            object.put("cEmmcCID", cEmmcCID);
            object.put("cBrand", cBrand);
            object.put("mNickName", mNickName);
            object.put("clazzName", clazzName);
            object.put("blueSupport",blueSupport);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public static TVDeviceInfo parse(String in) {
        try {
            JSONObject object = new JSONObject(in);
            String activeId = object.getString("activeId");
            String deviceName = object.getString("deviceName");
            String mMovieSource = object.getString("mMovieSource");
            String mChip = object.getString("mChip");
            String mModel = object.getString("mModel");
            String mSize = object.getString("mSize");
            String cHomepageVersion = object.getString("cHomepageVersion");
            String MAC = object.getString("MAC");
            String cFMode = object.getString("cFMode");
            String cTcVersion = object.getString("cTcVersion");
            String cPattern = object.getString("cPattern");
            String Resolution = object.getString("Resolution");
            String aSdk = object.getString("aSdk");
            String cEmmcCID = object.getString("cEmmcCID");
            String cBrand = object.getString("cBrand");
            String mNickName = object.getString("mNickName");
            int blueSupport = object.getInt("blueSupport");

            return new TVDeviceInfo(activeId,
                    deviceName,
                    mMovieSource,
                    mChip,
                    mModel,
                    mSize,
                    cHomepageVersion,
                    MAC,
                    cFMode,
                    cTcVersion,
                    cPattern,
                    Resolution,
                    aSdk,
                    cEmmcCID,
                    cBrand,
                    mNickName,
                    blueSupport);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
