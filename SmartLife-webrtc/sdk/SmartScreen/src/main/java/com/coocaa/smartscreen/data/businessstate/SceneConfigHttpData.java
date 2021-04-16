package com.coocaa.smartscreen.data.businessstate;

import com.coocaa.smartscreen.data.panel.PanelBean;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: AwenZeng
 */
public class SceneConfigHttpData implements Serializable {
    public int code;
    public String msg;
    public SceneConfigContent data;

    public static class SceneConfigContent implements Serializable{
        public List<SceneConfigBean> content;
    }
}
