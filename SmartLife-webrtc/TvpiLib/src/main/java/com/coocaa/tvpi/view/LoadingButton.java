package com.coocaa.tvpi.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.coocaa.tvpilib.R;


/**
 * 一个简单的点击显示进度条的组合控件按钮。。。
 * created by songxing on 2019/12/1
 */
//todo 通过自定义view实现该效果
public class LoadingButton extends FrameLayout {
    interface STATUS {
        int IDE = 0;
        int LOADING = 1;
    }

    private int curStatus = STATUS.IDE;

    private TextView tvButtonText;
    private ImageView ivLoadingView;
    private FrameLayout backgroundLayout;
    private OnLoadingListener onLoadingListener;
    private RotateAnimation rotateAnimation;
    private boolean useXmlBg = false;
    private int backgroundResId;
    private int backgroundUnfocusResId;

    private String ideText;
    private String loadingText;

    public LoadingButton( Context context) {
        super(context);
        init(null, 0);
    }

    public LoadingButton( Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public LoadingButton( Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LoadingButton( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleRes);
    }


    private void init(AttributeSet attrs, int defStyleRes) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LoadingButton, defStyleRes, 0);
        ideText = (String) a.getText(R.styleable.LoadingButton_loading_button_text);
        loadingText = (String) a.getText(R.styleable.LoadingButton_loading_button_loading_text);
        ColorStateList textColor = a.getColorStateList(R.styleable.LoadingButton_loading_button_textColor);
        if (textColor == null) {
            textColor = new ColorStateList(new int[][]{{android.R.attr.state_enabled}}, new int[]{getResources().getColor(R.color.white)});
        }
        int textSize = a.getDimensionPixelSize(R.styleable.LoadingButton_loading_button_textSize, getResources().getDimensionPixelSize(R.dimen.font_16));
        backgroundResId = a.getResourceId(R.styleable.LoadingButton_loading_button_background, R.drawable.bg_b_8_round_50);
        backgroundUnfocusResId = a.getResourceId(R.styleable.LoadingButton_loading_button_background_unfocus, R.drawable.bg_b_8_round_50);
        a.recycle();

        LayoutInflater.from(getContext()).inflate(R.layout.layout_loading_button, this, true);
        backgroundLayout = findViewById(R.id.app_button_layout);
        tvButtonText = findViewById(R.id.app_button_text);
        ivLoadingView = findViewById(R.id.app_button_loading);
        tvButtonText.setText(ideText);
        tvButtonText.setTextColor(textColor);
        tvButtonText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        tvButtonText.setEnabled(isEnabled());
        backgroundLayout.setBackgroundResource(backgroundResId);
    }


    /**
     * 开始加载
     */
    public void start() {
        if (curStatus != STATUS.LOADING){
            toLoading();
            startLoadingAnim();
            if (onLoadingListener != null) {
                onLoadingListener.onLoadingStart();
            }
        }
    }


    /**
     * 加载完成
     */
    public void complete() {
        end(false);
    }

    /**
     * 加载错误
     */
    public void fail() {
        end(true);
    }

    /**
     * 结束加载
     */
    private void end(boolean isFail) {
        stopLoadingAnim();
        toIde();
        if (onLoadingListener != null) {
            if (isFail)
                onLoadingListener.onLoadingFailed();
            else
                onLoadingListener.onLoadingCompleted();
        }
    }

    private void toIde() {
        curStatus = STATUS.IDE;
        setEnabled(true);
        if (tvButtonText != null) tvButtonText.setText(ideText);;
        if (ivLoadingView != null) ivLoadingView.setVisibility(GONE);

    }

    private void toLoading(){
        curStatus = STATUS.LOADING;
        setEnabled(false);
        if (tvButtonText != null) tvButtonText.setText(loadingText);;
        if (ivLoadingView != null) ivLoadingView.setVisibility(VISIBLE);
    }


    private void startLoadingAnim() {
        if (rotateAnimation == null) {
            rotateAnimation = new RotateAnimation(0f, 359f, android.view.animation.Animation.RELATIVE_TO_SELF,
                    0.5f, android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(300);//设置动画持续时间
            LinearInterpolator lin = new LinearInterpolator();
            rotateAnimation.setInterpolator(lin);
            rotateAnimation.setRepeatCount(-1);//设置重复次数
            rotateAnimation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
            rotateAnimation.setStartOffset(10);//执行前的等待时间
        }
        if (ivLoadingView != null) {
            ivLoadingView.startAnimation(rotateAnimation);
        }
    }


    private void stopLoadingAnim() {
        if (rotateAnimation != null) ivLoadingView.clearAnimation();
    }


    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            tvButtonText.setTextColor(getResources().getColor(R.color.color_white));
            backgroundLayout.setBackgroundResource(useXmlBg?backgroundResId:R.drawable.bg_loading_button_enable);
        } else {
            tvButtonText.setTextColor(getResources().getColor(R.color.color_white_40));
            backgroundLayout.setBackgroundResource(useXmlBg?backgroundUnfocusResId:R.drawable.bg_loading_button_unable);
        }
    }

    public LoadingButton setOnLoadingListener(OnLoadingListener onLoadingListener) {
        this.onLoadingListener = onLoadingListener;
        return this;
    }

    public void setUseXmlBg(boolean useXmlBg) {
        this.useXmlBg = useXmlBg;
    }

    public interface OnLoadingListener {
        void onLoadingStart();

        void onLoadingCompleted();

        void onLoadingFailed();
    }

    public static class OnLoadingListenerAdapter implements OnLoadingListener {

        @Override
        public void onLoadingStart() {

        }

        @Override
        public void onLoadingCompleted() {

        }

        @Override
        public void onLoadingFailed() {

        }
    }
}
