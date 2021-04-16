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

public class AudioData extends MediaData
{
    public long duration = 0;
    public String singer;
    public int albumId;

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
                .append("<upnp:class>object.item.audioItem.musicTrack</upnp:class>")
                .append("<res protocolInfo=\"http-get:*:audio/x-mp3:*\" ").append("size=\"")
                .append(String.valueOf(size)).append("\" ").append("duration=\"").append(duration)
                .append("\">").append(uri).append("</res>").append("</item>")
                .append("</DIDL-Lite>");
        return medioData.toString();
    }

    @Override
    public String getURI(String addr, int port)
    {
        StringBuffer uri = new StringBuffer();
        uri.append("http://").append(addr).append(":").append(port).append("/r?id=")
                .append(String.valueOf(id)).append("&type=audio");
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
        dest.writeLong(this.duration);
        dest.writeString(this.url);
        dest.writeString(this.singer);

    }

    public static final Creator<AudioData> CREATOR = new Creator<AudioData>()
    {

        @Override
        public AudioData[] newArray(int size)
        {
            // TODO Auto-generated method stub
            return new AudioData[size];
        }

        @Override
        public AudioData createFromParcel(Parcel source)
        {
            AudioData data = new AudioData();
            data.id = source.readLong();
            data.parentID = source.readLong();
            data.tittle = source.readString();
            data.size = source.readLong();
            data.duration=source.readLong();
            data.url= source.readString();
            data.singer=source.readString();
            return data;
        }
    };

}
