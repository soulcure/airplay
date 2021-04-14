package swaiotos.channel.iot.db.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

import swaiotos.channel.iot.entity.PadDeviceInfo;
import swaiotos.channel.iot.entity.PhoneDeviceInfo;
import swaiotos.channel.iot.entity.TVDeviceInfo;
import swaiotos.channel.iot.response.DeviceDataResp;

@Entity
public class Device implements Parcelable {
    //不能用int
    @Id(autoincrement = true)
    private Long id;

    @Unique
    private String zpLsid;

    private String zpNickName;
    private String zpHeadSculpture;
    private String zpRegisterId;
    private String zpChip;
    private String zpModel;
    private String deviceName;
    private String zpRegisterType;
    private String simulring;
    private String friendZpPosition;
    private String zpDeviceJson;
    private String zpAttributeJson;
    private int zpStatus;
    private int isTemp;
    private String roomId;
    private String merchantName;
    private String merchantIcon;
    private String merchantCoverPhoto;
    private String spaceName;
    private String merchantId;
    private String spaceId;

    @Generated(hash = 29159911)
    public Device(Long id, String zpLsid, String zpNickName, String zpHeadSculpture,
                  String zpRegisterId, String zpChip, String zpModel, String deviceName,
                  String zpRegisterType, String simulring, String friendZpPosition,
                  String zpDeviceJson, String zpAttributeJson, int zpStatus, int isTemp,
                  String roomId, String merchantName, String merchantIcon,
                  String merchantCoverPhoto, String spaceName, String merchantId,
                  String spaceId) {
        this.id = id;
        this.zpLsid = zpLsid;
        this.zpNickName = zpNickName;
        this.zpHeadSculpture = zpHeadSculpture;
        this.zpRegisterId = zpRegisterId;
        this.zpChip = zpChip;
        this.zpModel = zpModel;
        this.deviceName = deviceName;
        this.zpRegisterType = zpRegisterType;
        this.simulring = simulring;
        this.friendZpPosition = friendZpPosition;
        this.zpDeviceJson = zpDeviceJson;
        this.zpAttributeJson = zpAttributeJson;
        this.zpStatus = zpStatus;
        this.isTemp = isTemp;
        this.roomId = roomId;
        this.merchantName = merchantName;
        this.merchantIcon = merchantIcon;
        this.merchantCoverPhoto = merchantCoverPhoto;
        this.spaceName = spaceName;
        this.merchantId = merchantId;
        this.spaceId = spaceId;
    }


    public Device(DeviceDataResp.DataBean dataBean) {
        this.zpLsid = dataBean.getSid();
        this.zpHeadSculpture = "";
        this.zpRegisterId = "";
        this.zpChip = "";
        this.zpModel = "";
        this.deviceName = "";
        this.zpRegisterType = dataBean.getDeviceType();
        this.simulring = "";
        this.friendZpPosition = "";
        this.zpDeviceJson = dataBean.getDeviceInfo();
        this.zpAttributeJson = dataBean.getAttributeJson();
        this.zpStatus = dataBean.getStatus();
        this.isTemp = dataBean.getIsTemp();
        this.roomId = dataBean.getRoomId();
        this.merchantName = dataBean.getMerchantName();
        this.merchantIcon = dataBean.getMerchantIcon();
        this.merchantCoverPhoto = dataBean.getMerchantCoverPhoto();
        this.spaceName = dataBean.getSpaceName();
        this.merchantId = dataBean.getMerchantId();
        this.spaceId = dataBean.getSpaceId();
    }


    @Generated(hash = 1469582394)
    public Device() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getZpLsid() {
        return this.zpLsid;
    }

    public void setZpLsid(String zpLsid) {
        this.zpLsid = zpLsid;
    }

    public String getZpNickName() {
        return this.zpNickName;
    }

    public void setZpNickName(String zpNickName) {
        this.zpNickName = zpNickName;
    }

    public String getZpHeadSculpture() {
        return this.zpHeadSculpture;
    }

    public void setZpHeadSculpture(String zpHeadSculpture) {
        this.zpHeadSculpture = zpHeadSculpture;
    }

    public String getZpRegisterId() {
        return this.zpRegisterId;
    }

    public void setZpRegisterId(String zpRegisterId) {
        this.zpRegisterId = zpRegisterId;
    }

    public String getZpChip() {
        return this.zpChip;
    }

    public void setZpChip(String zpChip) {
        this.zpChip = zpChip;
    }

    public String getZpModel() {
        return this.zpModel;
    }

