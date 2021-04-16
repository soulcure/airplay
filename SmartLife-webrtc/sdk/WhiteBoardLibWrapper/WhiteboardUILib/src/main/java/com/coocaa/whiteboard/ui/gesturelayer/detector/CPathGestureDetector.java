package com.coocaa.whiteboard.ui.gesturelayer.detector;

import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.Nullable;


//单指移动路径监听
public class CPathGestureDetector {
    public static final String TAG = CPathGestureDetector.class.getSimpleName();
    private static final int TOUCH_TOLERANCE = 3;

    private float downX;
    private float downY;
    private float curveEndX;
    private float curveEndY;
    private final Rect invalidRect = new Rect();
    private final int invalidateExtraBorder = 10;

    private boolean isListeningForGestures;
    private long firstPointerDownTime;
    private boolean isMultiPointerDown = true;

    private final PathGestureDetectorListener listener;

    private final Path path = new Path();
    private boolean rewind = true;

    private final Handler handler = new Handler(Looper.getMainLooper());

    public CPathGestureDetector(PathGestureDetectorListener listener) {
        this.listener = listener;
    }

    public void setRewind(boolean rewind) {
        this.rewind = rewind;
    }

    private boolean changed = false;
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                changed = false;
                touchDown(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                changed = true;
                touchPointerDown();
                if (listener != null) {
                    listener.onGestureStarted(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (/*!isMultiPointerDown && */isListeningForGestures) {
                    touchMove(event);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (listener != null) {
                    listener.onGesture(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (/*!isMultiPointerDown &&*/ isListeningForGestures) {
                    touchUp(event, false);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (/*!isMultiPointerDown &&*/ isListeningForGestures) {
                    touchUp(event, true);
                }
                break;
        }
        return true;
    }


    private void touchDown(MotionEvent event) {
        isListeningForGestures = true;

        float x = event.getX();
        float y = event.getY();

        downX = x;
        downY = y;

        path.moveTo(x, y);
        Log.e("JQK", "G Move to ( " + x + " , " + y + ")");

        final int border = invalidateExtraBorder;
        invalidRect.set((int) x - border, (int) y - border, (int) x + border, (int) y + border);

        curveEndX = x;
        curveEndY = y;

        firstPointerDownTime = System.currentTimeMillis();
        handler.postDelayed(checkPointerDownRunnable, 150);
        Log.d(TAG, "touchDown: firstPointDownTime" + firstPointerDownTime);

        // pass the event to handlers
        if (listener != null) {
            listener.onGestureStarted(event);
        }
    }

    private void touchPointerDown() {
        Log.d(TAG, "touchPointerDown:  " + System.currentTimeMillis());
        isMultiPointerDown = System.currentTimeMillis() - firstPointerDownTime < 150;
        if (isMultiPointerDown) {
            handler.removeCallbacks(checkPointerDownRunnable);
        }
        Log.d(TAG, "touchPointerDown: isPointerDown " + isMultiPointerDown);
    }

    private void touchMove(MotionEvent event) {
        Rect areaToRefresh;

        final float x = event.getX();
        final float y = event.getY();

        final float previousX = downX;
        final float previousY = downY;

        final float dx = Math.abs(x - previousX);
        final float dy = Math.abs(y - previousY);

        if ((dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)) {
            areaToRefresh = invalidRect;

            // start with the curve end
            final int border = invalidateExtraBorder;
            areaToRefresh.set((int) curveEndX - border, (int) curveEndY - border,
                    (int) curveEndX + border, (int) curveEndY + border);

            float cX = curveEndX = (x + previousX) / 2;
            float cY = curveEndY = (y + previousY) / 2;
            path.lineTo(x, y);
//            path.quadTo(previousX, previousY, cX, cY);

            // union with the control point of the new curve
            areaToRefresh.union((int) previousX - border, (int) previousY - border,
                    (int) previousX + border, (int) previousY + border);

            // union with the end point of the new curve
            areaToRefresh.union((int) cX - border, (int) cY - border,
                    (int) cX + border, (int) cY + border);

            downX = x;
            downY = y;

            Log.e("DDD", "changed = "+changed);
            // pass the event to handlers
            if (listener != null) {
                listener.onGesture(event);
                listener.onPathChange(areaToRefresh, isMultiPointerDown || changed ? null : path);
            }
        }
    }

    private void touchUp(MotionEvent event, boolean cancel) {
        if (rewind)
            path.reset();
        isListeningForGestures = false;
        firstPointerDownTime = 0;
        isMultiPointerDown = true;

        if (!cancel) {
            // pass the event to handlers
            if (listener != null) {
                listener.onGestureEnded(event);
            }
        } else {
            if (listener != null) {
                listener.onGestureCancelled(event);
            }
        }

        if (listener != null) {
            listener.onPathChange(null, path);
        }
    }


    private final Runnable checkPointerDownRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "checkPointerDownRunnable run: isPointerDown false");
            isMultiPointerDown = false;
        }
    };


    public interface PathGestureDetectorListener {

        void onPathChange(@Nullable Rect invalidRect, Path changePath);

        void onGestureStarted(MotionEvent event);

        void onGesture(MotionEvent event);

        void onGestureEnded(MotionEvent event);

        void onGestureCancelled(MotionEvent event);
    }

    public static class SimpleGestureDetectorListener implements PathGestureDetectorListener {

        @Override
        public void onPathChange(@Nullable Rect invalidRect, Path changePath) {

        }

        @Override
        public void onGestureStarted(MotionEvent event) {
        }

        @Override
        public void onGesture(MotionEvent event) {
        }

        @Override
        public void onGestureEnded(MotionEvent event) {
        }

        @Override
        public void onGestureCancelled(MotionEvent event) {
        }
    }
}
