package com.coocaa.tvpi.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshKernel;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;
import com.scwang.smart.refresh.layout.util.SmartUtil;

import static com.airbnb.lottie.LottieDrawable.INFINITE;

public class SmartRefreshHeader extends LinearLayout implements RefreshHeader {
    private LottieAnimationView animationView;

    public SmartRefreshHeader(Context context) {
        this(context,null);
    }

    public SmartRefreshHeader(Context context,boolean isVisibility){
        this(context,null);
        animationView.setVisibility(INVISIBLE);
    }

    public SmartRefreshHeader(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER);
        animationView = new LottieAnimationView(getContext());
        animationView.setAnimation("pull_down_refresh.json");
        animationView.setRepeatCount(INFINITE);
        addView(animationView, SmartUtil.dp2px(70), SmartUtil.dp2px(44));
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @NonNull
    @Override
    public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.Translate;
    }

    @Override
    public void setPrimaryColors(int... colors) {

    }

    @Override
    public void onInitialized(@NonNull RefreshKernel kernel, int height, int maxDragHeight) {

    }

    @Override
    public void onMoving(boolean isDragging, float percent, int offset, int height, int maxDragHeight) {

    }

    @Override
    public void onReleased(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {

    }

    @Override
    public void onStartAnimator(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {
        animationView.setMinAndMaxFrame(9, 17);
        animationView.setRepeatCount(INFINITE);
        animationView.playAnimation();
    }

    @Override
    public int onFinish(@NonNull RefreshLayout refreshLayout, boolean success) {
        animationView.cancelAnimation();//停止动画
        return 100;//延迟100毫秒之后再弹回
    }

    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {

    }

    @Override
    public boolean isSupportHorizontalDrag() {
        return false;
    }

    @Override
    public void onStateChanged(@NonNull RefreshLayout refreshLayout, @NonNull RefreshState oldState, @NonNull RefreshState newState) {
        switch (newState) {
            case None:
            case PullDownToRefresh:
                animationView.setMinAndMaxFrame(0, 0);
                animationView.setRepeatCount(0);
                animationView.playAnimation();
                break;
            case Refreshing:

                break;
            case ReleaseToRefresh:
                animationView.setMinAndMaxFrame(0, 9);
                animationView.setRepeatCount(0);
                animationView.playAnimation();
                break;
        }
    }
}
