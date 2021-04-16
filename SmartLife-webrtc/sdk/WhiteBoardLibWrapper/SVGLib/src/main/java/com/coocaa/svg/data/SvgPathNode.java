package com.coocaa.svg.data;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.define.SvgTagDef;
import com.coocaa.svg.parser.SvgPathParser;

import java.text.ParseException;

public class SvgPathNode extends SvgDrawNode {

    public transient Path path;

    static SvgPathParser pathParser = new SvgPathParser();
    public String pathValueStr; //path标签中d="xxx"中的内容

    public SvgPathNode() {
        tagName = SvgTagDef.PATH;
        canvasInfo = new SvgCanvasInfo();//默认要带上
    }

    @Override
    public void fulfilSvgString(StringBuilder sb) {
       if (!hasAttr("d") && !hasAttr("D") && !TextUtils.isEmpty(pathValueStr)) {
           sb.append(" d=\"").append(pathValueStr).append("\" ");
       }
       if (svgPaint != null) {
           sb.append(svgPaint.toXmlString());
       }
       if(!hasAttr("x") && canvasInfo != null) {
           canvasInfo.toSvgString(sb);
       }
    }


    @Override
    protected boolean parse(String name, String value) {
        if("d".equals(name) || "D".equals(name)) {
            try {
                return parsePath(value);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return super.parse(name, value);
    }


    public boolean parsePath(String pathString) throws ParseException {
        pathValueStr = pathString;
        path = pathParser.parse(pathString);
        return path != null;
    }

//    Matrix pathMatrix1 = new Matrix();
//    Matrix pathMatrix2 = new Matrix();
    @Override
    public void draw(Canvas canvas, Paint parentPaint) {
        if (path != null) {
            canvas.save();
            //注释的代码是就方案
//            if (canvasInfo != null){
//                Matrix tmp = canvas.getMatrix();
//                pathMatrix1.reset();
//                pathMatrix2.reset();
//
//                pathMatrix1.setTranslate(-canvasInfo.x,-canvasInfo.y);
//                tmp.setConcat(tmp,pathMatrix1);
//                pathMatrix2.setScale(1f/canvasInfo.scale, 1f/canvasInfo.scale);
//                tmp.setConcat(tmp,pathMatrix2);
//                canvas.setMatrix(tmp);
//            }
            Paint mPaint = updatePaint(parentPaint);
//            float mStrokeWidth = mPaint.getStrokeWidth();
//            mPaint.setStrokeWidth(mStrokeWidth*canvasInfo.scale);
            canvas.drawPath(path,mPaint);
            canvas.restore();
        } else {
            Log.w(TAG, "try to draw null path !!");
        }
    }
}
