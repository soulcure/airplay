package com.coocaa.tvpi.module.web;

import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;


@Entity(tableName = "web_record", indices = {@Index(value="webUrl", unique=true)})
public class WebRecordBean implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    private String title;
    private String content;
    private String imageUrl;
    @ColumnInfo(name="webUrl")
    private String webUrl;

    @Ignore
    private transient boolean isShow;
    @Ignore
    private transient boolean isSelect;

    public WebRecordBean(String title, String content, String imageUrl, String webUrl) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.webUrl = webUrl;
        Log.d("SmartBrowserInfo",  "new WebBean : " + this);
    }

    public void set(WebRecordBean bean) {
        this.title = bean.title;
        this.content = bean.content;
        this.imageUrl = bean.imageUrl;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

//    @Override
//    public String toString() {
//        return "WebRecordBean{" +
//                "title='" + title + '\'' +
//                ", url='" + Md5Utils.getMD5(webUrl) +
//                ", icon='" + imageUrl + '\'' +
//                "}";
//    }


    @Override
    public String toString() {
        return "WebRecordBean{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", webUrl='" + webUrl + '\'' +
                "}\n";
    }
}
