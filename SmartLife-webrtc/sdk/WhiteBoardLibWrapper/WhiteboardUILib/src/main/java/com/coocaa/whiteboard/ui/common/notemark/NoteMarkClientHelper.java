package com.coocaa.whiteboard.ui.common.notemark;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.DisplayMetrics;

import com.coocaa.define.SvgConfig;
import com.coocaa.svg.util.BitmapUtil;
import com.coocaa.whiteboard.client.WhiteBoardClient;
import com.coocaa.whiteboard.ui.base.IToolLayerView;
import com.coocaa.whiteboard.ui.common.WBClientHelper;
import com.coocaa.whiteboard.ui.toollayer.NoteToolLayerView;

import static com.coocaa.svg.render.IRenderView.SAVE_BITMAP_FILE_PREFIX;

public abstract class NoteMarkClientHelper extends WBClientHelper {

    public NoteMarkClientHelper(Activity activity) {
        super(activity);
        SvgConfig.changeToClient();
        whiteBoardView.allowGesture(true);
        whiteBoardView.setZOrderOnTop(true);
    }

    @Override
    protected String tag() {
        return "NMClient";
    }

    @Override
    protected WhiteBoardClient newClient(Context context) {
        return new NoteMarkClient(context);
    }

    protected int getWidth(){
        return getWindowSize().x;
    }

    protected int getHeight(){
        return getWindowSize().y;
    }

    @Override
    protected int containerLeftMargin() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
        int screenWidth = outMetrics.widthPixels;
        return (screenWidth - getWindowSize().x) /2;
    }

    @Override
    protected int containerTopMargin() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
        int screenHeight = outMetrics.heightPixels;
        return (screenHeight - getWindowSize().y) /2;
    }

    @Override
    protected boolean isShowTipView() {
        return false;
    }

    @Override
    protected IToolLayerView createToolLayerView(Activity activity) {
        return new NoteToolLayerView(activity);
    }

    Bitmap savaBit;
    Canvas mCanvas;
    @Override
    public boolean savePicture() {
        if (savaBit == null)
            savaBit = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
        if (mCanvas == null)
            mCanvas = new Canvas(savaBit);
        mCanvas.save();
        containerLayout.draw(mCanvas);
        mCanvas.restore();
        return BitmapUtil.saveImageToGallery(activity, savaBit, SAVE_BITMAP_FILE_PREFIX);
    }

    Point mPoint = null;
    private Point getWindowSize(){
        if (mPoint != null){
            return mPoint;
        }
        mPoint = new Point(1920, 1080);
        containerLayout.setScaleX(getScaleX());
        containerLayout.setScaleY(getScaleY());
        return mPoint;
    }
    private float getScaleX(){
        return 0.75f;
    }

    private float getScaleY(){
        return 0.75f;
    }

    @Override
    protected void fixScale() {
//        super.fixScale();
    }
}
