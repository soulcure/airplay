package com.coocaa.tvpi.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;
import com.coocaa.tvpilib.R;
import com.liaoinstan.springview.container.BaseHeader;

import static com.airbnb.lottie.LottieDrawable.INFINITE;

/**
 * @ClassName CustomHeader2
 * @Description 新年loading动画
 * @User heni
 * @Date 2019/1/22
 */
public class CustomHeader extends BaseHeader {

    private static final String TAG = CustomHeader.class.getSimpleName();
    private Context context;

    private AnimationDrawable animationRefresh;
    private LottieAnimationView lottieAnimationView;

    public CustomHeader(Context context) {
        this.context = context;
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.custom_header2, viewGroup, true);
        lottieAnimationView = view.findViewById(R.id.loading_animation_view);
        lottieAnimationView.setAnimation("pull_down_refresh.json");
        lottieAnimationView.setRepeatCount(INFINITE);
        return view;
    }

    @Override
    public void onPreDrag(View rootView) {

    }

    @Override
    public void onDropAnim(View rootView, int dy) {

    }

    @Override
    public void onLimitDes(View rootView, boolean upORdown) {
        if (upORdown) {//向上
            lottieAnimationView.setMinAndMaxFrame(0, 0);
            lottieAnimationView.setRepeatCount(0);
            lottieAnimationView.playAnimation();
        } else {//向下
            lottieAnimationView.setMinAndMaxFrame(0, 9);
            lottieAnimationView.setRepeatCount(0);
            lottieAnimationView.playAnimation();
        }
    }

    @Override
    public void onStartAnim() {
        lottieAnimationView.setMinAndMaxFrame(9, 17);
        lottieAnimationView.setRepeatCount(INFINITE);
        lottieAnimationView.playAnimation();
    }

    @Override
    public void onFinishAnim() {
    }

}
