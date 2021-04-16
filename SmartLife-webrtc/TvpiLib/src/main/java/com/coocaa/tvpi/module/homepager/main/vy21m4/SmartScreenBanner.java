package com.coocaa.tvpi.module.homepager.main.vy21m4;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieListener;
import com.bumptech.glide.signature.ObjectKey;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.smartscreen.data.banner.BannerHttpData;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.tvpi.module.homepager.main.vy21m4.adapter.SmartScreenBannerAdapter;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.coocaa.tvpilib.R;
import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;

import java.util.List;

public class SmartScreenBanner extends LinearLayout {

    private final String TAG = SmartScreenBanner.class.getSimpleName();

    private Context context;
    private ConstraintLayout animationLayout;
    private ConstraintLayout imageLayout;
    private ConstraintLayout bannerLayout;
    private BannerHttpData.FunctionContent bannerData;

    private OnAnimalLoadListener onAnimalLoadListener;
    private OnViewShowListener onViewShowListener;

    public interface OnAnimalLoadListener {

        /**
         * 动画加载失败
         */
        void onLoadError();

        /**
         * 动画加载成功
         */
        void onLoadSuccess();

    }

    public interface OnViewShowListener {
        void onViewHide();

        void onViewShow();
    }

    public SmartScreenBanner(Context context) {
        this(context, null);
    }

    public SmartScreenBanner(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartScreenBanner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SmartScreenBanner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        initView();
    }

    public void setOnAnimalLoadListener(OnAnimalLoadListener onAnimalLoadListener) {
        this.onAnimalLoadListener = onAnimalLoadListener;
    }

    public void setOnViewShowListener(OnViewShowListener onViewShowListener) {
        this.onViewShowListener = onViewShowListener;
    }

    public BannerHttpData.FunctionContent getBannerData() {
        return bannerData;
    }

    public void showBanner(@Nullable BannerHttpData.FunctionContent bannerData) {
        this.bannerData = bannerData;
        if (bannerData.content == null || bannerData.content.isEmpty()) {
            Log.d(TAG, "showBanner: GONE");
            hindView();
        } else if (bannerData.style == 1) {
            Log.d(TAG, "showBanner: 1");
            showNormalBanner(bannerData.content);
        } else if (bannerData.style == 2) {
            Log.d(TAG, "showBanner: 2");
            showAnimationBanner(bannerData.content);
        } else if (bannerData.style == 3) {
            Log.d(TAG, "showBanner: 3");
            showImageBanner(bannerData.content);
        } else {
            Log.d(TAG, "showBanner: GONE");
            hindView();
        }
    }

    public void hindView() {
        animationLayout.setVisibility(GONE);
        imageLayout.setVisibility(GONE);
        bannerLayout.setVisibility(GONE);
        if(onViewShowListener != null) {
            onViewShowListener.onViewHide();
        }
    }

    private void initView() {
        View view = LayoutInflater.from(context).inflate(R.layout.smartscreen_y21m4_banner, this, true);
        animationLayout = view.findViewById(R.id.animation_layout);
        imageLayout = view.findViewById(R.id.image_layout);
        bannerLayout = view.findViewById(R.id.banner_layout);
    }

    private void showNormalBanner(List<FunctionBean> bannerList) {
        animationLayout.setVisibility(GONE);
        imageLayout.setVisibility(GONE);
        bannerLayout.setVisibility(VISIBLE);
        if(onViewShowListener != null){
            onViewShowListener.onViewShow();
        }
        Banner<FunctionBean, SmartScreenBannerAdapter> banner = bannerLayout.findViewById(R.id.banner);
        if (banner != null) {
            if (banner.getAdapter() == null) {
                SmartScreenBannerAdapter bannerAdapter = new SmartScreenBannerAdapter(bannerList, context);
                banner.setAdapter(bannerAdapter);
                if (getContext() instanceof Activity) {
                    banner.addBannerLifecycleObserver((LifecycleOwner) getContext());
                }
            } else {
                banner.getAdapter().setDatas(bannerList);
                banner.getAdapter().notifyDataSetChanged();
            }

            if (bannerList != null && bannerList.size() > 1) {
                CircleIndicator circleIndicator = new CircleIndicator(getContext());
                banner.setIndicator(circleIndicator);
                int dp4 = DimensUtils.dp2Px(getContext(), 4);
                banner.setIndicatorNormalWidth(dp4);
                banner.setIndicatorSelectedWidth(dp4);
                banner.setIndicatorSelectedColorRes(R.color.white);
                banner.setIndicatorNormalColorRes(R.color.color_white_60);
            } else {
                banner.removeIndicator();
            }
        }
    }

    private void showImageBanner(List<FunctionBean> bannerList) {
        animationLayout.setVisibility(GONE);
        bannerLayout.setVisibility(GONE);
        imageLayout.setVisibility(VISIBLE);
        if(onViewShowListener != null){
            onViewShowListener.onViewShow();
        }
        ImageView view = imageLayout.findViewById(R.id.image_banner);

        if (!TextUtils.isEmpty(bannerList.get(0).icon)) {
            GlideApp.with(getContext())
                    .load(bannerList.get(0).icon)
                    .centerCrop()
                    .signature(new ObjectKey(SmartConstans.getBuildInfo().buildTimestamp))
                    .into(view);
        }

        view.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(bannerList.get(0).icon)) {
                TvpiClickUtil.onClick(getContext(), (bannerList.get(0).uri()));
            }
        });
    }

    private void showAnimationBanner(List<FunctionBean> bannerList) {
        bannerLayout.setVisibility(GONE);
        imageLayout.setVisibility(GONE);
        animationLayout.setVisibility(VISIBLE);
        if(onViewShowListener != null){
            onViewShowListener.onViewShow();
        }
        LottieAnimationView view = animationLayout.findViewById(R.id.animation_banner);
        view.setVisibility(View.VISIBLE);
        try {
            if (!TextUtils.isEmpty(bannerList.get(0).icon) &&
                    bannerList.get(0).icon.startsWith("http") &&
                    bannerList.get(0).icon.endsWith("zip")) {
                view.setAnimationFromUrl(bannerList.get(0).icon);
                view.addAnimatorListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        Log.d("chen", "onAnimationStart: ");
                        onAnimalLoadListener.onLoadSuccess();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Log.d("chen", "onAnimationEnd: ");
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        Log.d("chen", "onAnimationCancel: ");
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                view.setFailureListener(new LottieListener<Throwable>() {
                    @Override
                    public void onResult(Throwable result) {
                        result.printStackTrace();
                        animationLayout.setVisibility(View.GONE);
                        if (onAnimalLoadListener != null) {
                            onAnimalLoadListener.onLoadError();
                        }
                    }
                });
                view.playAnimation();
            }
        } catch (Exception e) {
            Log.e("SmartScreenFragment2", "updateBottomAnimalBanner: Unable to parse ");
            onAnimalLoadListener.onLoadError();
        }


        view.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(bannerList.get(0).uri())) {
                TvpiClickUtil.onClick(getContext(), (bannerList.get(0).uri()));
            }
        });
    }


}
