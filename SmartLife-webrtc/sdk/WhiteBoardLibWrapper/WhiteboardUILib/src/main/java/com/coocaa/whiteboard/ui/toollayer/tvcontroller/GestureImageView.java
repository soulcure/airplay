package com.coocaa.whiteboard.ui.toollayer.tvcontroller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GestureImageView extends ProcessImageView {
    private static final String TAG = GestureImageView.class.getSimpleName();
    private static final int DOUBLE_TAP_ZOOM_DURATION = 300;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private float mMidPntX, mMidPntY;

    public GestureImageView(@NonNull Context context) {
        super(context);
    }

    public GestureImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        super.init();
        setupGestureListeners();
    }

    private void setupGestureListeners() {
        mGestureDetector = new GestureDetector(getContext(), new GestureListener(), null, true);
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
            cancelAllAnimations();
        }

        if (event.getPointerCount() > 1) {
            mMidPntX = (event.getX(0) + event.getX(1)) / 2;
            mMidPntY = (event.getY(0) + event.getY(1)) / 2;
        }

        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);

        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            setImageToWrapCropBounds();
        }

        return true;
    }


    private final class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            postScale(detector.getScaleFactor(), mMidPntX, mMidPntY);
            printMatrix(TAG + "onScale", getCurrentMatrix());
            return true;
        }
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            zoomImageToPosition(getDoubleTapTargetScale(), e.getX(), e.getY(), DOUBLE_TAP_ZOOM_DURATION);
            printMatrix(TAG + "onDoubleTap", getCurrentMatrix());
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            postTranslate(-distanceX, -distanceY);
            printMatrix(TAG + "onScroll", getCurrentMatrix());
            return true;
        }
    }
}
