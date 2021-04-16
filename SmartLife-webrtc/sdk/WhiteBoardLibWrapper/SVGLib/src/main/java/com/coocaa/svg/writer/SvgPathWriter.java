package com.coocaa.svg.writer;

import android.graphics.PointF;

import com.coocaa.define.CPath;
import com.coocaa.interfaces.IPathWriter;
import com.coocaa.svg.render.SvgPaint;

/**
 * @Author: yuzhan
 */
public class SvgPathWriter implements IPathWriter {

    final String SPACE = " ";
    final String moveTo = "M ";
    final String lineTo = "L ";
    final String close = " Z";
    final String quadTo = "Q ";

    private SvgPaint paint;

    @Override
    public String pathString(CPath path) {
        StringBuilder sb = new StringBuilder();
        moveTo(sb, path);
        lineTo(sb, path);
        endTo(sb, path, false);
        return sb.toString();
    }

    private void moveTo(StringBuilder sb, CPath path) {
        sb.append(moveTo).append(path.getStart().x).append(SPACE).append(path.getStart().y).append(SPACE);
    }

    private void lineTo(StringBuilder sb, CPath path) {
        for(PointF pointF: path.getMove()) {
            sb.append(lineTo).append(pointF.x).append(SPACE).append(pointF.y).append(SPACE);
//            if (pointF instanceof CPath.QPointF){
//                sb.append(quadTo).append(((CPath.QPointF) pointF).previousX).append(SPACE)
//                        .append(((CPath.QPointF) pointF).previousY).append(SPACE)
//                        .append(((CPath.QPointF) pointF).cX).append(SPACE)
//                        .append(((CPath.QPointF) pointF).cY).append(SPACE);
//            }else{
//                sb.append(lineTo).append(pointF.x).append(SPACE).append(pointF.y).append(SPACE);
//            }
        }
    }

    private void endTo(StringBuilder sb, CPath path, boolean close) {
        sb.append(lineTo).append(path.getEnd().x).append(SPACE).append(path.getEnd().y).append(SPACE);
        if(close) {
            sb.append(this.close);
        }
//        sb.append("\n");
    }

    public void setPaintInfo(SvgPaint paint) {
        this.paint = paint;
    }
}
