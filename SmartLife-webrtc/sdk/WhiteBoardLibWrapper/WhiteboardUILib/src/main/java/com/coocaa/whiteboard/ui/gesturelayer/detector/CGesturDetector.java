package com.coocaa.whiteboard.ui.gesturelayer.detector;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;

import com.coocaa.whiteboard.ui.callback.GestureLayerCallback;
import com.coocaa.whiteboard.ui.gesturelayer.GestureLayerView;

public class CGesturDetector {

    public static final String TAG = "CGesture";
    private static final int INVALID_POINTER_INDEX = -1;

    private float fX, fY = 0; //第一个手指按下坐标
    private float sX, sY = 0; //第二次手指按下坐标
    private float translateX, translateY = 0;
    private int pointerIndex1, pointerIndex2;
    private boolean isFirstTouch;
    private long firstPointDownTime;
    private boolean isMultiPointTouch = true;
    private boolean hasTranslate = false;//记录是否发生过translate变化
    private boolean hasScale = false;
    private long scaleEventDownTime;
    private float midPntX;
    private float midPntY;


    public interface onCGestureListener {
        void onEventDone(boolean scaleDone,boolean transDone);

        void offsetAndScale(float x, float y, float scale,float cx,float cy);
    }

    private CGestureListenerImpl listener;
    private Context mContext;
    private int mSpanSlop;
    private int mMinSpan;


    public static class CGestureListenerImpl extends SimpleOnScaleGestureListenerImpl implements onCGestureListener {

        private GestureLayerCallback gestureLayerCallback;

        public void setCallback(GestureLayerCallback gestureLayerCallback) {
            this.gestureLayerCallback = gestureLayerCallback;
        }

        @Override
        public void onEventDone(boolean scaleDone,boolean transDone) {
            Log.e("MMM","onEventDone...");
//            gestureLayerCallback.onScaleCanvasDone();
//            gestureLayerCallback.onTranslateDone();
            gestureLayerCallback.onEventDone(scaleDone, transDone);
        }

        @Override
        public void offsetAndScale(float x, float y, float scale,float cx,float cy) {
            gestureLayerCallback.offsetAndScale(x, y, scale,cx,cy);
        }
    }


    private GestureLayerView view;
    public CGesturDetector(Context context, CGestureListenerImpl listener, GestureLayerView view) {
        this.listener = listener;
        pointerIndex1 = INVALID_POINTER_INDEX;
        pointerIndex2 = INVALID_POINTER_INDEX;

        this.view = view;
        mContext = context;
        mSpanSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;

        mMinSpan = 2;
        final int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        if (targetSdkVersion > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setQuickScaleEnabled(true);
        }
        // Stylus scale is enabled by default after LOLLIPOP_MR1
        if (targetSdkVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            setStylusScaleEnabled(true);
        }
    }

    public void setStylusScaleEnabled(boolean scales) {
        mStylusScaleEnabled = scales;
    }

    private GestureDetector mGestureDetector;
    private boolean mStylusScaleEnabled;
    private float mAnchoredScaleStartX, mAnchoredScaleStartY;
    private static final float SCALE_FACTOR = .5f;
    private static final int ANCHORED_SCALE_MODE_NONE = 0;
    private static final int ANCHORED_SCALE_MODE_DOUBLE_TAP = 1;
    private static final int ANCHORED_SCALE_MODE_STYLUS = 2;
    private int mAnchoredScaleMode;
    private boolean mInProgress;

