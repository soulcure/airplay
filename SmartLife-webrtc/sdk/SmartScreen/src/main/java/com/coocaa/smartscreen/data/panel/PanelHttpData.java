package com.coocaa.smartscreen.data.panel;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: yuzhan
 */
public class PanelHttpData implements Serializable {

    public int code;
    public String msg;
    public PanelContent data;

    public static class PanelContent implements Serializable{
        public List<PanelBean> content;
    }
}
