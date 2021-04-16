package com.coocaa.svg.render;

import android.graphics.Bitmap;
import android.view.View;

import com.coocaa.svg.data.SvgNode;

public interface IRenderView extends IRender {

    String SAVE_BITMAP_FILE_PREFIX = "CC_WhiteBoard_";

    View getView();
    void offsetAndScale(float x, float y, float scale,float cx,float cy);
    boolean savePicture();
    Bitmap toBitmap();

    void setOnTop(boolean b);
    SvgNode parseServerNode(String renderData);
}
