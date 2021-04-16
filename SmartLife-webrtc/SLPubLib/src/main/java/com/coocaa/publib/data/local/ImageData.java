/**
 * Copyright (C) 2013 The SkyTvOS Project
 *
 * Version     Date           Author
 * ─────────────────────────────────────
 *           2013-7-5         hq
 *
 */

package com.coocaa.publib.data.local;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageData extends MediaData implements Parcelable
{

    public String resolution = "448x336";
    public String thumb;
    public String bucketName;

    @Override
    public String getMedioData(String uri)
    {
        StringBuffer medioData = new StringBuffer();
        medioData.append("<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" ")
                .append("xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" ")
                .append("xmlns:dc=\"http://purl.org/dc/elements/1.1/\">").append("<item id=\"")
                .append(String.valueOf(id)).append("\" ").append("parentID=\"")
                .append(String.valueOf(parentID)).append("\" ").append("restricted=\"false\">")
                .append("<dc:title>").append(tittle).append("</dc:title>")
                .append("<dc:creator>SKYWORTH</dc:creator>")
                .append("<upnp:class>object.item.imageItem</upnp:class>")
                .append("<res protocolInfo=\"http-get:*:image/jpeg:*\" ").append("size=\"")
                .append(String.valueOf(size)).append("\">").append(uri).append("</res>")
                .append("</item>").append("</DIDL-Lite>");
        return medioData.toString();
    }

    @Override
    public String getURI(String addr, int port)
    {
        StringBuffer uri = new StringBuffer();
        uri.append("http://").append(addr).append(":").append(port).append("/r?id=")
                .append(String.valueOf(id)).append("&type=image");
        return uri.toString();
    }

    @Override
    public int describeContents()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(this.id);
        dest.writeLong(this.parentID);
        dest.writeString(this.tittle);
        dest.writeLong(this.size);
        dest.writeString(this.resolution);
        dest.writeString(this.url);

    }

    public static final Creator<ImageData> CREATOR = new Creator<ImageData>()
    {

        @Override
        public ImageData[] newArray(int size)
        {
            // TODO Auto-generated method stub
            return new ImageData[size];
        }

        @Override
        public ImageData createFromParcel(Parcel source)
        {
            ImageData data = new ImageData();
            data.id = source.readLong();
            data.parentID = source.readLong();
            data.tittle = source.readString();
            data.size = source.readLong();
            data.resolution = source.readString();
            data.url = source.readString();
            return data;
        }
    };

    @Override
    public String toString() {
        return "ImageData{" +
                "resolution='" + resolution + '\'' +
                ", data='" + url + '\'' +
                ", thumb='" + thumb + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", takeTime=" + takeTime +
                ", isCheck=" + isCheck +
                '}';
    }
}
