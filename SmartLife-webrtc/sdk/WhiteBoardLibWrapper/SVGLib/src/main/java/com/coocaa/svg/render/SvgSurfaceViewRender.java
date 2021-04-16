package com.coocaa.svg.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.coocaa.define.SvgConfig;
import com.coocaa.svg.SvgParser;
import com.coocaa.svg.data.SvgCanvasInfo;
import com.coocaa.svg.data.SvgData;
import com.coocaa.svg.data.SvgGroupNode;
import com.coocaa.svg.data.SvgNode;
import com.coocaa.svg.data.SvgPathNode;
import com.coocaa.svg.parser.SvgXmlPathParser;
import com.coocaa.svg.util.BitmapUtil;

import java.text.ParseException;

/**
 * @Author: yuzhan
 */
public class SvgSurfaceViewRender extends SurfaceView implements IRenderView, SurfaceHolder.Callback, Runnable {

    protected SurfaceHolder holder;

    Canvas canvas;
    protected Paint paint = SvgPaint.defaultPaint();
    protected SvgData data;
    protected volatile boolean drawing = false;
    private Thread renderThread;
    protected SvgPaint svgPaint = new SvgPaint();
    protected SvgXmlPathParser xmlPathParser = new SvgXmlPathParser();
    SvgParser svgParser = new SvgParser();
    protected static String TAG = "SVG-Render";

    protected float offsetX = 0,offsetY = 0,scale = 1f;
    protected float cx,cy;
    protected int totalX,totalY;
    protected SvgCanvasInfo svgCanvasInfo = new SvgCanvasInfo();
    protected Matrix transMatrix = new Matrix();
    protected Matrix scaleMatrix = new Matrix();

    public SvgSurfaceViewRender(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);
        data = new SvgData();
        scaleMatrix.setScale(3, 3);
    }

    @Override
    public void setPaint(SvgPaint sp) {
        if(sp == null)
            return ;
        sp.updatePaint(paint);
        this.svgPaint = sp;
//        judgeAddNewGroup();//画笔变化
    }


    @Override
    public void callRender() {
        Log.e("kkk","callRender");
        doRender();
    }



    @Override
    public void setCanvasInfo(SvgCanvasInfo canvasInfo) {
        svgCanvasInfo.set(canvasInfo);
    }

    //判断最后一个节点，如果不是SvgGroup，就创建SvgGroup，如果是则判断是否是空SvgGroup
    //同样画笔的，放一个SvgGroup里面，节省数据
    private void judgeAddNewGroup() {
        if(data.getLastNode() != null) {
            boolean needAddNewGroup = true;
            if(data.getLastNode() instanceof SvgGroupNode && ((SvgGroupNode) data.getLastNode()).hasChild()) {
                needAddNewGroup = false;
            }
            if(needAddNewGroup) {
                data.addNode(new SvgGroupNode());
            }
        }
    }

    @Override
    public void offsetAndScale(float x, float y, float scale,float cx,float cy) {

    }


    @Override
    public SvgNode renderDiff(String renderData) throws RenderException{
        Log.d(TAG, "renderDiff : " + renderData);
        return renderDiff(renderData,true);
    }

    public void setOnTop(boolean top){
        setZOrderOnTop(top);
    }

    @Override
    public SvgNode renderDiff(String renderData, boolean render) throws RenderException {
        try {
            SvgPathNode newNode = new SvgPathNode();
            newNode.updatePaint(svgPaint);
//            newNode.setCanvasInfo(svgCanvasInfo);
            boolean ret = newNode.parsePath(renderData);
            Log.d(TAG, "renderDiff ret : " + ret);
            data.addNodeWithGroup(newNode);
            if (render)
                doRender();
            return newNode;
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d(TAG, "renderDiff : " + e.toString());
            throw new RenderException(e);
        }
    }

    @Override
    public SvgNode parseServerNode(String renderData) {
        SvgPathNode newNode = new SvgPathNode();
        newNode.updatePaint(svgPaint);
        try {
            boolean ret = newNode.parsePath(renderData);
            Log.d(TAG, "parseServerNode ret : " + ret);
            return newNode;
        } catch (ParseException e) {
            throw new RenderException(e);
        }
    }

    @Override
    public SvgNode renderDiffXml(String diffXmlRenderData) throws RenderException {
        try {
            Log.d(TAG, "before renderDiffXml data =" + diffXmlRenderData);
            SvgPathNode newNode = xmlPathParser.parse(diffXmlRenderData);
            data.addNodeWithGroup(newNode);
            Log.d(TAG, "after renderDiffXml, childSize=" + data.childSize());
            doRender();
            return newNode;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RenderException(e);
        }
    }

    @Override
    public void clear() {
        Log.d(TAG, "call clear");
        if(data.childNodeList != null) {
            data.childNodeList.clear();
        }
        doRender();
    }

    @Override
    public void renderXml(String renderXml) throws RenderException {
        try {
            Log.d(TAG, "renderXml");
            data = svgParser.parse(renderXml);
            Log.d(TAG, "after parse, childSize=" + data.childSize());
            doRender();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RenderException(e);
        }
    }

    @Override
    public SvgData getSvgData() {
        return data;
    }

    //    @Override
