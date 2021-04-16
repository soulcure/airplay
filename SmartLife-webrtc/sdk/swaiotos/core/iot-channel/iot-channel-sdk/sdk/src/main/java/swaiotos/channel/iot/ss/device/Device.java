package swaiotos.channel.iot.ss.device;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import swaiotos.channel.iot.ss.controller.DeviceState;

public class Device<T extends DeviceInfo> implements Parcelable {
    private String mLsid;
    private T mInfo = null;
    private DeviceState mDeviceState;
    private int status;                     // 上下线状态 0:下线 1:上线
    private int isTemp;                     //0:默认设备  1：临时设备
    private String roomId;                  //房间号
    private int bleStatus;                  //0 蓝牙围栏外 1 蓝牙围栏内
    private String zpRegisterType;          //设备类型  pad  tv
    private String merchantName;            //商家名称
    private String merchantIcon;            //商家图标
    private String spaceName;               //空间名称
    private String merchantId;              //商家ID
    private String spaceId;                 //空间ID
    private String merchantCoverPhoto;      //商家Cover
    private long lastConnectTime;           //最后连接时间
    private String merchantNameAlias;       //商家简称

    public Device() {

    }

    public Device(String lsid, T info, DeviceState deviceState, int status) {
        this.mLsid = lsid;
        this.mInfo = info;
        this.mDeviceState = deviceState;
        this.status = status;
        this.isTemp = 0;
    }

    public Device(String lsid, T info, DeviceState deviceState, int status,
                  int isTemp, String roomId, String zpRegisterType) {
        this.mLsid = lsid;
        this.mInfo = info;
        this.mDeviceState = deviceState;
        this.status = status;
        this.isTemp = isTemp;
        this.roomId = roomId;
        this.zpRegisterType = zpRegisterType;
    }

    public Device(String lsid, T info, DeviceState deviceState, int status,
                  int isTemp, String roomId, String zpRegisterType, String merchantName,
                  String merchantIcon,String spaceName,String merchantId,String spaceId) {
        this.mLsid = lsid;
        this.mInfo = info;
        this.mDeviceState = deviceState;
        this.status = status;
        this.isTemp = isTemp;
        this.roomId = roomId;
        this.zpRegisterType = zpRegisterType;
        this.merchantName = merchantName;
        this.merchantIcon = merchantIcon;
        this.spaceName = spaceName;
        this.merchantId = merchantId;
        this.spaceId = spaceId;
    }

    public Device(String lsid, T info, DeviceState deviceState, int status,
                  int isTemp, String roomId, String zpRegisterType, String merchantName,
                  String merchantIcon,String spaceName,String merchantId,String spaceId,String merchantCoverPhoto) {
        this.mLsid = lsid;
        this.mInfo = info;
        this.mDeviceState = deviceState;
        this.status = status;
        this.isTemp = isTemp;
        this.roomId = roomId;
        this.zpRegisterType = zpRegisterType;
        this.merchantName = merchantName;
        this.merchantIcon = merchantIcon;
        this.spaceName = spaceName;
        this.merchantId = merchantId;
        this.spaceId = spaceId;
        this.merchantCoverPhoto = merchantCoverPhoto;
    }

    public Device(String lsid, T info, DeviceState deviceState, int status,
                  int isTemp, String roomId, String zpRegisterType, String merchantName,
                  String merchantIcon,String spaceName,String merchantId,String spaceId,String merchantCoverPhoto,long lastConnectTime) {
        this.mLsid = lsid;
        this.mInfo = info;
        this.mDeviceState = deviceState;
        this.status = status;
        this.isTemp = isTemp;
        this.roomId = roomId;
        this.zpRegisterType = zpRegisterType;
        this.merchantName = merchantName;
        this.merchantIcon = merchantIcon;
        this.spaceName = spaceName;
        this.merchantId = merchantId;
        this.spaceId = spaceId;
        this.merchantCoverPhoto = merchantCoverPhoto;
        this.lastConnectTime = lastConnectTime;
    }

