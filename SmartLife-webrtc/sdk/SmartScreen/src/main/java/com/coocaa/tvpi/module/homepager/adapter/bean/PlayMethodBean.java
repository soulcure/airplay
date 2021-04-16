package com.coocaa.tvpi.module.homepager.adapter.bean;

import com.coocaa.smartscreen.data.action.NewAction;

import java.io.Serializable;
import java.util.Map;

public class PlayMethodBean implements Serializable {
    public String title;
    public String subTitle;
    public String poster;
    public float scale;
    public Map<String, String> detail;

    //跳转参数
    public NewAction action;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PlayMethodBean{");
        sb.append("title='").append(title).append('\'');
        sb.append(", subTitle='").append(subTitle).append('\'');
        sb.append(", poster='").append(poster).append('\'');
        sb.append(", scale=").append(scale);
        sb.append(", detail=").append(detail);
        sb.append(", action=").append(action);
        sb.append('}');
        return sb.toString();
    }
}
