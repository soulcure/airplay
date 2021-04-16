package com.coocaa.publib.data.local;

import android.os.Parcel;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 2020/10/23
 */
public class DocumentData extends MediaData {

    public long lastModifiedTime;
    public String url;
    public String suffix;//后缀
    public String format;//格式 Word、PPT、PDF、Excel等

    @Override
    public String getMedioData(String uri) {
        return "";
    }

    @Override
    public String getURI(String addr, int port) {
        return "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.parentID);
        dest.writeString(this.tittle);
        dest.writeLong(this.size);
        dest.writeLong(this.lastModifiedTime);
        dest.writeString(this.url);
        dest.writeString(this.suffix);
        dest.writeString(this.format);
    }

    public static final Creator<DocumentData> CREATOR = new Creator<DocumentData>() {

        @Override
        public DocumentData[] newArray(int size) {
            // TODO Auto-generated method stub
            return new DocumentData[size];
        }

        @Override
        public DocumentData createFromParcel(Parcel source) {
            DocumentData data = new DocumentData();
            data.id = source.readLong();
            data.parentID = source.readLong();
            data.tittle = source.readString();
            data.size = source.readLong();
            data.lastModifiedTime = source.readLong();
            data.url = source.readString();
            data.suffix = source.readString();
            data.format = source.readString();
            return data;
        }
    };
}
