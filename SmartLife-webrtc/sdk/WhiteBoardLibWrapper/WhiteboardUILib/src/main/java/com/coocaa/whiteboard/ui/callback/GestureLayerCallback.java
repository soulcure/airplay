package com.coocaa.whiteboard.ui.callback;

import android.view.MotionEvent;

/**
 * 手势层回调
 */
public interface GestureLayerCallback {
    /**
     * 双指移动画布
     * @param translateX x方向
     * @param translateY y方向
     */
    void onTranslateCanvas(float translateX, float translateY);

    void onTranslateDone();

    /**
     * 双指缩放画布
     * @param scaleFactor 缩放系数
     * @param px 中心x
     * @param py 中心y
     */
    void onScaleCanvas(float scaleFactor, float px, float py);

    void onScaleCanvasDone();

    /**
     * 单指开始
     * @param event MotionEvent
     */
    void onGestureStarted(MotionEvent event);

    /**
     * 单指移动
     * @param event MotionEvent
     */
    void onGesture(MotionEvent event);

    /**
     * 单指结束
     * @param event MotionEvent
     */
    void onGestureEnded(MotionEvent event);

    /**
     * 单指取消
     * @param event MotionEvent
     */
    void onGestureCancelled(MotionEvent event);


    void offsetAndScale(float x, float y, float scale,float cx,float cy);
    void onEventDone(boolean scaleDone,boolean transDone);
}
