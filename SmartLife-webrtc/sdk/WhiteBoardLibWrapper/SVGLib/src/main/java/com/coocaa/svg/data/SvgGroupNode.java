package com.coocaa.svg.data;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.coocaa.define.SvgTagDef;

import java.util.LinkedList;
import java.util.List;

public class SvgGroupNode extends SvgDrawNode {
    public List<SvgNode> childNodeList;

    public SvgGroupNode() {
        tagName = SvgTagDef.GROUP;
    }

    public void addNode(SvgNode node) {
        if(node == null)
            return ;
        if(childNodeList == null) {
            childNodeList = new LinkedList<>();
        }
        childNodeList.add(node);
    }

    public boolean hasChild() {
        return childNodeList != null && !childNodeList.isEmpty();
    }

    @Override
    public String toSvgString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n<").append(tagName);
        fulfilAttrString(sb);
        fulfilSvgString(sb);
        sb.append(" >");
        if(childNodeList != null) {
            for(SvgNode node : childNodeList) {
                sb.append(node.toSvgString());
            }
        }
        sb.append("\n</").append(tagName).append(">\n");

        return sb.toString();
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if(childNodeList != null) {
            for(SvgNode node : childNodeList) {
                if(node instanceof SvgDrawNode) {
                    Paint tmp = updatePaint(paint);
                    Log.d(TAG, "draw node : " + node);
                    ((SvgDrawNode) node).draw(canvas, tmp);
                }
            }
        }
    }
}