    public void setQuickScaleEnabled(boolean scales) {
        mQuickScaleEnabled = scales;
        if (mQuickScaleEnabled && mGestureDetector == null) {
            GestureDetector.SimpleOnGestureListener gestureListener =
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            // Double tap: start watching for a swipe
                            mAnchoredScaleStartX = e.getX();
                            mAnchoredScaleStartY = e.getY();
                            mAnchoredScaleMode = ANCHORED_SCALE_MODE_DOUBLE_TAP;
                            return true;
                        }
                    };
            mGestureDetector = new GestureDetector(mContext, gestureListener/*, null*/);
        }
    }

    private boolean inAnchoredScaleMode() {
        return mAnchoredScaleMode != ANCHORED_SCALE_MODE_NONE;
    }

    private boolean mEventBeforeOrAboveStartingGestureEvent;
    private float mCurrSpan, mPrevSpan;

    public float getScaleFactor() {
        if (inAnchoredScaleMode()) {
            // Drag is moving up; the further away from the gesture
            // start, the smaller the span should be, the closer,
            // the larger the span, and therefore the larger the scale
            final boolean scaleUp =
                    (mEventBeforeOrAboveStartingGestureEvent && (mCurrSpan < mPrevSpan)) ||
                            (!mEventBeforeOrAboveStartingGestureEvent && (mCurrSpan > mPrevSpan));
            final float spanDiff = (Math.abs(1 - (mCurrSpan / mPrevSpan)) * SCALE_FACTOR);
            return mPrevSpan <= 0 ? 1 : scaleUp ? (1 + spanDiff) : (1 - spanDiff);
        }
        return mPrevSpan > 0 ? mCurrSpan / mPrevSpan : 1;
    }

    private long mCurrTime;
    private boolean mQuickScaleEnabled;
    private float mInitialSpan, mFocusX, mFocusY, mPrevSpanX, mPrevSpanY, mCurrSpanX, mCurrSpanY;
    private long mPrevTime;

    public interface OnScaleGestureListener {

        boolean onScale(CGesturDetector detector);

        boolean onScaleBegin(CGesturDetector detector);

        void onScaleEnd(CGesturDetector detector);

        void onScaleDone();
    }

    public static class SimpleOnScaleGestureListenerImpl implements CGesturDetector.OnScaleGestureListener {

        public boolean onScale(CGesturDetector detector) {
            return false;
        }

        public boolean onScaleBegin(CGesturDetector detector) {
            return true;
        }

        public void onScaleEnd(CGesturDetector detector) {
            // Intentionally empty
        }

        @Override
        public void onScaleDone() {

        }
    }


    ScaleGestureDetector mScaleGestureDetector ;
    private float scale = 1f;
    private boolean isScale = false;
    int totalX = 0;
    int totalY = 0;
    public boolean onTouchEvent(MotionEvent event) {
        if(mScaleGestureDetector == null){
            mScaleGestureDetector = new ScaleGestureDetector(mContext,new ScaleGestureDetector.SimpleOnScaleGestureListener(){
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    scale *= detector.getScaleFactor();
                    Log.e("PPP","onScale = "+scale);
                    isScale = true;
                    return true;
                }

                @Override
                public void onScaleEnd(ScaleGestureDetector detector) {
                    super.onScaleEnd(detector);
//                    scale = 1f;
                    isScale = false;
                    Log.e("PPP","onScaleEnd...");
                }
            });
        }
        mScaleGestureDetector.onTouchEvent(event);

        if (event.getPointerCount() > 1) {
            midPntX = (event.getX(0) + event.getX(1)) / 2;
            midPntY = (event.getY(0) + event.getY(1)) / 2;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                fX = event.getX();
                fY = event.getY();
                pointerIndex1 = event.findPointerIndex(event.getPointerId(0));
                translateX = 0;
                translateY = 0;
                totalX = totalY = 0;
                isFirstTouch = true;
                firstPointDownTime = System.currentTimeMillis();
                hasTranslate = false;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                sX = event.getX();
                sY = event.getY();
                pointerIndex2 = event.findPointerIndex(event.getPointerId(event.getActionIndex()));
                translateX = 0;
                translateY = 0;
                isFirstTouch = true;
                Log.d(TAG, "onTouchEvent:isMultiPointTouch " + (System.currentTimeMillis() - firstPointDownTime));
                isMultiPointTouch = System.currentTimeMillis() - firstPointDownTime < 150;
                hasTranslate = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (pointerIndex1 != INVALID_POINTER_INDEX
                        && pointerIndex2 != INVALID_POINTER_INDEX
                        && event.getPointerCount() > pointerIndex2
                        && isMultiPointTouch) {
                    float nfX = event.getX(pointerIndex1);
                    float nfY = event.getY(pointerIndex1);
                    float nsX = event.getX(pointerIndex2);
                    float nsY = event.getY(pointerIndex2);

                    if (isFirstTouch) {
                        translateX = 0;
                        translateY = 0;
                        isFirstTouch = false;
                    } else {
                        calculateTranslate(fX, fY, nfX, nfY, sX, sY, nsX, nsY);
                        totalX+=translateX;
                        totalY+=translateY;

                        if (listener != null) {
                            if (translateX != 0 && translateY != 0) {
                                hasTranslate = true;
                            }

                            listener.offsetAndScale(translateX, translateY,scale,midPntX,midPntY);
                        }
                        fX = nfX;
                        fY = nfY;
                        sX = nsX;
                        sY = nsY;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                pointerIndex1 = INVALID_POINTER_INDEX;
                firstPointDownTime = 0;
                isMultiPointTouch = true;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (hasTranslate)
                {
                    pointerIndex2 = INVALID_POINTER_INDEX;
                    firstPointDownTime = System.currentTimeMillis();
                }
                listener.onEventDone(hasScale,hasTranslate);
                if (hasScale)
                    hasScale = false;
                if (hasTranslate)
                    hasTranslate = false;
                break;
        }
        return true;
    }



//
//    public boolean onTouchEvent(@NonNull MotionEvent event) {
//        mCurrTime = event.getEventTime();
//
//        final int action = event.getActionMasked();
//
//        // Forward the event to check for double tap gesture
//        if (mQuickScaleEnabled) {
//            mGestureDetector.onTouchEvent(event);
//        }
//
//        final int count = event.getPointerCount();
//        final boolean isStylusButtonDown =
//                (event.getButtonState() & MotionEvent.BUTTON_STYLUS_PRIMARY) != 0;
//
//        final boolean anchoredScaleCancelled =
//                mAnchoredScaleMode == ANCHORED_SCALE_MODE_STYLUS && !isStylusButtonDown;
//        final boolean streamComplete = action == MotionEvent.ACTION_UP ||
//                action == MotionEvent.ACTION_CANCEL || anchoredScaleCancelled;
//
//        if (action == MotionEvent.ACTION_DOWN || streamComplete) {
//            if (mInProgress) {
//                listener.onScaleEnd(this);
//                mInProgress = false;
//                mInitialSpan = 0;
//                mAnchoredScaleMode = ANCHORED_SCALE_MODE_NONE;
//            } else if (inAnchoredScaleMode() && streamComplete) {
//                mInProgress = false;
//                mInitialSpan = 0;
//                mAnchoredScaleMode = ANCHORED_SCALE_MODE_NONE;
//            }
//
//            if (streamComplete) {
//                return true;
//            }
//        }
//
//        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
//            hasScale = false;
//        }
//
//        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_UP) {
//            firstPointDownTime = System.currentTimeMillis();
//        }
//
//        if (action == MotionEvent.ACTION_POINTER_DOWN) {
//            isMultiPointTouch = System.currentTimeMillis() - firstPointDownTime < 150;
//        }
//
//        if (action == MotionEvent.ACTION_UP) {
//            firstPointDownTime = 0;
//        }
//
//        if (!mInProgress && mStylusScaleEnabled && !inAnchoredScaleMode()
//                && !streamComplete && isStylusButtonDown) {
//            // Start of a button scale gesture
//            mAnchoredScaleStartX = event.getX();
//            mAnchoredScaleStartY = event.getY();
//            mAnchoredScaleMode = ANCHORED_SCALE_MODE_STYLUS;
//            mInitialSpan = 0;
//        }
//
//        final boolean configChanged = action == MotionEvent.ACTION_DOWN ||
//                action == MotionEvent.ACTION_POINTER_UP ||
//                action == MotionEvent.ACTION_POINTER_DOWN || anchoredScaleCancelled;
//
//        final boolean pointerUp = action == MotionEvent.ACTION_POINTER_UP;
//        final int skipIndex = pointerUp ? event.getActionIndex() : -1;
//
//        // Determine focal point
//        float sumX = 0, sumY = 0;
//        final int div = pointerUp ? count - 1 : count;
//        final float focusX;
//        final float focusY;
//        if (inAnchoredScaleMode()) {
//            // In anchored scale mode, the focal pt is always where the double tap
//            // or button down gesture started
//            focusX = mAnchoredScaleStartX;
//            focusY = mAnchoredScaleStartY;
//            if (event.getY() < focusY) {
//                mEventBeforeOrAboveStartingGestureEvent = true;
//            } else {
//                mEventBeforeOrAboveStartingGestureEvent = false;
//            }
//        } else {
//            for (int i = 0; i < count; i++) {
//                if (skipIndex == i) continue;
//                sumX += event.getX(i);
//                sumY += event.getY(i);
//            }
//
//            focusX = sumX / div;
//            focusY = sumY / div;
//        }
//
//        // Determine average deviation from focal point
//        float devSumX = 0, devSumY = 0;
//        for (int i = 0; i < count; i++) {
//            if (skipIndex == i) continue;
//
//            // Convert the resulting diameter into a radius.
//            devSumX += Math.abs(event.getX(i) - focusX);
//            devSumY += Math.abs(event.getY(i) - focusY);
//        }
//        final float devX = devSumX / div;
//        final float devY = devSumY / div;
//
//        // Span is the average distance between touch points through the focal point;
//        // i.e. the diameter of the circle with a radius of the average deviation from
//        // the focal point.
//        final float spanX = devX * 2;
//        final float spanY = devY * 2;
//        final float span;
//        if (inAnchoredScaleMode()) {
//            span = spanY;
//        } else {
//            span = (float) Math.hypot(spanX, spanY);
//        }
//
//        // Dispatch begin/end events as needed.
//        // If the configuration changes, notify the app to reset its current state by beginning
//        // a fresh scale event stream.
//        final boolean wasInProgress = mInProgress;
//        mFocusX = focusX;
//        mFocusY = focusY;
//        if (!inAnchoredScaleMode() && mInProgress && (span < mMinSpan || configChanged)) {
//            listener.onScaleEnd(this);
//            mInProgress = false;
//            mInitialSpan = span;
//        }
//        if (configChanged) {
//            mPrevSpanX = mCurrSpanX = spanX;
//            mPrevSpanY = mCurrSpanY = spanY;
//            mInitialSpan = mPrevSpan = mCurrSpan = span;
//        }
//
//        final int minSpan = inAnchoredScaleMode() ? mSpanSlop : mMinSpan;
//        if (!mInProgress && span >= minSpan &&
//                (wasInProgress || Math.abs(span - mInitialSpan) > mSpanSlop)) {
//            mPrevSpanX = mCurrSpanX = spanX;
//            mPrevSpanY = mCurrSpanY = spanY;
//            mPrevSpan = mCurrSpan = span;
//            mPrevTime = mCurrTime;
//            mInProgress = listener.onScaleBegin(this);
//        }
//
//
//        switch (event.getActionMasked()) {
//            case MotionEvent.ACTION_DOWN:
//                fX = event.getX();
//                fY = event.getY();
//                pointerIndex1 = event.findPointerIndex(event.getPointerId(0));
//                translateX = 0;
//                translateY = 0;
//                isFirstTouch = true;
//                firstPointDownTime = System.currentTimeMillis();
//                hasTranslate = false;
//                break;
//
//            case MotionEvent.ACTION_POINTER_DOWN:
//                sX = event.getX();
//                sY = event.getY();
//                pointerIndex2 = event.findPointerIndex(event.getPointerId(event.getActionIndex()));
//                translateX = 0;
//                translateY = 0;
//                isFirstTouch = true;
//                Log.d(TAG, "onTouchEvent:isMultiPointTouch " + (System.currentTimeMillis() - firstPointDownTime));
//                isMultiPointTouch = System.currentTimeMillis() - firstPointDownTime < 150;
//                hasTranslate = false;
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//
//                mCurrSpanX = spanX;
//                mCurrSpanY = spanY;
//                mCurrSpan = span;
//
//                boolean updatePrev = true;
//
//                if (mInProgress) {
//                    if (isMultiPointTouch) {
//                        hasScale = true;
//                        updatePrev = listener.onScale(this);
//                    }
//                }
//
//                if (updatePrev) {
//                    mPrevSpanX = mCurrSpanX;
//                    mPrevSpanY = mCurrSpanY;
//                    mPrevSpan = mCurrSpan;
//                    mPrevTime = mCurrTime;
//                }
//
//
//                if (pointerIndex1 != INVALID_POINTER_INDEX
//                        && pointerIndex2 != INVALID_POINTER_INDEX
//                        && event.getPointerCount() > pointerIndex2
//                        && isMultiPointTouch) {
//                    float nfX = event.getX(pointerIndex1);
//                    float nfY = event.getY(pointerIndex1);
//                    float nsX = event.getX(pointerIndex2);
//                    float nsY = event.getY(pointerIndex2);
//
//                    if (isFirstTouch) {
//                        translateX = 0;
//                        translateY = 0;
//                        isFirstTouch = false;
//                    } else {
//                        calculateTranslate(fX, fY, nfX, nfY, sX, sY, nsX, nsY);
//                        if (listener != null) {
//                            if (translateX != 0 && translateY != 0) {
//                                hasTranslate = true;
//                                listener.offsetAndScale(translateX, translateY, getScaleFactor());
//                            }
//                        }
//                        fX = nfX;
//                        fY = nfY;
//                        sX = nsX;
//                        sY = nsY;
//                    }
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                pointerIndex1 = INVALID_POINTER_INDEX;
//                firstPointDownTime = 0;
//                isMultiPointTouch = true;
//                break;
//            case MotionEvent.ACTION_POINTER_UP:
//                pointerIndex2 = INVALID_POINTER_INDEX;
//                firstPointDownTime = System.currentTimeMillis();
//                if (listener != null && hasTranslate) {
//                    listener.onEventDone(false);
//                    hasTranslate = false;
//                }
//                if (listener != null && hasScale){
//                    listener.onEventDone(true);
//                    hasScale = false;
//                }
//                break;
//        }
//
//        return true;
//    }


    private void calculateTranslate(float fx1, float fy1, float sx1, float sy1,
                                    float fx2, float fy2, float sx2, float sy2) {
        //屏蔽双指缩放手势产生的位移
        if ((sx1 - fx1 > 0 && sx2 - fx2 < 0)
                || (sx1 - fx1 < 0 && sx2 - fx2 > 0)
                || (sy1 - fy1 > 0 && sy2 - fy2 < 0)
                || (sy1 - fy1 < 0 && sy2 - fy2 > 0)) {
            translateX = 0;
            translateY = 0;
            return;
        }

        translateX = ((sx1 - fx1) + (sx2 - fx2)) / 2;
        translateY = ((sy1 - fy1) + (sy2 - fy2)) / 2;
        Log.d(TAG, "calculateTranslate: translateX " + translateX);
        Log.d(TAG, "calculateTranslate: translateY " + translateY);
    }


}
