package com.coocaa.swaiotos.virtualinput.module.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class LaserRelativeLayout extends RelativeLayout {
    public LaserRelativeLayout(Context context) {
        this(context, null);
    }

    public LaserRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LaserRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public LaserRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    //分发任务，返回true表示继续向下分发任务，false不继续向下分发任务
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean bool=super.dispatchTouchEvent(ev);
        Log.d("d","[LaserRelativeLayout]<"+ ev.getAction()+">任务需要分派?:"+bool);
        return bool;
    }

    //拦截任务，返回true表示拦截任务，false不拦截任务
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean bool=super.onInterceptTouchEvent(ev);
        Log.d("d","[LaserRelativeLayout]<"+ ev.getAction()+">任务需要拦截?:"+bool);

        return bool;
    }

    //处理任务，返回true表示该视图成功处理任务，false该视图无法处理任务
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean bool=super.onTouchEvent(event);
        Log.d("d","[LaserRelativeLayout]<"+ event.getAction()+">自己处理任务?:"+bool);
        return bool;
    }

}
