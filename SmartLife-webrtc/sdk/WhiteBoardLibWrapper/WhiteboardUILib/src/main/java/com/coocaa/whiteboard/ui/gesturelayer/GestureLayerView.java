/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.coocaa.whiteboard.ui.gesturelayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.coocaa.whiteboard.ui.callback.GestureLayerCallback;
import com.coocaa.whiteboard.ui.gesturelayer.detector.CGesturDetector;
import com.coocaa.whiteboard.ui.gesturelayer.detector.CScaleGestureDetector;
import com.coocaa.whiteboard.ui.gesturelayer.detector.PathGestureDetector;
import com.coocaa.whiteboard.ui.gesturelayer.detector.TranslateGestureDetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * 手势捕捉层UI
 */
public class GestureLayerView extends FrameLayout {
    private static final String TAG = GestureLayerView.class.getSimpleName();
    private static final boolean GESTURE_RENDERING_ANTIALIAS = true;
    private static final boolean DITHER_FLAG = true;
    private int certainGestureColor = 0xFFFC1A4E;
    private float gestureStrokeWidth = 5f;

    protected PathGestureDetector pathGestureDetector;
    protected CScaleGestureDetector cScaleGestureDetector;
    protected TranslateGestureDetector translateGestureDetector;
    protected Path path = new Path();
    protected Paint paint = new Paint();
    protected float midPntX, midPntY;
    protected boolean isDrawPath = true;

    protected GestureLayerCallback gestureLayerCallback;

    protected CGesturDetector mCGesturDetector;
    private CGesturDetector.CGestureListenerImpl mCGestureListener;
    public GestureLayerView(@NonNull Context context) {
        this(context, null);
    }

    public GestureLayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureLayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public GestureLayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//
//    }

    protected void init() {
        setWillNotDraw(false);
        pathGestureDetector = new PathGestureDetector(new PathListener());
        cScaleGestureDetector = new CScaleGestureDetector(getContext(), new ScaleListener());
        translateGestureDetector = new TranslateGestureDetector(new TranslateListener());
        mCGestureListener = new CGesturDetector.CGestureListenerImpl();

        mCGesturDetector = new CGesturDetector(getContext(),mCGestureListener,this);
        final Paint gesturePaint = new Paint();
        gesturePaint.setAntiAlias(GESTURE_RENDERING_ANTIALIAS);
        gesturePaint.setColor(certainGestureColor);
        gesturePaint.setStyle(Paint.Style.STROKE);
        gesturePaint.setStrokeJoin(Paint.Join.ROUND);
        gesturePaint.setStrokeCap(Paint.Cap.ROUND);
        gesturePaint.setStrokeWidth(gestureStrokeWidth);
        gesturePaint.setDither(DITHER_FLAG);
        paint = gesturePaint;
        pathGestureDetector.setRewind(true);
    }

    public void setGestureColor(int color) {
        certainGestureColor = color;
        paint.setColor(certainGestureColor);
    }

    public void setGestureStrokeWidth(float width) {
        gestureStrokeWidth = width;
        paint.setStrokeWidth(gestureStrokeWidth);
    }

    public void isDrawPath(boolean isDrawPath){
        this.isDrawPath = isDrawPath;
    }


    public void setGestureLayerCallback(GestureLayerCallback gestureLayerCallback) {
        this.gestureLayerCallback = gestureLayerCallback;
        mCGestureListener.setCallback(gestureLayerCallback);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (event.getPointerCount() > 1) {
            midPntX = (event.getX(0) + event.getX(1)) / 2;
            midPntY = (event.getY(0) + event.getY(1)) / 2;
        }
        pathGestureDetector.onTouchEvent(event);
        cScaleGestureDetector.onTouchEvent(event);
        translateGestureDetector.onTouchEvent(event);
        mCGesturDetector.onTouchEvent(event);

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (path != null && isDrawPath) {
            canvas.drawPath(path, paint);
        }
    }

    private class PathListener extends PathGestureDetector.SimpleGestureDetectorListener {
        @Override
        public void onPathChange(@Nullable Rect invalidRect, Path changePath) {
            Log.d(TAG, "onPathChange: changePath" +  changePath.toString());
            path = changePath;
            if (invalidRect != null) {
                invalidate(invalidRect);
            } else {
                invalidate();
            }
        }

        @Override
        public void onGestureStarted(MotionEvent event) {
            if(gestureLayerCallback != null){
                gestureLayerCallback.onGestureStarted(event);
            }
        }

        @Override
        public void onGesture(MotionEvent event) {
            if(gestureLayerCallback != null){
                gestureLayerCallback.onGesture(event);
            }
        }

        @Override
        public void onGestureEnded(MotionEvent event) {
            if(gestureLayerCallback != null){
                gestureLayerCallback.onGestureEnded(event);
            }
        }

        @Override
        public void onGestureCancelled(MotionEvent event) {
            if(gestureLayerCallback != null){
                gestureLayerCallback.onGestureCancelled(event);
            }
        }
    }

    private class ScaleListener extends CScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(CScaleGestureDetector detector) {
            if(gestureLayerCallback != null){
                gestureLayerCallback.onScaleCanvas(detector.getScaleFactor(),midPntX,midPntY);
            }
            Log.d(TAG, "onScale: getScaleFactor" + detector.getScaleFactor());
            Log.d(TAG, "onScale: midPntX" + midPntX);
            Log.d(TAG, "onScale: midPntY" + midPntY);
            return true;
        }

        @Override
        public void onScaleDone() {
            Log.d(TAG, "onScaleDone");
            if(gestureLayerCallback != null){
                gestureLayerCallback.onScaleCanvasDone();
            }
        }
    }

    private class TranslateListener implements TranslateGestureDetector.OnTranslateGestureDetectorListener {

        @Override
        public void onTranslate(float translateX, float translateY) {
            if(gestureLayerCallback != null){
                gestureLayerCallback.onTranslateCanvas(translateX,translateY);
            }
            Log.d(TAG, "onTranslate: translateX" + translateX);
            Log.d(TAG, "onTranslate: translateY" + translateY);
        }

        @Override
        public void onTranslateDone() {
            Log.d(TAG, "onTranslateDone");
            if(gestureLayerCallback != null){
                gestureLayerCallback.onTranslateDone();
            }
        }
    }
}
