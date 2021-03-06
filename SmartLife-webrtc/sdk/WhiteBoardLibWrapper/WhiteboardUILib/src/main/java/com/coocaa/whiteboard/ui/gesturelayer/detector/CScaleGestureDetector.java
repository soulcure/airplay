package com.coocaa.whiteboard.ui.gesturelayer.detector;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;

/**
 * 屏蔽单指一段时间后再加入其他手指认为时双指的逻辑
 *  {@link ScaleGestureDetector}
 */
public class CScaleGestureDetector {

    private static final String TAG = "ScaleGestureWrapDetector";

    public interface OnScaleGestureListener {

        public boolean onScale(CScaleGestureDetector detector);

        public boolean onScaleBegin(CScaleGestureDetector detector);

        public void onScaleEnd(CScaleGestureDetector detector);
        void onScaleDone();
    }

    public static class SimpleOnScaleGestureListener implements OnScaleGestureListener {

        public boolean onScale(CScaleGestureDetector detector) {
            return false;
        }

        public boolean onScaleBegin(CScaleGestureDetector detector) {
            return true;
        }

        public void onScaleEnd(CScaleGestureDetector detector) {
            // Intentionally empty
        }

        @Override
        public void onScaleDone() {

        }
    }

    private final Context mContext;
    private final OnScaleGestureListener mListener;

    private float mFocusX;
    private float mFocusY;

    private boolean mQuickScaleEnabled;
    private boolean mStylusScaleEnabled;

    private float mCurrSpan;
    private float mPrevSpan;
    private float mInitialSpan;
    private float mCurrSpanX;
    private float mCurrSpanY;
    private float mPrevSpanX;
    private float mPrevSpanY;
    private long mCurrTime;
    private long mPrevTime;
    private boolean mInProgress;
    private int mSpanSlop;
    private int mMinSpan;

    private final Handler mHandler;

    private float mAnchoredScaleStartX;
    private float mAnchoredScaleStartY;
    private int mAnchoredScaleMode = ANCHORED_SCALE_MODE_NONE;

    private static final long TOUCH_STABILIZE_TIME = 128; // ms
    private static final float SCALE_FACTOR = .5f;
    private static final int ANCHORED_SCALE_MODE_NONE = 0;
    private static final int ANCHORED_SCALE_MODE_DOUBLE_TAP = 1;
    private static final int ANCHORED_SCALE_MODE_STYLUS = 2;

    private long firstPointDownTime; //第一个手指点下的时间
    private boolean isMultiPointTouch = true; //是否是多点触控

    private boolean hasScale = false;


    private GestureDetector mGestureDetector;

    private boolean mEventBeforeOrAboveStartingGestureEvent;

    public CScaleGestureDetector(Context context, OnScaleGestureListener listener) {
        this(context, listener, null);
    }

