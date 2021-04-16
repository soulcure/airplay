package com.coocaa.svg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.Log;

import com.coocaa.define.SvgConfig;
import com.coocaa.svg.data.SvgCanvasInfo;
import com.coocaa.svg.data.SvgNode;
import com.coocaa.svg.data.SvgPathNode;
import com.coocaa.svg.render.RenderException;
import com.coocaa.svg.render.SvgSurfaceViewRender;

import java.text.ParseException;

/**
 * @Author: yuzhan
 */
public class ClientSurfaceViewRender extends SvgSurfaceViewRender {




    public ClientSurfaceViewRender(Context context) {
        super(context);
        TAG = "WBClient";
    }

    @Override
    public void offsetAndScale(float x, float y, float scale,float cx,float cy) {
        this.offsetX = x;
        this.offsetY = y;
        this.totalX += offsetX;
        this.totalY += offsetY;
        this.scale = scale;
        this.cx = cx;
        this.cy =cy;
        scaleMatrix.setScale(scale,scale,cx,cy);
        transMatrix.setTranslate(totalX,totalY);
    }


    private void fixLastNode(SvgPathNode node){
        if (node.canvasInfo == null)
            node.canvasInfo = new SvgCanvasInfo();
         node.canvasInfo.x = (int) ((totalX*scale + (1-scale)*cx)/scale);
         node.canvasInfo.y = (int) ((totalY*scale + (1-scale)*cy)/scale);
         node.canvasInfo.scale = scale;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        setMeasuredDimension(1920*3, 1080*3);
    }

    @Override
    public SvgNode renderDiff(String renderData) throws RenderException {
        Log.d(TAG, "renderDiff : " + renderData);
        return renderDiff(renderData, true);
    }

    @Override
    public SvgNode renderDiff(String renderData, boolean render) throws RenderException {
        try {
            SvgPathNode newNode = new SvgPathNode();
            newNode.updatePaint(svgPaint);
//            newNode.setCanvasInfo(svgCanvasInfo);
            boolean ret = newNode.parsePath(renderData);
            Log.d(TAG, "renderDiff ret : " + ret);
            fixLastNode(newNode);
            data.addNodeWithGroup(newNode);
            if (render)
            doRender();
            return newNode;
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RenderException(e);
        }
    }

}