    public Device(String lsid,
                  T info,
                  DeviceState deviceState,
                  int status,
                  int isTemp,
                  String roomId,
                  String zpRegisterType,
                  String merchantName,
                  String merchantIcon,
                  String spaceName,
                  String merchantId,
                  String spaceId,
                  String merchantCoverPhoto,
                  long lastConnectTime,
                  String merchantNameAlias) {
        this.mLsid = lsid;
        this.mInfo = info;
        this.mDeviceState = deviceState;
        this.status = status;
        this.isTemp = isTemp;
        this.roomId = roomId;
        this.zpRegisterType = zpRegisterType;
        this.merchantName = merchantName;
        this.merchantIcon = merchantIcon;
        this.spaceName = spaceName;
        this.merchantId = merchantId;
        this.spaceId = spaceId;
        this.merchantCoverPhoto = merchantCoverPhoto;
        this.lastConnectTime = lastConnectTime;
        this.merchantNameAlias = merchantNameAlias;
    }

    public Device(Parcel in) {
        mLsid = in.readString();
        int info = in.readInt();
        if (info > 0) {
            String infoJson = in.readString();
            if (!TextUtils.isEmpty(infoJson)) {
                if (infoJson.contains("swaiotos.channel.iot.ss.device.PhoneDeviceInfo")) {
                    mInfo = (T) PhoneDeviceInfo.parse(infoJson);
                } else if (infoJson.contains("swaiotos.channel.iot.ss.device.TVDeviceInfo")) {
                    mInfo = (T) TVDeviceInfo.parse(infoJson);
                } else if (infoJson.contains("swaiotos.channel.iot.ss.device.PadDeviceInfo")) {
                    mInfo = (T) PadDeviceInfo.parse(infoJson);
                } else if (infoJson.contains("swaiotos.channel.iot.ss.device.RobotDeviceInfo")) {
                    mInfo = (T) RobotDeviceInfo.parse(infoJson);
                }
            } else {
                mInfo = null;
            }
        } else {
            mInfo = null;
        }
        int state = in.readInt();
        if (state > 0) {
            String stateJson = in.readString();
            if (!TextUtils.isEmpty(stateJson))
                mDeviceState = DeviceState.parse(stateJson);//JSONObject.parseObject(stateJson, DeviceState.class);
        }
        status = in.readInt();
        isTemp = in.readInt();
        roomId = in.readString();
        bleStatus = in.readInt();
        zpRegisterType = in.readString();
        merchantName = in.readString();
        merchantIcon = in.readString();
        spaceName = in.readString();
        merchantId = in.readString();
        spaceId = in.readString();
        merchantCoverPhoto = in.readString();
        lastConnectTime = in.readLong();
        merchantNameAlias = in.readString();
    }

