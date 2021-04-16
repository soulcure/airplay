package com.coocaa.smartsdk.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * @Author: yuzhan
 */
public class IUserInfo implements Serializable, Parcelable {
    public String nickName;
    public String mobile;
    public String avatar;
    public String open_id;
    public String accessToken;
    public String tp_token;

    public IUserInfo() {

    }

    protected IUserInfo(Parcel in) {
        nickName = in.readString();
        mobile = in.readString();
        avatar = in.readString();
        open_id = in.readString();
        accessToken = in.readString();
        tp_token = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nickName);
        dest.writeString(mobile);
        dest.writeString(avatar);
        dest.writeString(open_id);
        dest.writeString(accessToken);
        dest.writeString(tp_token);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<IUserInfo> CREATOR = new Creator<IUserInfo>() {
        @Override
        public IUserInfo createFromParcel(Parcel in) {
            return new IUserInfo(in);
        }

        @Override
        public IUserInfo[] newArray(int size) {
            return new IUserInfo[size];
        }
    };

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("IUserInfo{");
        sb.append("nickName='").append(nickName).append('\'');
        sb.append(", mobile='").append(mobile).append('\'');
        sb.append(", avatar='").append(avatar).append('\'');
        sb.append(", open_id='").append(open_id).append('\'');
        sb.append(", accessToken='").append(accessToken).append('\'');
        sb.append(", tp_token='").append(tp_token).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
