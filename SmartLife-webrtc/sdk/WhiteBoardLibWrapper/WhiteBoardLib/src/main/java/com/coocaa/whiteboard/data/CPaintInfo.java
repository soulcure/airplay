package com.coocaa.whiteboard.data;

import android.graphics.Paint;
import android.util.ArrayMap;

import com.coocaa.define.SvgPaintDef;

import java.util.Iterator;
import java.util.Map;

public class CPaintInfo {

    private Map<String, String> paramMap;

    public CPaintInfo setStrokeWidth(String width) {
        addPaintInfo(SvgPaintDef.STROKE_WIDTH, width);
        return this;
    }

    public CPaintInfo setStrokeColor(String color) {
        addPaintInfo(SvgPaintDef.STROKE_COLOR, color);
        return this;
    }

//    public CPaintInfo setStrokeColor(String color) {
//        addPaintInfo(SvgPaintDef.STROKE_COLOR, color);
//        return this;
//    }

    public CPaintInfo setStyle(int style) {
        addPaintInfo(SvgPaintDef.STYLE, Paint.Style.STROKE.ordinal()+"");
        return this;
    }

    public CPaintInfo setEffect(String effect) {
        addPaintInfo(SvgPaintDef.EFFECT, effect);
        return this;
    }

    public CPaintInfo update(CPaintInfo info) {
        if(info.paramMap == null)
            return this;
        Iterator<Map.Entry<String, String>> iter = info.paramMap.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            addPaintInfo(entry.getKey(), entry.getValue());
        }

        return this;
    }

    public CPaintInfo update(CEraseInfo info) {
        if(info == null)
            return this;
        addPaintInfo(SvgPaintDef.STROKE_WIDTH, info.getWidth()+"");
        addPaintInfo(SvgPaintDef.STROKE_COLOR, info.getColor());
        addPaintInfo(SvgPaintDef.EFFECT, SvgPaintDef.EFF_ERASE);
        return this;
    }

    private void addPaintInfo(String key, String value) {
        if (paramMap == null)
            paramMap = new ArrayMap<>();
        if(value == null)
            paramMap.remove(key);
        paramMap.put(key, value);
    }

    public Iterator<Map.Entry<String, String>> iter() {
        if(paramMap == null)
            return null;
        return paramMap.entrySet().iterator();
    }

    public boolean hasInfo(String key) {
        return paramMap != null && paramMap.containsKey(key);
    }

    public String getInfo(String key) {
        return paramMap == null ? "" : paramMap.get(key);
    }
}
