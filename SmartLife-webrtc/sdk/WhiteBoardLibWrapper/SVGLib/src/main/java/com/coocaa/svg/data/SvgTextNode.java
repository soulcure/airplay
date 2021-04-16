package com.coocaa.svg.data;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.coocaa.define.SvgTagDef;
import com.coocaa.svg.render.SvgPaint;

public class SvgTextNode extends SvgDrawNode {
    public String text = "1122334";
    public int x, y;
    public int dx, dy;

    public SvgTextNode() {
        tagName = SvgTagDef.TEXT;
    }

    @Override
    public void setNodeValue(String text) {
        this.text = text;
    }

    @Override
    protected boolean parse(String name, String value) {
        if("x".equals(name)) {
            this.x = parseInt(value);
            return true;
        } else if("y".equals(name)) {
            this.y = parseInt(value);
            return true;
        } else if("dx".equals(name)) {
            this.dx = parseInt(value);
            return true;
        } else if("dy".equals(name)) {
            this.dy = parseInt(value);
            return true;
        }
        return super.parse(name, value);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        Log.d("SVG-Draw", "draw text : " + text);
        canvas.drawText(text, x, y, SvgPaint.defaultPaint());
    }
}
