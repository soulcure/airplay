package com.coocaa.publib.data.local;

import android.os.Parcelable;

import java.io.Serializable;
import java.sql.Date;


public abstract class MediaData implements Parcelable, Serializable, Comparable<MediaData> {
    public MediaData.TYPE type;
    public long id = 0;
    public long parentID = 0;
    public String tittle=null;
    public long size = 0;
    public Date takeTime;
    public String url;

    //是否选中标记位
    public boolean isCheck = false;
	
	public abstract String getMedioData (String uri);
	public abstract String getURI (String addr, int port);

	public enum TYPE{
	    VIDEO,
        IMAGE
    }

    @Override
    public int compareTo(MediaData o) {
	    try {
	        if(this.takeTime.equals(o.takeTime))
	            return 0;
	        return this.takeTime.after(o.takeTime) ? -1 : 1;
        } catch (Exception e) {
	        e.printStackTrace();
        }
        return 0;
    }

    @Override
    public String toString() {
        return "MediaData{" +
                "type=" + type +
                ", id=" + id +
                ", parentID=" + parentID +
                ", tittle='" + tittle + '\'' +
                ", size=" + size +
                ", takeTime=" + takeTime +
                ", isCheck=" + isCheck +
                '}';
    }
}
