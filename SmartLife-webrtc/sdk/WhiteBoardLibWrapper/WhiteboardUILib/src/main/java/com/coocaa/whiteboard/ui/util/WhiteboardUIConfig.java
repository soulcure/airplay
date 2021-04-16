package com.coocaa.whiteboard.ui.util;

import android.util.SparseArray;
import android.util.SparseIntArray;


public class WhiteboardUIConfig {

    public final static SparseArray<String> PAINT_COLOR_MAP = new SparseArray<String>() {
        {
            put(0, "#FC1A4E");
            put(1, "#FD8F15");
            put(2, "#0BE58E");
            put(3, "#1C36FE");
            put(4, "#8C1CFE");
            put(5, "#000001");
        }
    };

    public final static SparseIntArray PAINT_SIZE_MAP = new SparseIntArray() {
        {
            put(0, 4);
            put(1, 6);
            put(2, 8);
            put(3, 10);
            put(4, 12);
            put(5, 14);
        }
    };

    public final static SparseIntArray ERASER_SIZE_MAP = new SparseIntArray() {
        {
            put(0, 12);
            put(1, 24);
            put(2, 36);
            put(3, 48);
            put(4, 60);
            put(5, 72);
        }
    };

    public static final String DEFAULT_WB_PAINT_COLOR = PAINT_COLOR_MAP.get(5);
    public static final String DEFAULT_NOTE_PAINT_COLOR = PAINT_COLOR_MAP.get(0);
    public static final int DEFAULT_PAINT_SIZE = PAINT_SIZE_MAP.get(0);
    public static final int DEFAULT_ERASER_SIZE = ERASER_SIZE_MAP.get(0);
}
