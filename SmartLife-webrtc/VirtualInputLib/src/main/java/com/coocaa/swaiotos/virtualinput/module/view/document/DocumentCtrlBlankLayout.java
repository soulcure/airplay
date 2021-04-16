package com.coocaa.swaiotos.virtualinput.module.view.document;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

/**
 * @Description: 文档控制空白区域父布局(解决ViewGrop滑动与子View点击事件冲突)
 * @Author: wzh
 * @CreateDate: 1/28/21
 */
public class DocumentCtrlBlankLayout extends RelativeLayout {
    private OnSlideCtrlListener mOnCtrlListener;
    private String mCurFormat;

    public DocumentCtrlBlankLayout(Context context) {
        super(context);
    }

    public DocumentCtrlBlankLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnSlideCtrlListener(OnSlideCtrlListener listener) {
        mOnCtrlListener = listener;
    }

    public void setDocFormat(String format) {
        mCurFormat = format;
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
                mScrolling = Math.abs(x1 - event.getX()) >= slop || Math.abs(y1 - event.getY()) >= slop;
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
                if (!TextUtils.isEmpty(mCurFormat) && mCurFormat.equals("PPT")) {
                    //PPT 操控方式：上下左右滑动
                    if ((x1 - x2) > 50 || (y1 - y2) > 50) {
                        //左滑、上滑：下一张
                        if (mOnCtrlListener != null) {
                            mOnCtrlListener.nextPage();
                        }
                    } else if ((x2 - x1) > 50 || (y2 - y1) > 50) {
                        //右滑、下滑：上一张
                        if (mOnCtrlListener != null) {
                            mOnCtrlListener.prePage();
                        }
                    }
                } else {
                    //PDF、Word操控方式：上下滑动
                    if (mOnCtrlListener != null) {
                        if (y1 - y2 > 50) {
                            //上滑：下一张
                            mOnCtrlListener.nextPage();
                        } else if (y2 - y1 > 50) {
                            //下滑：上一张
                            mOnCtrlListener.prePage();
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public interface OnSlideCtrlListener {
        void prePage();

        void nextPage();
    }
}
