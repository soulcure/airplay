package com.coocaa.whiteboard.ui.toollayer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

import com.coocaa.whiteboard.ui.R;
import com.coocaa.whiteboard.ui.base.IToolLayerView;
import com.coocaa.whiteboard.ui.util.WhiteboardUIConfig;

public class NoteToolLayerView extends WBToolLayerView implements IToolLayerView {


    public NoteToolLayerView(Context context) {
        this(context, null);
    }

    public NoteToolLayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected String getDefaultPaintColor(){
        return WhiteboardUIConfig.DEFAULT_NOTE_PAINT_COLOR;
    }

    @Override
    protected int getDefaultPaintColorRadioButtonId(){
        return R.id.whiteboard_toolbar_rb_paint_red;
    }

    @Override
    protected boolean isShowEraserRadioGroup() {
        return false;
    }
}