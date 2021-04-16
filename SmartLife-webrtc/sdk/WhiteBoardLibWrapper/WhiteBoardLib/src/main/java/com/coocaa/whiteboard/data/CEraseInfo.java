package com.coocaa.whiteboard.data;

import com.coocaa.define.SvgConfig;

public class CEraseInfo {

    /**
     * 橡皮大小
     */
    private int width = 15;

    private int effect; //橡皮效果，橡皮、马赛克


    public CEraseInfo() {

    }

    public CEraseInfo(int w) {
        this.width = w;
    }

    public CEraseInfo setWidth(int w) {
        this.width = w;

        return this;
    }

    public CEraseInfo set(CEraseInfo eraseInfo) {
        if(eraseInfo != null)
            this.width = eraseInfo.width;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public String getColor() {
        return SvgConfig.BG_COLOR_STRING;
    }
}
