package com.coocaa.svg.writer;

import android.graphics.PointF;

import com.coocaa.define.CPath;
import com.coocaa.interfaces.IPathWriter;
import com.coocaa.svg.render.SvgPaint;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author: yuzhan
 */
public class SvgBezierPathWriter implements IPathWriter {

    final String SPACE = " ";
    final String moveTo = "M ";
    final String close = " Z";
    final String curTo = "C ";
    final String lineTo = "L ";
    private SvgPaint paint;

    @Override
    public String pathString(CPath path) {
//        return "M555,535C555,535,595,555,615,559.5M615,559.5C635,564,662,572,696.5,577.5M696.5,577.5C731,583,766,585,798,586.5M798,586.5C830,588,863,589,895,591.5M895,591.5C927,594,963,599,1003.5,603M1003.5,603C1044,607,1082,611,1119,613.5M1119,613.5C1156,616,1192,619,1222.5,619.5M1222.5,619.5C1253,620,1282,620,1319,617.5M1319,617.5C1356,615,1401,609,1434,603.5M1434,603.5C1467,598,1491,589,1522.5,579M1522.5,579C1554,569,1587,559,1613,554.5M1613,554.5C1639,550,1645,544,1645,544";
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
        //三个一组进行分割
        List<BezierQuadInfo> quadInfoList = new LinkedList<>();
        if(path.getMove().size() < 1) {
            return ;
        } else if(path.getMove().size() == 1) {
            BezierQuadInfo info = new BezierQuadInfo();
            info.start = path.getStart();
            info.control = path.getMove().get(0);
            info.end = path.getEnd();
            quadInfoList.add(info);
        } else {
            List<PointF> moveList = path.getMove();
            int seek = 0;
            int size = moveList.size();
            BezierQuadInfo tempInfo = null;
            for(PointF p : moveList) {
                if(seek == 0) {
                    tempInfo = new BezierQuadInfo();
                    tempInfo.start = p;
                    quadInfoList.add(tempInfo);
                } else if(seek == 1) {
                    tempInfo.control = p;
                } else if(seek == 2) {
                    tempInfo.end = p;
                }
//                if((seek & 1) == 1) {
//                    //是奇数
//                    tempInfo.end = p;
//                } else {
//                    tempInfo = new BezierQuadInfo();
//                    tempInfo.control = p;
//                    quadInfoList.add(tempInfo);
//                }
                seek++;
                if(seek > 2) {
                    seek = 0;
                }
            }
//            if((size & 1) == 1) {
//                //长度是奇数，补一个end
//                tempInfo.end = path.getEnd();
//            }

            int mod = size % 3;
            if(mod == 1) {
                quadInfoList.remove(quadInfoList.size()-1);
            } else if(mod == 2) {
                tempInfo.end = path.getEnd();
            }
        }
        PointF moveToPoint = null;
        for(BezierQuadInfo info : quadInfoList) {
            if(moveToPoint != null) {
                sb.append(moveTo).append(moveToPoint.x).append(SPACE).append(moveToPoint.y).append(SPACE);
            }
            sb.append(curTo).append(info.start.x).append(SPACE).append(info.start.y).append(SPACE).append(info.control.x).append(SPACE).append(info.control.y)
                    .append(SPACE).append(info.end.x).append(SPACE).append(info.end.y).append(SPACE);
            moveToPoint = info.end;
        }

    }

    private void endTo(StringBuilder sb, CPath path, boolean close) {
        sb.append(moveTo).append(path.getEnd().x).append(SPACE).append(path.getEnd().y).append(SPACE);
        if(close) {
            sb.append(this.close);
        }
//        sb.append("\n");
    }

    public void setPaintInfo(SvgPaint paint) {
        this.paint = paint;
    }

    static class BezierQuadInfo {
        PointF start;
        PointF control;
        PointF end;
    }
    
}
