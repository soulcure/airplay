package com.coocaa.smartscreen.data.channel.events;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @ClassName UnbindEvent
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/5/11
 * @Version TODO (write something)
 */
public class UnbindEvent implements Parcelable {
    public String lsid;
    public boolean isUnbinded;
    public String errorType;
    public String msg;

    public UnbindEvent(String lsid, boolean isUnbinded, String errorType, String msg) {
        this.lsid = lsid;
        this.isUnbinded = isUnbinded;
        this.errorType = errorType;
        this.msg = msg;
    }

    protected UnbindEvent(Parcel in) {
        lsid = in.readString();
        isUnbinded = in.readByte() != 0;
        errorType = in.readString();
        msg = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(lsid);
        dest.writeByte((byte) (isUnbinded ? 1 : 0));
        dest.writeString(errorType);
        dest.writeString(msg);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UnbindEvent> CREATOR = new Creator<UnbindEvent>() {
        @Override
        public UnbindEvent createFromParcel(Parcel in) {
            return new UnbindEvent(in);
        }

        @Override
        public UnbindEvent[] newArray(int size) {
            return new UnbindEvent[size];
        }
    };
}
