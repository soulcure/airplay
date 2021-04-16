package com.coocaa.svg.render;

import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.util.ArrayMap;

import com.coocaa.define.SvgPaintDef;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class SvgPaint {

    Map<String, String> paramMap;

    private static final String SPACE = " ";

    public SvgPaint addParams(String key, String value) {
        if(paramMap == null)
            paramMap = new ArrayMap<>();
        paramMap.put(key, value);
        return this;
    }

    public boolean hasInfo(String key) {
        return paramMap != null && paramMap.containsKey(key);
    }

    public String getInfo(String key) {
        return paramMap == null ? "" : paramMap.get(key);
    }

//    public void set(SvgPaint paint) {
//        if(paint != null && paint.paramMap != null && paint.paramMap.isEmpty()) {
//            if(this.paramMap == null) {
//                this.paramMap = new ArrayMap<>();
//            }
//            this.paramMap.putAll(paint.paramMap);
//        }
//    }

    public void updatePaint(Paint paint) {
        if(paramMap == null || paint == null)
            return ;
        if(paramMap.containsKey(SvgPaintDef.STROKE_WIDTH)) {
            paint.setStrokeWidth(Integer.parseInt(Objects.requireNonNull(paramMap.get(SvgPaintDef.STROKE_WIDTH))));
        }
        if(paramMap.containsKey(SvgPaintDef.STROKE_COLOR)) {
            try {
                paint.setColor(Color.parseColor(paramMap.get(SvgPaintDef.STROKE_COLOR)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(paramMap.containsKey(SvgPaintDef.STYLE)) {

        }
    }

    public boolean isErase() {
       return paramMap.containsKey(SvgPaintDef.EFFECT) && SvgPaintDef.EFF_ERASE.equals(paramMap.containsKey(SvgPaintDef.EFFECT));
    }

    public String toXmlString() {
        if(paramMap == null)
            return "";
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, String>> iter =  paramMap.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            sb.append(SPACE).append(entry.getKey()).append("=\"")
                    .append(entry.getValue()).append("\"").append(SPACE);
        }
        return sb.toString();
    }

    public static Paint defaultPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setPathEffect(new CornerPathEffect(10));
        paint.setTextSize(30);

        return paint;
    }
}
