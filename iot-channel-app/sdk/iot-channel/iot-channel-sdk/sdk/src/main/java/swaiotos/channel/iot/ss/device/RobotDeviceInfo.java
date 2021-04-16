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
public class RobotDeviceInfo extends DeviceInfo {
    private String deviceId;
    private String nickName;
    private String model;

    public RobotDeviceInfo(String activeId,
                           String nickName,
                           String model) {
        this.deviceId = activeId;
        this.nickName = nickName;
        this.model = model;

    }

    public RobotDeviceInfo() {
    }

    RobotDeviceInfo(Parcel source) {
        this.deviceId = source.readString();
        this.nickName = source.readString();
        this.model = source.readString();
    }

    @Override
    public TYPE type() {
        return TYPE.THIRD;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceId);
        dest.writeString(nickName);
        dest.writeString(model);
    }

    public static final Creator<RobotDeviceInfo> CREATOR = new Creator<RobotDeviceInfo>() {
        @Override
        public RobotDeviceInfo createFromParcel(Parcel source) {
            return new RobotDeviceInfo(source);
        }

        @Override
        public RobotDeviceInfo[] newArray(int size) {
            return new RobotDeviceInfo[size];
        }
    };

    @Override
    public String encode() {
        JSONObject object = new JSONObject();
        try {
            object.put("deviceId", deviceId);
            object.put("nickName", nickName);
            object.put("model", model);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public static RobotDeviceInfo parse(String in) {
        try {
            JSONObject object = new JSONObject(in);
            String deviceId = object.getString("deviceId");
            String nickName = object.getString("nickName");
            String model = object.getString("model");
            return new RobotDeviceInfo(deviceId,
                    nickName,
                    model);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getNickName() {
        return nickName;
    }

    public String getModel() {
        return model;
    }
}
