package com.coocaa.whiteboard.ui.toollayer.tvcontroller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;


public class TvControllerOverlayView extends View {

    private final RectF mCropViewRect = new RectF();
    protected int mThisWidth, mThisHeight;
    private float mTargetAspectRatio = 16f / 9;
    private final int mDimmedColor;
    private final Paint mCropFramePaint;


    public TvControllerOverlayView(Context context) {
        this(context, null);
    }

    public TvControllerOverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TvControllerOverlayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        mCropFramePaint = new Paint();
        mCropFramePaint.setStyle(Paint.Style.STROKE);
        mCropFramePaint.setStrokeWidth(5);
        mCropFramePaint.setColor(Color.parseColor("#3288FF"));
        mDimmedColor = Color.parseColor("#00000000");
    }


    public void setTargetAspectRatio(final float targetAspectRatio) {
        mTargetAspectRatio = targetAspectRatio;
        if (mThisWidth > 0) {
            setupCropBounds();
            postInvalidate();
        }
    }

    private void setupCropBounds() {
        int height = (int) (mThisWidth / mTargetAspectRatio);
        if (height > mThisHeight) {
            int width = (int) (mThisHeight * mTargetAspectRatio);
            int halfDiff = (mThisWidth - width) / 2;
            mCropViewRect.set(getPaddingLeft() + halfDiff, getPaddingTop(),
                    getPaddingLeft() + width + halfDiff, getPaddingTop() + mThisHeight);
        } else {
            int halfDiff = (mThisHeight - height) / 2;
            mCropViewRect.set(getPaddingLeft(), getPaddingTop() + halfDiff,
                    getPaddingLeft() + mThisWidth, getPaddingTop() + height + halfDiff);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            left = getPaddingLeft();
            top = getPaddingTop();
            right = getWidth() - getPaddingRight();
            bottom = getHeight() - getPaddingBottom();
            mThisWidth = right - left;
            mThisHeight = bottom - top;
            setTargetAspectRatio(mTargetAspectRatio);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDimmedLayer(canvas);
        drawCropFrame(canvas);
    }

    protected void drawDimmedLayer(Canvas canvas) {
        canvas.save();
        canvas.clipRect(mCropViewRect, Region.Op.DIFFERENCE);
        canvas.drawColor(mDimmedColor);
        canvas.restore();
    }

    protected void drawCropFrame(Canvas canvas) {
        canvas.drawRect(mCropViewRect, mCropFramePaint);
    }

}
