package swaiotos.channel.iot.ss.session;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.ss.session
 * @ClassName: RoomDevice
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/10/25 13:54
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/10/25 13:54
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class RoomDevice implements Parcelable {
    private String sid;
    private String property;
    private int isHost;

    public RoomDevice() {

    }

    protected RoomDevice(Parcel in) {
        sid = in.readString();
        property = in.readString();
        isHost = in.readInt();
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public int getIsHost() {
        return isHost;
    }

    public void setIsHost(int isHost) {
        this.isHost = isHost;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sid);
        dest.writeString(property);
        dest.writeInt(isHost);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RoomDevice> CREATOR = new Creator<RoomDevice>() {
        @Override
        public RoomDevice createFromParcel(Parcel in) {
            return new RoomDevice(in);
        }

        @Override
        public RoomDevice[] newArray(int size) {
            return new RoomDevice[size];
        }
    };
}
