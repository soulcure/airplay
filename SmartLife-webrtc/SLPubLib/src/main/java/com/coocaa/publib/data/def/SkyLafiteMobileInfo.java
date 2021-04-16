package com.coocaa.publib.data.def;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dengxiuzhen on 2016/9/27.
 */
public class SkyLafiteMobileInfo implements Parcelable {
    private String type;
    private Object content;

    public SkyLafiteMobileInfo(){}


    protected SkyLafiteMobileInfo(Parcel in) {
        type = in.readString();
        content = in.readValue(Object.class.getClassLoader());
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeValue(content);
    }

    public static final Creator<SkyLafiteMobileInfo> CREATOR = new Creator<SkyLafiteMobileInfo>() {
        @Override
        public SkyLafiteMobileInfo createFromParcel(Parcel source) {
            return new SkyLafiteMobileInfo(source);
        }

        @Override
        public SkyLafiteMobileInfo[] newArray(int size) {
            return new SkyLafiteMobileInfo[size];
        }
    };

    @Override
    public String toString() {
        return "type: " + type + "\n"
                + "content: " + content;
    }
}
