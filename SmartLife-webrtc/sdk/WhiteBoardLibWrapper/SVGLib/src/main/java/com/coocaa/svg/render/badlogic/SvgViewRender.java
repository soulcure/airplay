package com.coocaa.svg.render.badlogic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.coocaa.define.SvgConfig;
import com.coocaa.svg.SvgParser;
import com.coocaa.svg.data.SvgCanvasInfo;
import com.coocaa.svg.data.SvgData;
import com.coocaa.svg.data.SvgNode;
import com.coocaa.svg.data.SvgPathNode;
import com.coocaa.svg.parser.SvgXmlPathParser;
import com.coocaa.svg.render.IRenderView;
import com.coocaa.svg.render.RenderException;
import com.coocaa.svg.render.SvgPaint;
import com.coocaa.svg.util.BitmapUtil;

import java.text.ParseException;

/**
 * 在某些手机上 surfaceview 尺寸过大会导致不显示
 */
public class SvgViewRender extends View implements IRenderView {

    protected Paint paint = SvgPaint.defaultPaint();
    protected StringBuilder sb = new StringBuilder();
    protected SvgData data;

    protected SvgPaint svgPaint = new SvgPaint();
    protected SvgXmlPathParser xmlPathParser = new SvgXmlPathParser();
    protected SvgCanvasInfo svgCanvasInfo = new SvgCanvasInfo();
    protected static String TAG = "SVG-Render";
    private Bitmap savaBit ;
    protected SvgParser svgParser = new SvgParser();
    private Canvas mCanvas;
    protected Matrix scaleMatrix = new Matrix();
    protected Matrix transMatrix = new Matrix();
    protected float offsetX,offsetY,cx,cy,totalX,totalY;
    protected float scale = 1f;

    public SvgViewRender(Context context) {
        super(context);
        scaleMatrix.setScale(3, 3);
//        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_HARDWARE, paint);
        Log.d(TAG,"SvgViewRender-init");
    }


    @Override
    public View getView() {
        return this;
    }


    @Override
    public SvgNode parseServerNode(String renderData) {
        return null;
    }


    @Override
    public void offsetAndScale(float x, float y, float scale,float cx,float cy) {

    }

    @Override
    public boolean savePicture() {
        Log.e("Save", "savePicture..");
        return BitmapUtil.saveImageToGallery(getContext(),toBitmap(),SAVE_BITMAP_FILE_PREFIX);
    }

    @Override
    public Bitmap toBitmap() {
        if (savaBit == null)
            savaBit = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
        if (mCanvas == null)
            mCanvas = new Canvas(savaBit);
        mCanvas.save();
        mCanvas.concat(scaleMatrix);
        mCanvas.scale(1f/3, 1f/3);
        mCanvas.drawColor(getPaintColor());
        data.draw(mCanvas, paint);
        mCanvas.restore();
        return savaBit;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Log.e("k3", "renderview draw");

        transformCanvas(canvas);
        canvas.drawColor(getPaintColor());
        data.draw(canvas, paint);
    }

    protected void transformCanvas(Canvas canvas) {

    }

    protected int getPaintColor(){
        return SvgConfig.BG_COLOR;
    }

    @Override
    public void setPaint(SvgPaint sp) {
        if(sp == null)
            return ;
        sp.updatePaint(paint);
        this.svgPaint = sp;
    }

    @Override
    public void callRender() {
        invalidate();
    }

    @Override
    public void setCanvasInfo(SvgCanvasInfo canvasInfo) {
        svgCanvasInfo.set(canvasInfo);
    }

    @Override
    public void renderXml(String renderXml) throws RenderException {
        try {
            Log.d(TAG, "renderXml");
            data = svgParser.parse(renderXml);
            Log.d(TAG, "after parse, childSize=" + data.childSize());
            invalidate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RenderException(e);
        }
    }

    @Override
    public SvgNode renderDiff(String renderData) throws RenderException {
        return renderDiff(renderData,true);
    }

    @Override
    public SvgNode renderDiff(String renderData, boolean render) throws RenderException {
        try {
            SvgPathNode newNode = new SvgPathNode();
            newNode.updatePaint(svgPaint);
            boolean ret = newNode.parsePath(renderData);
            Log.d(TAG, "renderDiff ret : " + ret);
            Log.e("k3", "renderDiff ret : " + ret);
            data.addNodeWithGroup(newNode);
            if (render)
                invalidate();
            return newNode;
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d(TAG, "renderDiff : " + e.toString());
            throw new RenderException(e);
        }
    }

    @Override
    public SvgNode renderDiffXml(String diffXmlRenderData) {
        try {
            Log.d(TAG, "before renderDiffXml data =" + diffXmlRenderData);
            SvgPathNode newNode = xmlPathParser.parse(diffXmlRenderData);
            data.addNodeWithGroup(newNode);
            Log.d(TAG, "after renderDiffXml, childSize=" + data.childSize());
            invalidate();
            return newNode;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RenderException(e);
        }
    }

    @Override
    public void setOnTop(boolean b) {
        // top
    }

    @Override
    public void clear() {
        Log.d(TAG, "call clear");
        if(data.childNodeList != null) {
            data.childNodeList.clear();
        }
        invalidate();
    }

    @Override
    public SvgData getSvgData() {
        return data;
    }
}