package com.coocaa.whiteboard.data.canvas;

import java.io.Serializable;

/**
 * 画布
 * @Author: yuzhan
 */
public class CCanvasInfo implements Serializable {

    /**
     * 画布总宽度
     */
    public int width;
    /**
     * 画布总高度
     */
    public int height;

    /**
     * 画布缩放比例
     */
    public float scale;

    /**
     * 画布当前位置x
     */
    public int posX;

    /**
     * 画布当前位置y
     */
    public int posY;
}