//    public void renderNode(SvgNode node) throws RenderException {
//        if(node == null)
//            return ;
//        Log.d(TAG, "renderNode : " + node);
//        if(data == null)
//            data = new SvgData();
//        data.addNodeWithGroup(node);
//        doRender();
//    }

//    @Override
//    public void render(SvgData svgData) throws RenderException {
//        this.data = svgData;
//        doRender();
//    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e("kkk","surfaceCreated");
        drawing = true;
        renderThread = new Thread(this, "SvgSurfaceRender");
        renderThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e("kkk","surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e("kkk","surfaceDestroyed");
        drawing = false;
        if(renderThread != null) {
            try {
                renderThread.interrupt();
                renderThread = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
           if (drawing){
               doRender();
           }
    }


    private Bitmap savaBit ;
    private Canvas mCanvas;
    @Override
    public boolean savePicture(){
        return BitmapUtil.saveImageToGallery(getContext(),toBitmap(),SAVE_BITMAP_FILE_PREFIX);

    }

    @Override
    public Bitmap toBitmap() {
        if (savaBit == null)
            savaBit = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
        if (mCanvas == null)
            mCanvas = new Canvas(savaBit);
        doRender(mCanvas);
        return savaBit;
    }

    protected final Object renderLock = new Object();



    protected void doRender(Canvas c) {
        Log.e("kkk","drawing = "+drawing + holder.getSurface());
        if(!drawing) {
            return ;
        }
        synchronized (renderLock) {
            if(!drawing) {
                return ;
            }
            Log.d(TAG, "start doRender");
            try {
                canvas = holder.lockCanvas();
                Log.e("kkk","lockcanvas = "+canvas);
                if (canvas == null) {
                    return;
                }
                if (c != null){
//                    scaleMatrix.setScale(scale, scale);
                    c.concat(scaleMatrix);
//                    c.concat(transMatrix);
                }
                //旧方案兼容措施
//                canvas.concat(scaleMatrix);
//                canvas.concat(transMatrix);

                if (c != null){
                    c.scale(1f/3, 1f/3);
                }
                transformCanvas(canvas);
                canvas.drawColor(getPaintColor());
//                drawTest(canvas);
                data.draw(canvas, paint);
                if (c != null){
                    c.drawColor(getPaintColor());
                    data.draw(c, paint);
                }
            }catch (Exception e){
                Log.d(TAG, "doRender error : " + e.toString());
                e.printStackTrace();
            }finally {
                if (canvas != null){
                    //释放canvas对象并提交画布
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    protected int getPaintColor(){
        return SvgConfig.BG_COLOR;
    }
    protected void transformCanvas(Canvas canvas) {

    }


    protected void doRender(){
       doRender(null);
    }


    Paint mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Matrix test = new Matrix();
    private void drawTest(Canvas canvas) {
        int startX =0;
        int startY =0;
        for (int i = 0; i < 9; i++) {
            startX = 640*(i%3);
            startY = 360*(i/3);
            canvas.save();
            canvas.setMatrix(test);
            mBgPaint.setColor(i % 2 == 0 ? Color.parseColor("#bbffaa"):
                    Color.parseColor("#ffaabb"));
            canvas.drawRect(startX,startY,startX+640,startY+360,mBgPaint);
            canvas.restore();
        }
    }


    @Override
    public View getView() {
        return this;
    }
}