    public void setZpModel(String zpModel) {
        this.zpModel = zpModel;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getZpRegisterType() {
        return this.zpRegisterType;
    }

    public void setZpRegisterType(String zpRegisterType) {
        this.zpRegisterType = zpRegisterType;
    }

    public String getSimulring() {
        return this.simulring;
    }

    public void setSimulring(String simulring) {
        this.simulring = simulring;
    }

    public String getFriendZpPosition() {
        return this.friendZpPosition;
    }

    public void setFriendZpPosition(String friendZpPosition) {
        this.friendZpPosition = friendZpPosition;
    }

    public String getZpDeviceJson() {
        return this.zpDeviceJson;
    }

    public void setZpDeviceJson(String zpDeviceJson) {
        this.zpDeviceJson = zpDeviceJson;
    }

    public String getZpAttributeJson() {
        return this.zpAttributeJson;
    }

    public void setZpAttributeJson(String zpAttributeJson) {
        this.zpAttributeJson = zpAttributeJson;
    }

    public int getZpStatus() {
        return this.zpStatus;
    }

    public void setZpStatus(int zpStatus) {
        this.zpStatus = zpStatus;
    }

    public int getIsTemp() {
        return this.isTemp;
    }

    public boolean isTempDevice() {
        return isTemp == 1;
    }

    public void setIsTemp(int isTemp) {
        this.isTemp = isTemp;
    }

    public String getRoomId() {
        return this.roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getMerchantName() {
        return this.merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantIcon() {
        return this.merchantIcon;
    }

    public void setMerchantIcon(String merchantIcon) {
        this.merchantIcon = merchantIcon;
    }

    public String getMerchantCoverPhoto() {
        return this.merchantCoverPhoto;
    }

    public void setMerchantCoverPhoto(String merchantCoverPhoto) {
        this.merchantCoverPhoto = merchantCoverPhoto;
    }

    public String getSpaceName() {
        return this.spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public String getMerchantId() {
        return this.merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getSpaceId() {
        return this.spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }


    public TVDeviceInfo getTVDeviceInfo() {
        TVDeviceInfo deviceInfo = null;
        if (zpRegisterType.equalsIgnoreCase("tv")) {
            deviceInfo = new TVDeviceInfo(zpDeviceJson);
        }
        return deviceInfo;
    }


    public PhoneDeviceInfo getPhoneDeviceInfo() {
        PhoneDeviceInfo deviceInfo = null;
        if (zpRegisterType.equalsIgnoreCase("phone")) {
            deviceInfo = new PhoneDeviceInfo(zpDeviceJson);
        }
        return deviceInfo;
    }


    public PadDeviceInfo getPadDeviceInfo() {
        PadDeviceInfo deviceInfo = null;
        if (zpRegisterType.equalsIgnoreCase("pad")) {
            deviceInfo = new PadDeviceInfo(zpDeviceJson);
        }
        return deviceInfo;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.zpLsid);
        dest.writeString(this.zpNickName);
        dest.writeString(this.zpHeadSculpture);
        dest.writeString(this.zpRegisterId);
        dest.writeString(this.zpChip);
        dest.writeString(this.zpModel);
        dest.writeString(this.deviceName);
        dest.writeString(this.zpRegisterType);
        dest.writeString(this.simulring);
        dest.writeString(this.friendZpPosition);
        dest.writeString(this.zpDeviceJson);
        dest.writeString(this.zpAttributeJson);
        dest.writeInt(this.zpStatus);
        dest.writeInt(this.isTemp);
        dest.writeString(this.roomId);
        dest.writeString(this.merchantName);
        dest.writeString(this.merchantIcon);
        dest.writeString(this.merchantCoverPhoto);
        dest.writeString(this.spaceName);
        dest.writeString(this.merchantId);
        dest.writeString(this.spaceId);
    }

    protected Device(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.zpLsid = in.readString();
        this.zpNickName = in.readString();
        this.zpHeadSculpture = in.readString();
        this.zpRegisterId = in.readString();
        this.zpChip = in.readString();
        this.zpModel = in.readString();
        this.deviceName = in.readString();
        this.zpRegisterType = in.readString();
        this.simulring = in.readString();
        this.friendZpPosition = in.readString();
        this.zpDeviceJson = in.readString();
        this.zpAttributeJson = in.readString();
        this.zpStatus = in.readInt();
        this.isTemp = in.readInt();
        this.roomId = in.readString();
        this.merchantName = in.readString();
        this.merchantIcon = in.readString();
        this.merchantCoverPhoto = in.readString();
        this.spaceName = in.readString();
        this.merchantId = in.readString();
        this.spaceId = in.readString();
    }

    public static final Parcelable.Creator<Device> CREATOR = new Parcelable.Creator<Device>() {
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
