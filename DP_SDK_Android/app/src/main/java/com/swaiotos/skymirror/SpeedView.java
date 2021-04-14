package com.swaiotos.skymirror;

import android.content.Context;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;


public class SpeedView extends FrameLayout {
    private Context mContext;
    public TextView downText;
    public TextView upText;
    private WindowManager windowManager;
    private float preX, preY;

    public SpeedView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        //a view inflate itself, that's funny
        inflate(mContext, R.layout.speed_layout, this);
        downText = (TextView) findViewById(R.id.speed_down);
        upText = (TextView) findViewById(R.id.speed_up);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preX = event.getRawX();
                preY = event.getRawY();
                return true;
            case MotionEvent.ACTION_MOVE:
                float x = event.getRawX();
                float y = event.getRawY();
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) getLayoutParams();
                params.x += x - preX;
                params.y += y - preY;
                windowManager.updateViewLayout(this, params);
                preX = x;
                preY = y;
                return true;
            default:
                break;

        }
        return super.onTouchEvent(event);
    }

    public int getViewX() {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) getLayoutParams();
        return params.x;
    }


    public int getViewY() {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) getLayoutParams();
        return params.y;
    }
}
