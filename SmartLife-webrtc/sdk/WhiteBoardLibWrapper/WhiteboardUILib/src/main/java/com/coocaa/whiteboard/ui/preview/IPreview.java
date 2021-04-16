package com.coocaa.whiteboard.ui.preview;

import android.graphics.Rect;
import android.view.View;

public interface IPreview {
    /**
     * client端的位置和大小
     * @return
     */
    Rect clientRect();

    /**
     * server端的位置和大小
     * @return
     */
    Rect serverRect();

    /**
     * 预览view
     * @return
     */
    View getPreview();
}