    public CScaleGestureDetector(Context context, OnScaleGestureListener listener,
                                 Handler handler) {
        mContext = context;
        mListener = listener;
        mSpanSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;

        mMinSpan = 2;
        mHandler = handler;
        // Quick scale is enabled by default after JB_MR2
        final int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        if (targetSdkVersion > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setQuickScaleEnabled(true);
        }
        // Stylus scale is enabled by default after LOLLIPOP_MR1
        if (targetSdkVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            setStylusScaleEnabled(true);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {

        mCurrTime = event.getEventTime();

        final int action = event.getActionMasked();

        // Forward the event to check for double tap gesture
        if (mQuickScaleEnabled) {
            mGestureDetector.onTouchEvent(event);
        }

        final int count = event.getPointerCount();
        final boolean isStylusButtonDown =
                (event.getButtonState() & MotionEvent.BUTTON_STYLUS_PRIMARY) != 0;

        final boolean anchoredScaleCancelled =
                mAnchoredScaleMode == ANCHORED_SCALE_MODE_STYLUS && !isStylusButtonDown;
        final boolean streamComplete = action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_CANCEL || anchoredScaleCancelled;

        if (action == MotionEvent.ACTION_DOWN || streamComplete) {
            // Reset any scale in progress with the listener.
            // If it's an ACTION_DOWN we're beginning a new event stream.
            // This means the app probably didn't give us all the events. Shame on it.
            if (mInProgress) {
                mListener.onScaleEnd(this);
                mInProgress = false;
                mInitialSpan = 0;
                mAnchoredScaleMode = ANCHORED_SCALE_MODE_NONE;
            } else if (inAnchoredScaleMode() && streamComplete) {
                mInProgress = false;
                mInitialSpan = 0;
                mAnchoredScaleMode = ANCHORED_SCALE_MODE_NONE;
            }

            if (streamComplete) {
                return true;
            }
        }

        if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            hasScale = false;
        }

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_UP) {
            firstPointDownTime = System.currentTimeMillis();
        }

        if(action == MotionEvent.ACTION_POINTER_DOWN){
            isMultiPointTouch = System.currentTimeMillis() - firstPointDownTime <150;
        }

        if(action == MotionEvent.ACTION_UP){
            firstPointDownTime = 0;
        }

        if (!mInProgress && mStylusScaleEnabled && !inAnchoredScaleMode()
                && !streamComplete && isStylusButtonDown) {
            // Start of a button scale gesture
            mAnchoredScaleStartX = event.getX();
            mAnchoredScaleStartY = event.getY();
            mAnchoredScaleMode = ANCHORED_SCALE_MODE_STYLUS;
            mInitialSpan = 0;
        }

        final boolean configChanged = action == MotionEvent.ACTION_DOWN ||
                action == MotionEvent.ACTION_POINTER_UP ||
                action == MotionEvent.ACTION_POINTER_DOWN || anchoredScaleCancelled;

        final boolean pointerUp = action == MotionEvent.ACTION_POINTER_UP;
        final int skipIndex = pointerUp ? event.getActionIndex() : -1;

        // Determine focal point
        float sumX = 0, sumY = 0;
        final int div = pointerUp ? count - 1 : count;
        final float focusX;
        final float focusY;
        if (inAnchoredScaleMode()) {
            // In anchored scale mode, the focal pt is always where the double tap
            // or button down gesture started
            focusX = mAnchoredScaleStartX;
            focusY = mAnchoredScaleStartY;
            if (event.getY() < focusY) {
                mEventBeforeOrAboveStartingGestureEvent = true;
            } else {
                mEventBeforeOrAboveStartingGestureEvent = false;
            }
        } else {
            for (int i = 0; i < count; i++) {
                if (skipIndex == i) continue;
                sumX += event.getX(i);
                sumY += event.getY(i);
            }

            focusX = sumX / div;
            focusY = sumY / div;
        }

        // Determine average deviation from focal point
        float devSumX = 0, devSumY = 0;
        for (int i = 0; i < count; i++) {
            if (skipIndex == i) continue;

            // Convert the resulting diameter into a radius.
            devSumX += Math.abs(event.getX(i) - focusX);
            devSumY += Math.abs(event.getY(i) - focusY);
        }
        final float devX = devSumX / div;
        final float devY = devSumY / div;

        // Span is the average distance between touch points through the focal point;
        // i.e. the diameter of the circle with a radius of the average deviation from
        // the focal point.
        final float spanX = devX * 2;
        final float spanY = devY * 2;
        final float span;
        if (inAnchoredScaleMode()) {
            span = spanY;
        } else {
            span = (float) Math.hypot(spanX, spanY);
        }

        // Dispatch begin/end events as needed.
        // If the configuration changes, notify the app to reset its current state by beginning
        // a fresh scale event stream.
        final boolean wasInProgress = mInProgress;
        mFocusX = focusX;
        mFocusY = focusY;
        if (!inAnchoredScaleMode() && mInProgress && (span < mMinSpan || configChanged)) {
            mListener.onScaleEnd(this);
            mInProgress = false;
            mInitialSpan = span;
        }
        if (configChanged) {
            mPrevSpanX = mCurrSpanX = spanX;
            mPrevSpanY = mCurrSpanY = spanY;
            mInitialSpan = mPrevSpan = mCurrSpan = span;
        }

        final int minSpan = inAnchoredScaleMode() ? mSpanSlop : mMinSpan;
        if (!mInProgress && span >= minSpan &&
                (wasInProgress || Math.abs(span - mInitialSpan) > mSpanSlop)) {
            mPrevSpanX = mCurrSpanX = spanX;
            mPrevSpanY = mCurrSpanY = spanY;
            mPrevSpan = mCurrSpan = span;
            mPrevTime = mCurrTime;
            mInProgress = mListener.onScaleBegin(this);
        }

        // Handle motion; focal point and span/scale factor are changing.
        if (action == MotionEvent.ACTION_MOVE) {
            mCurrSpanX = spanX;
            mCurrSpanY = spanY;
            mCurrSpan = span;

            boolean updatePrev = true;

            if (mInProgress) {
                if(isMultiPointTouch) {
                    hasScale = true;
                    updatePrev = mListener.onScale(this);
                }
            }

            if (updatePrev) {
                mPrevSpanX = mCurrSpanX;
                mPrevSpanY = mCurrSpanY;
                mPrevSpan = mCurrSpan;
                mPrevTime = mCurrTime;
            }
        } else if(action == MotionEvent.ACTION_POINTER_UP) {
            if(hasScale) {
                mListener.onScaleDone();
                hasScale = false;
            }
        }

        return true;
    }

    private boolean inAnchoredScaleMode() {
        return mAnchoredScaleMode != ANCHORED_SCALE_MODE_NONE;
    }


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
            mGestureDetector = new GestureDetector(mContext, gestureListener, mHandler);
        }
    }

    public boolean isQuickScaleEnabled() {
        return mQuickScaleEnabled;
    }

    public void setStylusScaleEnabled(boolean scales) {
        mStylusScaleEnabled = scales;
    }

    public boolean isStylusScaleEnabled() {
        return mStylusScaleEnabled;
    }

    public boolean isInProgress() {
        return mInProgress;
    }

    public float getFocusX() {
        return mFocusX;
    }


    public float getFocusY() {
        return mFocusY;
    }

    public float getCurrentSpan() {
        return mCurrSpan;
    }

    public float getCurrentSpanX() {
        return mCurrSpanX;
    }

    public float getCurrentSpanY() {
        return mCurrSpanY;
    }

    public float getPreviousSpan() {
        return mPrevSpan;
    }

    public float getPreviousSpanX() {
        return mPrevSpanX;
    }

    public float getPreviousSpanY() {
        return mPrevSpanY;
    }

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

    public long getTimeDelta() {
        return mCurrTime - mPrevTime;
    }

    public long getEventTime() {
        return mCurrTime;
    }
}
