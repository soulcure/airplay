package com.coocaa.svg.note;

import android.content.Context;
import android.graphics.Color;

import com.coocaa.svg.render.SvgSurfaceViewRender;
import com.coocaa.svg.render.badlogic.SvgViewRender;

/**
 * @Author: yuzhan
 */
public class NoteMarkClientSvgViewRender extends SvgViewRender {




    public NoteMarkClientSvgViewRender(Context context) {
        super(context);
        TAG = "WBClient";
    }

    @Override
    protected int getPaintColor() {
        return Color.TRANSPARENT;
    }
}
