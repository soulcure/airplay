package com.coocaa.smartscreen.data.banner;

import com.coocaa.smartscreen.data.function.FunctionBean;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: yuzhan
 */
public class BannerHttpData implements Serializable{
    public int code;
    public String msg;
    public FunctionContent data;

    public static class FunctionContent implements Serializable {
        public List<FunctionBean> content;
        public String bg;
        public String theme;
        public int style;
    }
}
