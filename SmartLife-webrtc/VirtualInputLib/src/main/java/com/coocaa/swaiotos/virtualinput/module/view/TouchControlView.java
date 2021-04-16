package com.coocaa.swaiotos.virtualinput.module.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

/**
 * @ClassName TouchControlView
 * @Description 控制器下方区域同时响应点击和触摸的父布局(解决ViewGroup滑动与子View点击事件冲突)
 * @User heni
 * @Date 2021/2/2
 */
public class TouchControlView extends RelativeLayout {

    private OnSlideCtrlListener mOnCtrlListener;

    public void setOnSlideCtrlListener(OnSlideCtrlListener listener) {
        mOnCtrlListener = listener;
    }

    public TouchControlView(Context context) {
        super(context);
    }

    public TouchControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TouchControlView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private boolean mScrolling;
    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    private float x1 = 0;
    private float x2 = 0;
    private float y1 = 0;
    private float y2 = 0;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //当手指按下的时候
                x1 = event.getX();
                y1 = event.getY();
                mScrolling = false;
                break;
            case MotionEvent.ACTION_MOVE:
                int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                mScrolling = Math.abs(y1 - event.getY()) >= slop;
                break;
            case MotionEvent.ACTION_UP:
                mScrolling = false;
                break;
        }
        return mScrolling;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                //当手指离开的时候
                x2 = event.getX();
                y2 = event.getY();
                if ((y1 - y2) > 50) {
                    //上滑：下一张
                    if (mOnCtrlListener != null) {
                        mOnCtrlListener.next();
                    }
                } else if ((y2 - y1) > 50) {
                    //下滑：上一张
                    if (mOnCtrlListener != null) {
                        mOnCtrlListener.previous();
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public interface OnSlideCtrlListener {
        void previous();

        void next();
    }
}
