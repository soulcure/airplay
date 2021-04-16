package swaiotos.channel.iot.common.lsid;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * @ClassName: LSID
 * @Author: lu
 * @CreateDate: 2020/4/8 5:28 PM
 * @Description:
 */
public class LSID implements Parcelable {

    public String lsid;
    public String token;
    public String tempCode;
    public String roomId;

    public LSID(String lsid, String token) {
        this.lsid = lsid;
        this.token = token;
    }

    public LSID(String lsid, String token, String tempCode, String roomId) {
        this.lsid = lsid;
        this.token = token;
        this.tempCode = tempCode;
        this.roomId = roomId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.lsid);
        dest.writeString(this.token);
        dest.writeString(this.tempCode);
        dest.writeString(this.roomId);
    }

    protected LSID(Parcel in) {
        this.lsid = in.readString();
        this.token = in.readString();
        this.tempCode = in.readString();
        this.roomId = in.readString();
    }

    public static final Creator<LSID> CREATOR = new Creator<LSID>() {
        @Override
        public LSID createFromParcel(Parcel source) {
            return new LSID(source);
        }

        @Override
        public LSID[] newArray(int size) {
            return new LSID[size];
        }
    };
}
