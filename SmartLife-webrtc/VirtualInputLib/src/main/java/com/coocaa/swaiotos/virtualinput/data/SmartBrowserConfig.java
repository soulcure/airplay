package com.coocaa.swaiotos.virtualinput.data;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.List;

public class SmartBrowserConfig implements Serializable {

    public List<SmartBrowserConfigBean> dataList;

    public static class SmartBrowserConfigBean implements Serializable {
        public String host;
        public String extJs;
        public boolean showVideo;

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }
    }

    @Override
    public String toString() {
        return "SmartBrowserConfig{" +
                "dataList=" + dataList +
                '}';
    }
}
