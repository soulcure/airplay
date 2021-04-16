package swaiotos.channel.iot.tv.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * founter：符乃辉
 * time：2018/12/6
 * email:wizarddev@163.com
 * description:
 */
public class AutoScrollRecyclerView extends RecyclerView {
    private int mState;
    private OnScrollListener mScrollListener;

    public AutoScrollRecyclerView(Context context) {
        this(context,null);
    }

    public AutoScrollRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AutoScrollRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mScrollListener = new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mState = newState;
            }
        };
        //添加滑动监听
        addOnScrollListener(mScrollListener);
    }

    //判断是否拦截事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return mState != 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
                return mState == 0;
            case MotionEvent.ACTION_MOVE:
                return false;
            case MotionEvent.ACTION_POINTER_UP:
                return false;
        }
        return false;
    }

}
