package swaiotos.sensor.utils;

import android.animation.ValueAnimator;

import swaiotos.sensor.touch.InputTouchView;
import swaiotos.sensor.touch.TouchPoint;

public class TouchPointAnimator {
    //松开手指后缩小动画
    private ValueAnimator downAnimator;
    //按下手指后放大动画
    private ValueAnimator upAnimator;
    //反馈图标id
    private int id;
    private InputTouchView inputTouchView;

    public TouchPointAnimator(InputTouchView inputTouchView, int id) {
        this.inputTouchView = inputTouchView;
        this.id = id;
    }


    public void startUpAnim() {
        if (upAnimator == null) {
            upAnimator = ValueAnimator.ofFloat(TouchPoint.endScale, TouchPoint.startScale);
            upAnimator.setDuration(300);
            upAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    inputTouchView.setAnimatorValue((float) animation.getAnimatedValue(), id);
                }
            });
        }
        upAnimator.start();
    }

    public void startDownAnim() {
        if (downAnimator == null) {
            downAnimator = ValueAnimator.ofFloat(TouchPoint.startScale, TouchPoint.endScale);
            downAnimator.setDuration(300);
            downAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    inputTouchView.setAnimatorValue((float) animation.getAnimatedValue(), id);
                }
            });
        }
        downAnimator.start();
    }

}
