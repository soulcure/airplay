package com.coocaa.smartscreen.data.businessstate;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * @ClassName BusinessStateBean
 * @Description TODO (write something)
 * @User heni
 * @Date 2020/12/23
 */
public class SceneConfigBean implements Serializable, Parcelable {
    /**
     * id : 场景类型标注，包名+唯一类型
     * titleFormat : 标题格式，正在播放[user]分享的影片[title]、[user]正在玩抽扑克
     * subTitle : 子标题,业务自定义
     * appletName : 小程序的名称，类似金刚区的标题
     * appletUri : 遥控跳转进入小程序uri
     * appletIcon : 小程序的图标
     * contentType : 遥控内容类型,np: native,h5: web页面
     * contentUrl : 遥控内容页面地址，web页面，np的类名
     * scale : 小程序对应控制页面UI宽高比
     */
    public String id;
    public String titleFormat;
    public String subTitle;
    public String appletName;
    public String appletUri;
    public String appletIcon;
    public String contentType;
    public String contentUrl;
    public float scale;

    public SceneConfigBean() {

    }

    protected SceneConfigBean(Parcel in) {
        id = in.readString();
        titleFormat = in.readString();
        subTitle = in.readString();
        appletName = in.readString();
        appletUri = in.readString();
        appletIcon = in.readString();
        contentType = in.readString();
        contentUrl = in.readString();
        scale = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(titleFormat);
        dest.writeString(subTitle);
        dest.writeString(appletName);
        dest.writeString(appletUri);
        dest.writeString(appletIcon);
        dest.writeString(contentType);
        dest.writeString(contentUrl);
        dest.writeFloat(scale);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SceneConfigBean> CREATOR = new Creator<SceneConfigBean>() {
        @Override
        public SceneConfigBean createFromParcel(Parcel in) {
            return new SceneConfigBean(in);
        }

        @Override
        public SceneConfigBean[] newArray(int size) {
            return new SceneConfigBean[size];
        }
    };

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
