package com.coocaa.smartscreen.data.account;

import java.io.Serializable;

public class UpdateAvatarBean implements Serializable {
    public String size;//需要的头像尺寸大小目前有：50,70,200,250,370,500,800
    public String url;

    public UpdateAvatarBean(String size, String url) {
        this.size = size;
        this.url = url;
    }

    @Override
    public String toString() {
        return "UpdateAvatarBean{" +
                "size='" + size + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
