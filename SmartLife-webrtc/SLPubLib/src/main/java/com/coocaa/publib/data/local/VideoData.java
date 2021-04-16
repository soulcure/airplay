package com.coocaa.publib.data.local;

import android.os.Parcel;

import java.io.Serializable;

public class VideoData extends MediaData implements Serializable{

    public long duration = 0;
    public String resolution = "448x336";
    public String bucketName;
    public String thumbnailPath;

    public String getMedioData(String uri) {
        StringBuffer medioData = new StringBuffer();
        medioData.append("<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" ")
                .append("xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" ")
                .append("xmlns:dc=\"http://purl.org/dc/elements/1.1/\">").append("<item id=\"")
                .append(String.valueOf(id)).append("\" ").append("parentID=\"")
                .append(String.valueOf(parentID)).append("\" ").append("restricted=\"false\">")
                .append("<dc:title>").append(tittle).append("</dc:title>")
                .append("<dc:creator>SKYWORTH</dc:creator>")
                .append("<upnp:class>object.item.videoItem</upnp:class>")
                .append("<res protocolInfo=\"http-get:*:video/x-flv:*\" ").append("size=\"")
                .append(String.valueOf(size)).append("\" ").append("duration=\"").append(duration)
                .append("\" ").append("resolution=\"").append(resolution).append("\">").append(uri)
                .append("</res>").append("</item>").append("</DIDL-Lite>");
        return medioData.toString();
    }

    public String getURI(String addr, int port) {
        StringBuffer uri = new StringBuffer();
        uri.append("http://").append(addr).append(":").append(port).append("/r?id=")
                .append(String.valueOf(id)).append("&type=video");
        return uri.toString();
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.parentID);
        dest.writeString(this.tittle);
        dest.writeLong(this.size);

        dest.writeLong(this.duration);
        dest.writeString(this.resolution);
        dest.writeString(this.url);
        dest.writeString(this.thumbnailPath);
        dest.writeString(this.bucketName);
    }

    public static final Creator<VideoData> CREATOR = new Creator<VideoData>() {

        @Override
        public VideoData[] newArray(int size) {
            // TODO Auto-generated method stub
            return new VideoData[size];
        }

        @Override
        public VideoData createFromParcel(Parcel source) {
            // TODO Auto-generated method stub
            VideoData data = new VideoData();
            data.id = source.readLong();
            data.parentID = source.readLong();
            data.tittle = source.readString();
            data.size = source.readLong();

            data.duration = source.readLong();
            data.resolution = source.readString();
            data.url = source.readString();
            data.thumbnailPath = source.readString();
            data.bucketName = source.readString();
            return data;
        }
    };

    @Override
    public String toString() {
        return "VideoData{" +
                "duration=" + duration +
                ", url='" + url + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", thumbnailPath='" + thumbnailPath + '\'' +
                ", takeTime=" + takeTime +
                ", isCheck=" + isCheck +
                ", resolution='" + resolution + '\'' +
                ", url='" + url + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", thumbnailPath='" + thumbnailPath + '\'' +
                '}';
    }
}
