package com.coocaa.tvpi.view.commondialog;

import java.io.Serializable;

/**
 * Created by IceStorm on 2018/1/12.
 */

public class CommonModel implements Serializable{
    private static final long serialVersionUID = 2000L;

    // 标题带有颜色，所以需要将对应颜色传递进来
    public int iconResourceId;
    public String iconUrl;
    public int colorResourceId;
    public String title;

    public CommonModel(int iconResourceId, String iconUrl, int colorResourceId, String title) {
        this.iconResourceId = iconResourceId;
        this.iconUrl = iconUrl;
        this.colorResourceId = colorResourceId;
        this.title = title;
    }
}
