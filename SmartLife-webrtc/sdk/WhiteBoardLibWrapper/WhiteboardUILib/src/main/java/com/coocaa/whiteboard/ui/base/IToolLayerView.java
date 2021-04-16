package com.coocaa.whiteboard.ui.base;

import android.view.View;

import androidx.annotation.IntDef;

import com.coocaa.whiteboard.ui.callback.ToolLayerCallback;
import com.coocaa.whiteboard.ui.toollayer.WBToolLayerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface IToolLayerView {
    int MODE_PAINT = 0; //当前是画笔模式
    int MODE_ERASER = 1; //当前是橡皮擦模式
    int MODE_NONE = 2; //当前画笔和橡皮都不聚焦

    @IntDef({MODE_PAINT, MODE_ERASER, MODE_NONE})
    @Retention(RetentionPolicy.SOURCE)
    @interface PaintMode {
    }


    View getContentView();

    String getCurrPaintColor();

    int getCurrPaintSize();

    int getCurrEraserSize();

    @PaintMode int getCurrPaintMode();

    void setToolLayerCallback(ToolLayerCallback toolLayerCallback);

    void hideAllPopupWindow();
}
