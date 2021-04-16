package com.coocaa.whiteboard.ui.toollayer.tvcontroller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;


import com.coocaa.whiteboard.ui.R;
import com.coocaa.whiteboard.ui.base.BaseToolLayerView;
import com.coocaa.whiteboard.ui.callback.ToolLayerCallback;
import com.coocaa.whiteboard.ui.toollayer.tvcontroller.GestureImageView;
import com.coocaa.whiteboard.ui.toollayer.tvcontroller.TvControllerOverlayView;

/**
 * 工具UI层 电视显示区域按钮视图
 */
public class TvControllerView extends BaseToolLayerView {
    private View tvSizeSettingsBtn;  //电视显示区域按钮
    private View tvControllerBgLayout;
    private View tvControllerLayout;
    private TvControllerOverlayView tvControllerOverlayView;
    private GestureImageView tvControllerImageView;

    private ToolLayerCallback toolOverlayCallback;

    public TvControllerView(Context context) {
        this(context, null);
    }

    public TvControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    @Override
    public int getLayoutId() {
        return R.layout.whiteboard_tv_controller_layout;
    }

    private void initView() {
        tvSizeSettingsBtn = findViewById(R.id.whiteboard_tv_controller_btn);
        tvControllerBgLayout = findViewById(R.id.whiteboard_tv_controller_bg_layout);
        tvControllerLayout = findViewById(R.id.whiteboard_tv_controller_layout);
        tvControllerImageView = findViewById(R.id.whiteboard_tv_controller_image);
        tvControllerOverlayView = findViewById(R.id.whiteboard_tv_controller_image_overlay);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.test_bitmap);
        tvControllerImageView.setImageBitmap(bitmap);
        tvControllerBgLayout.setVisibility(GONE);

        tvSizeSettingsBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("CCCC", "tvSizeSettingsBtn onClick");
                unfoldTvSizeControllerLayout();
            }
        });

        tvControllerBgLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("CCCC", "tvControllerBgLayout onClick");
                foldTvSizeControllerLayout();
            }
        });
    }

    //收起
    private void unfoldTvSizeControllerLayout() {
        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f);
        animation.setDuration(300);
        tvControllerLayout.startAnimation(animation);
        tvControllerBgLayout.setVisibility(VISIBLE);
        tvSizeSettingsBtn.setVisibility(GONE);
    }

    //收起
    private void foldTvSizeControllerLayout() {
        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f);
        animation.setDuration(300);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tvControllerBgLayout.setVisibility(GONE);
                tvSizeSettingsBtn.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        tvControllerLayout.startAnimation(animation);
    }

    public void setToolOverlayCallback(ToolLayerCallback toolOverlayCallback) {
        this.toolOverlayCallback = toolOverlayCallback;
    }
}