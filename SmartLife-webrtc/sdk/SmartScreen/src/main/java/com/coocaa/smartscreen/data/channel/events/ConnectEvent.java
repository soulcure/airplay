package com.coocaa.smartscreen.data.channel.events;

import android.os.Parcel;
import android.os.Parcelable;

import swaiotos.channel.iot.ss.device.Device;

/**
 * @ClassName ConnectEvent
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/4/15
 * @Version TODO (write something)
 */
public class ConnectEvent implements Parcelable {
    public boolean isConnected;
    public String msg;
    public Device device;

    public ConnectEvent(boolean isConnected, Device device, String msg) {
        this.isConnected = isConnected;
        this.device = device;
        this.msg = msg;
    }

    protected ConnectEvent(Parcel in) {
        isConnected = in.readByte() != 0;
        msg = in.readString();
        device = in.readParcelable(Device.class.getClassLoader());
    }

    public static final Creator<ConnectEvent> CREATOR = new Creator<ConnectEvent>() {
        @Override
        public ConnectEvent createFromParcel(Parcel in) {
            return new ConnectEvent(in);
        }

        @Override
        public ConnectEvent[] newArray(int size) {
            return new ConnectEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (isConnected ? 1 : 0));
        parcel.writeString(msg);
        parcel.writeParcelable(device, i);
    }

    @Override
    public String toString() {
        return "ConnectEvent{" +
                "isConnected=" + isConnected +
                ", msg='" + msg + '\'' +
                ", device=" + device +
                '}';
    }
}
