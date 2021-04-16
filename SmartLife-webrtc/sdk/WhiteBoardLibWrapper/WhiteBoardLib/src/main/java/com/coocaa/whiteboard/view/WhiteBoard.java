package com.coocaa.whiteboard.view;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.coocaa.define.CPath;
import com.coocaa.interfaces.IPathWriter;
import com.coocaa.svg.data.SvgCanvasInfo;
import com.coocaa.svg.data.SvgData;
import com.coocaa.svg.render.IRenderView;
import com.coocaa.svg.render.RenderException;
import com.coocaa.svg.render.SvgPaint;
import com.coocaa.svg.render.SvgSurfaceViewRender;
import com.coocaa.svg.render.badlogic.SvgViewRender;
import com.coocaa.svg.writer.SvgPathWriter;
import com.coocaa.whiteboard.IWhiteBoard;
import com.coocaa.whiteboard.client.WhiteBoardClientListener;
import com.coocaa.whiteboard.data.CEraseInfo;
import com.coocaa.whiteboard.data.CPaintInfo;

import java.util.Iterator;
import java.util.Map;

/**
 * @Author: yuzhan
 */
public class WhiteBoard implements IWhiteBoard {
    protected IRenderView renderView;
    protected WhiteBoardClientListener listener;

    Paint paint;
    protected CPath clientPath = new CPath();
    protected CPath serverPath = new CPath();
    protected IPathWriter pathWriter = newWriter();
    protected int historySize = 0;
    protected boolean useSurfaceView = true;
    CPaintInfo paintInfo = new CPaintInfo();
    private CEraseInfo eraseInfo = new CEraseInfo();
    protected SvgCanvasInfo svgCanvasMoveInfo = new SvgCanvasInfo();

    protected String TAG = "WhiteBoard";

    public WhiteBoard() {
        paint = SvgPaint.defaultPaint();
    }


    protected IPathWriter newWriter() {
        return new SvgPathWriter();
    }

    @Override
    public View getView() {
        return renderView.getView();
    }

    @Override
    public IWhiteBoard setPaintInfo(CPaintInfo paintInfo) {
        this.paintInfo.update(paintInfo);
        renderView.setPaint(toSvgPaint(paintInfo));
        return this;
    }

    @Override
    public IWhiteBoard setEraseInfo(CEraseInfo eraseInfo) {
        this.eraseInfo.set(eraseInfo);
        this.paintInfo.update(eraseInfo);
        renderView.setPaint(toSvgPaint(paintInfo));
        return this;
    }

    public void updateEraseInfo(int w){
        eraseInfo.setWidth(w);
    }
    public void updatePaintInfo(int w,String color){
        paintInfo.setStrokeColor(color);
        paintInfo.setStrokeWidth(w+"");
    }

    private SvgPaint toSvgPaint(CPaintInfo info) {
        SvgPaint paint = new SvgPaint();
        Iterator<Map.Entry<String, String>> iter = info.iter();
        if (iter != null) {
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                paint.addParams(entry.getKey(), entry.getValue());
            }
        }

        return paint;
    }

    @Override
    public IWhiteBoard render(String renderData) {
        try {
            Log.d(TAG, "render : " + renderData);
            renderView.renderXml(renderData);
        } catch (RenderException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public IWhiteBoard renderDiff(String diffRenderData) {
        renderView.renderDiff(diffRenderData);
        return this;
    }

    @Override
    public IWhiteBoard renderDiffXml(String diffXmlRenderData) {
        try {
            Log.d(TAG, "renderDiffXml : " + diffXmlRenderData);
            renderView.renderDiffXml(diffXmlRenderData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

//    @Override
//    public IWhiteBoard setSvgData(SvgData data) {
//        this.svgData = data;
//        renderView.render(data);
//        return this;
//    }

    protected void resetCanvasMoveInfo() {
        svgCanvasMoveInfo.x = 0;
        svgCanvasMoveInfo.y = 0;
        svgCanvasMoveInfo.scale = 1f;
    }

    @Override
    public IWhiteBoard onMotionEvent(MotionEvent event) {
        return onMotionEvent(event, false);
    }

    @Override
    public IWhiteBoard onEraseMotionEvent(MotionEvent event) {
        return onMotionEvent(event, true);
    }

    protected int moveType = MotionEvent.INVALID_POINTER_ID;
    protected float scale = 1f;


    protected IWhiteBoard onMotionEvent(MotionEvent event, boolean isErase) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                moveType = 1;
                clientPath.reset();
                clientPath.start(event.getX(), event.getY());
                resetCanvasMoveInfo();
                break;
            case MotionEvent.ACTION_CANCEL:
                moveType = 1;
                clientPath.reset();
                clientPath.start(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                moveType = 2;
                break;
            case MotionEvent.ACTION_MOVE:
                if (moveType == 1) {
                    clientPath.moveTo(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                if (moveType == 1) {
                    historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        clientPath.end(event.getHistoricalX(i), event.getHistoricalY(i));
                    }
                    clientPath.end(event.getX(), event.getY());
                    onPathFinish(clientPath, isErase);
                }
                break;
        }
        return this;
    }

    protected void onPathFinish(CPath path, boolean isErase) {
        renderView.renderDiff(pathWriter.pathString(path));
    }


    @Override
    public IWhiteBoard registerListener(WhiteBoardClientListener listener) {
        this.listener = listener;
        return this;

    }

    public String getCurrentSvgString() {
        SvgData svgData = renderView.getSvgData();
        if (svgData != null)
            return svgData.toSvgString();
        return null;
    }

    public boolean isSvgChanged() {
        SvgData svgData = renderView.getSvgData();
        return svgData != null && svgData.hasChild();
    }

    public void setZOrderOnTop(boolean top){
        renderView.setOnTop(top);
    }

    @Override
    public void test() {
        Log.d(TAG, "print svg data....");
        Log.d(TAG, getCurrentSvgString());
    }

    @Override
    public boolean undo() {
        return false;
    }

    @Override
    public boolean redo() {
        return false;
    }

    @Override
    public void setServerAddress(String server) {

    }

    @Override
    public void connect() {

    }

    @Override
    public void close() {

    }

    @Override
    public void offsetAndScale(float x, float y, float scale,float cx,float cy) {
        if (renderView != null)
            renderView.offsetAndScale(x, y,scale,cx,cy);
        renderView.callRender();
        svgCanvasMoveInfo.x += (int) x;
        svgCanvasMoveInfo.y += (int) y;
    }


    @Override
    public void onCreate(Context context) {
        if (renderView == null) {
            renderView = useSurfaceView ? new SvgSurfaceViewRender(context) : new SvgViewRender(context);
        }
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onNewIntent() {

    }

    @Override
    public void clearWhiteBoard(boolean exit) {

    }

    public boolean savePicture() {
        if (renderView != null)
              return renderView.savePicture();
        return false;
    }
}