    public void parse(String str) {
        try {
            JSONObject jsonObject = new JSONObject(str);
            mLsid = jsonObject.getString("id");
            status = jsonObject.getInt("status");
            String stateJson = jsonObject.getString("state");
            if (!TextUtils.isEmpty(stateJson))
                mDeviceState = DeviceState.parse(stateJson);
            String infoJson = jsonObject.getString("info");
            if (!TextUtils.isEmpty(infoJson)) {
                if (infoJson.contains("swaiotos.channel.iot.ss.device.PhoneDeviceInfo")) {
                    mInfo = (T) PhoneDeviceInfo.parse(infoJson);
                } else if (infoJson.contains("swaiotos.channel.iot.ss.device.TVDeviceInfo")) {
                    mInfo = (T) TVDeviceInfo.parse(infoJson);
                } else if (infoJson.contains("swaiotos.channel.iot.ss.device.PadDeviceInfo")) {
                    mInfo = (T) PadDeviceInfo.parse(infoJson);
                } else if (infoJson.contains("swaiotos.channel.iot.ss.device.RobotDeviceInfo")) {
                    mInfo = (T) RobotDeviceInfo.parse(infoJson);
                }
            } else {
                mInfo = null;
            }

            isTemp = jsonObject.optInt("isTemp");
            roomId = jsonObject.optString("roomId");
            bleStatus = jsonObject.optInt("bleStatus");
            zpRegisterType = jsonObject.optString("zpRegisterType");

            merchantName = jsonObject.optString("merchantName");
            merchantIcon = jsonObject.optString("merchantIcon");
            spaceName = jsonObject.optString("spaceName");
            merchantId = jsonObject.optString("merchantId");
            spaceId = jsonObject.optString("spaceId");
            merchantCoverPhoto = jsonObject.optString("merchantCoverPhoto");
            lastConnectTime = jsonObject.optLong("lastConnectTime");
            merchantNameAlias = jsonObject.optString("merchantNameAlias");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getLsid() {
        return mLsid;
    }

    public void setLsid(String sid) {
        this.mLsid = sid;
    }

    public T getInfo() {
        return mInfo;
    }

    public void setInfo(T mInfo) {
        this.mInfo = mInfo;
    }

    public DeviceState getDeviceState() {
        return mDeviceState;
    }

    public void setDeviceState(DeviceState mDeviceState) {
        this.mDeviceState = mDeviceState;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isTempDevice() {
        return isTemp == 1;
    }

    public int getIsTemp() {
        return isTemp;
    }

    public void setIsTemp(int isTemp) {
        this.isTemp = isTemp;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getBleStatus() {
        return bleStatus;
    }

    public void setBleStatus(int bleStatus) {
        this.bleStatus = bleStatus;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantIcon() {
        return merchantIcon;
    }

    public void setMerchantIcon(String merchantIcon) {
        this.merchantIcon = merchantIcon;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getMerchantCoverPhoto() {
        return merchantCoverPhoto;
    }

    public void setMerchantCoverPhoto(String merchantCoverPhoto) {
        this.merchantCoverPhoto = merchantCoverPhoto;
    }

    public String getZpRegisterType() {
        if (isTemp == 1) {
            zpRegisterType = "dongle";
        }
        return zpRegisterType;
    }

    public void setZpRegisterType(String zpRegisterType) {
        this.zpRegisterType = zpRegisterType;
    }

    public long getLastConnectTime() {
        return lastConnectTime;
    }

    public void setLastConnectTime(long lastConnectTime) {
        this.lastConnectTime = lastConnectTime;
    }

    public String getMerchantNameAlias() {
        return merchantNameAlias;
    }

    public void setMerchantNameAlias(String merchantNameAlias) {
        this.merchantNameAlias = merchantNameAlias;
    }

    public final String encode() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", mLsid);
            if (mInfo != null)
                object.put("info", mInfo.encode());
            if (mDeviceState != null)
                object.put("state", mDeviceState.encode());
            object.put("status", status);
            object.put("isTemp", isTemp);
            object.put("roomId", TextUtils.isEmpty(roomId) ? "" : roomId);
            object.put("bleStatus", bleStatus);
            object.put("zpRegisterType", TextUtils.isEmpty(zpRegisterType) ? "" : zpRegisterType);
            object.put("merchantName", TextUtils.isEmpty(merchantName) ? "" : merchantName);
            object.put("merchantIcon", TextUtils.isEmpty(merchantIcon) ? "" : merchantIcon);
            object.put("spaceName", TextUtils.isEmpty(spaceName) ? "" : spaceName);
            object.put("merchantId", TextUtils.isEmpty(merchantId) ? "" : merchantId);
            object.put("spaceId", TextUtils.isEmpty(spaceId) ? "" : spaceId);
            object.put("merchantCoverPhoto", TextUtils.isEmpty(merchantCoverPhoto) ? "" : merchantCoverPhoto);
            object.put("lastConnectTime",lastConnectTime);
            object.put("merchantNameAlias",TextUtils.isEmpty(merchantNameAlias) ? "" : merchantNameAlias);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Device device = (Device) o;
        return mLsid.equals(device.mLsid);
    }

    @Override
    public int hashCode() {
        return mLsid.hashCode();
    }

    @Override
    public String toString() {
        return encode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mLsid);
        dest.writeInt(mInfo != null ? 1 : 0);
        if (mInfo != null) {
            dest.writeString(mInfo.encode());
        }
        dest.writeInt(mDeviceState != null ? 1 : 0);
        if (mDeviceState != null) {
            dest.writeString(mDeviceState.encode());
        }
        dest.writeInt(status);
        dest.writeInt(isTemp);
        dest.writeString(TextUtils.isEmpty(roomId)              ? "" : roomId);
        dest.writeInt(bleStatus);
        dest.writeString(TextUtils.isEmpty(zpRegisterType)      ? "" : zpRegisterType);
        dest.writeString(TextUtils.isEmpty(merchantName)        ? "" : merchantName);
        dest.writeString(TextUtils.isEmpty(merchantIcon)        ? "" : merchantIcon);
        dest.writeString(TextUtils.isEmpty(spaceName)           ? "" : spaceName);
        dest.writeString(TextUtils.isEmpty(merchantId)          ? "" : merchantId);
        dest.writeString(TextUtils.isEmpty(spaceId)             ? "" : spaceId);
        dest.writeString(TextUtils.isEmpty(merchantCoverPhoto)  ? "" : merchantCoverPhoto);
        dest.writeLong(lastConnectTime);
        dest.writeString(TextUtils.isEmpty(merchantNameAlias)   ? "" : merchantNameAlias);
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel source) {
            return new Device(source);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };
}
