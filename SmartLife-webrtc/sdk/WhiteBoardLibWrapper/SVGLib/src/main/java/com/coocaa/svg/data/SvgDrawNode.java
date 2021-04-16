package com.coocaa.svg.data;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.ArrayMap;
import android.util.Log;

import com.coocaa.define.SvgConfig;
import com.coocaa.define.SvgPaintDef;
import com.coocaa.svg.render.SvgPaint;

import java.util.Iterator;
import java.util.Map;

public abstract class SvgDrawNode extends SvgNode {

    public Map<String, String> paintMap;
    private Paint paint = SvgPaint.defaultPaint();
    protected SvgPaint svgPaint;
    public SvgCanvasInfo canvasInfo;

    public SvgDrawNode() {

    }

    public void update(SvgDrawNode node) {

    }

    public void setCanvasInfo(SvgCanvasInfo canvasInfo) {
        if(canvasInfo == null)
            return ;
        if(this.canvasInfo == null) {
            this.canvasInfo = new SvgCanvasInfo();
        }
        this.canvasInfo.set(canvasInfo);
    }

    private boolean addCanvasInfo(String key, String value) {
        if(SvgCanvasInfo.isValidKey(key)) {
            if(canvasInfo == null) {
                canvasInfo = new SvgCanvasInfo();
            }
            canvasInfo.setAttr(key, value);
            return true;
        }
        return false;
    }

    @Override
    protected boolean parse(String name, String value) {
        if (SvgPaintDef.STROKE_WIDTH.equals(name)) {
            addPaintInfo(name, value);
            return true;
        } else if (SvgPaintDef.STROKE_COLOR.equals(name)) {
            addPaintInfo(name, value);
            return true;
        } else if (SvgPaintDef.EFFECT.equals(name)) {
            addPaintInfo(name, value);
            return true;
        } else {
            if(addCanvasInfo(name, value)) {
                return true;
            }
        }
        return super.parse(name, value);
    }

    public void setStrokeWidth(String width) {
        addPaintInfo(SvgPaintDef.STROKE_WIDTH, width);
    }

    public void setStrokeColor(String color) {
        addPaintInfo(SvgPaintDef.STROKE_COLOR, color);
    }

    public void setStyle(String style) {
        addPaintInfo(SvgPaintDef.STYLE, style);
    }

    public void setEffect(String effect) {
        addPaintInfo(SvgPaintDef.EFFECT, effect);
    }

    private void addPaintInfo(String key, String value) {
        Log.d(TAG, "addPaintInfo, key=" + key + ", value=" + value);
        if (paintMap == null)
            paintMap = new ArrayMap<>();
        if(value != null) {
            paintMap.put(key, value);
        }
    }

    public abstract void draw(Canvas canvas, Paint paint);

    protected int parseColor(String value) {
        try {
            return Color.parseColor(value);
        } catch (Exception e) {

        }
        return Color.RED;
    }

    public void updatePaint(SvgPaint paint) {
        svgPaint = paint;
        if (paint.hasInfo(SvgPaintDef.STROKE_WIDTH)) {
            setStrokeWidth(paint.getInfo(SvgPaintDef.STROKE_WIDTH));
        }
        if (paint.hasInfo(SvgPaintDef.STROKE_COLOR)) {
            setStrokeColor(paint.getInfo(SvgPaintDef.STROKE_COLOR));
        }
        if(paint.hasInfo(SvgPaintDef.EFFECT)) {
           setEffect(paint.getInfo(SvgPaintDef.EFFECT));
        }
    }


    public Paint updatePaint(Paint parentPaint) {
        if (paintMap == null) return parentPaint;
//        childPaint.reset();
        paint.set(parentPaint);
        if (paintMap.get(SvgPaintDef.STROKE_WIDTH) != null) {
            paint.setStrokeWidth(parseFloat(paintMap.get(SvgPaintDef.STROKE_WIDTH)));
        }
        if (this instanceof SvgPathNode || this instanceof SvgTextNode) {
            if(isErase() || (svgPaint != null && svgPaint.isErase())) {
                paint.setColor(SvgConfig.BG_COLOR);
            } else {
                if (paintMap.get(SvgPaintDef.STROKE_COLOR) != null) {
                    paint.setColor(parseColor(paintMap.get(SvgPaintDef.STROKE_COLOR)));
                }
            }
        } else {
            if (paintMap.get(SvgPaintDef.STYLE) != null)
                paint.setColor(parseInt(paintMap.get(SvgPaintDef.STYLE)));
        }
//        Log.d(TAG, "set width=" + paintMap.get(SvgPaintDef.STROKE_WIDTH));
//        Log.d(TAG, "set color=#" + Integer.toHexString(parseInt(paintMap.get(SvgPaintDef.STROKE_COLOR))));
        return paint;
    }

    public boolean isErase() {
        return paintMap.containsKey(SvgPaintDef.EFFECT) && SvgPaintDef.EFF_ERASE.equals(paintMap.get(SvgPaintDef.EFFECT));
    }

    @Override
    public void fulfilSvgString(StringBuilder sb) {
        if(paintMap != null) {
            Iterator<Map.Entry<String, String>> iter =  paintMap.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
//                sb.append(SPACE).append(entry.getKey()).append("=").append(entry.getValue()).append(SPACE);
            }
        }
    }
}
