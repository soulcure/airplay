package com.coocaa.tvpi.module.pay.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class CCPayResp implements Parcelable {

    public String type;


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PayResp{");
        sb.append("type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
    }

    public CCPayResp() {
    }

    protected CCPayResp(Parcel in) {
        this.type = in.readString();
    }

    public static final Creator<CCPayResp> CREATOR = new Creator<CCPayResp>() {
        @Override
        public CCPayResp createFromParcel(Parcel source) {
            return new CCPayResp(source);
        }

        @Override
        public CCPayResp[] newArray(int size) {
            return new CCPayResp[size];
        }
    };
}
