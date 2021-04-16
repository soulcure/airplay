package com.coocaa.svg.note;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.coocaa.svg.data.SvgCanvasInfo;
import com.coocaa.svg.data.SvgNode;
import com.coocaa.svg.data.SvgPathNode;
import com.coocaa.svg.render.RenderException;
import com.coocaa.svg.render.SvgSurfaceViewRender;

import java.text.ParseException;

/**
 * @Author: yuzhan
 */
public class NoteMarkClientSurfaceViewRender extends SvgSurfaceViewRender {




    public NoteMarkClientSurfaceViewRender(Context context) {
        super(context);
        TAG = "WBClient";
    }

    @Override
    protected int getPaintColor() {
        return Color.TRANSPARENT;
    }
}
