package com.coocaa.whiteboard.ui.gesturelayer.detector;

import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

//双指移动手势监听
public class TranslateGestureDetector {
    public static final String TAG = TranslateGestureDetector.class.getSimpleName();
    private static final int INVALID_POINTER_INDEX = -1;

    private float fX, fY = 0; //第一个手指按下坐标
    private float sX, sY = 0; //第二次手指按下坐标
    private float translateX, translateY = 0;
    private int pointerIndex1, pointerIndex2;
    private boolean isFirstTouch;
    private long firstPointDownTime;
    private boolean isMultiPointTouch = true;
    private boolean hasTranslate = false;//记录是否发生过translate变化

    private final OnTranslateGestureDetectorListener listener;

    public TranslateGestureDetector(OnTranslateGestureDetectorListener listener) {
        this.listener = listener;
        pointerIndex1 = INVALID_POINTER_INDEX;
        pointerIndex2 = INVALID_POINTER_INDEX;
    }

    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                fX = event.getX();
                fY = event.getY();
                pointerIndex1 = event.findPointerIndex(event.getPointerId(0));
                translateX = 0;
                translateY = 0;
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
                        if (listener != null) {
                            if (translateX != 0 && translateY != 0) {
                                hasTranslate = true;
                                listener.onTranslate(translateX, translateY);
                            }
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
                pointerIndex2 = INVALID_POINTER_INDEX;
                firstPointDownTime = System.currentTimeMillis();
                if (listener != null && hasTranslate) {
                    listener.onTranslateDone();
                    hasTranslate = false;
                }
                break;
        }
        return true;
    }


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


    public interface OnTranslateGestureDetectorListener {

        void onTranslate(float translateX, float translateY);
        void onTranslateDone();
    }
}
