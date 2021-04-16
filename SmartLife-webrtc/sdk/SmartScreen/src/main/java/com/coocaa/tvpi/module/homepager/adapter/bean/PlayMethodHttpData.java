package com.coocaa.tvpi.module.homepager.adapter.bean;

import com.coocaa.smartscreen.data.panel.PanelBean;
import com.coocaa.smartscreen.data.panel.PanelHttpData;

import java.io.Serializable;
import java.util.List;

public class PlayMethodHttpData implements Serializable {
    public int code;
    public String msg;
    public PlayMethodHttpContent data;

    public static class PlayMethodHttpContent implements Serializable{
        public List<PlayMethodBean> content;
    }

}
