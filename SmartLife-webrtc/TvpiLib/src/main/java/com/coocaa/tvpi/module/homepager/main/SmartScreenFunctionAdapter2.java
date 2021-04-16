package com.coocaa.tvpi.module.homepager.main;

import android.animation.Animator;
import android.animation.StateListAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieListener;
import com.bumptech.glide.signature.ObjectKey;
import com.chad.library.adapter.base.BaseDelegateMultiAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.tvpi.module.homepager.adapter.bean.SmartScreenWrapBean;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.coocaa.tvpi.view.decoration.PictureItemDecoration;
import com.coocaa.tvpilib.R;
import com.makeramen.roundedimageview.RoundedImageView;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerAdapter;
import com.youth.banner.indicator.CircleIndicator;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import swaiotos.channel.iot.ss.device.Device;

public class SmartScreenFunctionAdapter2 extends BaseDelegateMultiAdapter<SmartScreenWrapBean, BaseViewHolder> {

    public SmartScreenFunctionAdapter2() {
        super();
        setMultiTypeDelegate(new SmartScreenFunctionAdapter2.MyMultiTypeDelegate());
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, SmartScreenWrapBean smartScreenWrapBean) {
        switch (holder.getItemViewType()) {
            case SmartScreenWrapBean.TYPE_BANNER_LIST:
                if (smartScreenWrapBean.style == 1) {
                    updateNormalBanner(smartScreenWrapBean.getBannerList(), holder);
                }
                if (smartScreenWrapBean.style == 2) {
                    updateBottomAnimalBanner(smartScreenWrapBean.getBannerList(), holder);
                }
                if (smartScreenWrapBean.style == 3) {
                    updateBottomImageBanner(smartScreenWrapBean.getBannerList(), holder);
                }
                break;
            case SmartScreenWrapBean.TYPE_FUNCTION_LIST:
                List<FunctionBean> functionBeanList = smartScreenWrapBean.getFunctionBeanList();
                RecyclerView rvFunction = holder.findView(R.id.rvFunction);
                if (rvFunction != null) {
                    if (rvFunction.getLayoutManager() == null) {
                        LinearLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
                        rvFunction.setLayoutManager(layoutManager);
                    }

                    if (rvFunction.getItemDecorationCount() == 0) {
                        //留出更多位置显示文字
                        int dp25 = DimensUtils.dp2Px(getContext(), 25);
                        PictureItemDecoration decoration = new PictureItemDecoration(3, dp25, 0);
                        rvFunction.addItemDecoration(decoration);
                    }

                    if (rvFunction.getAdapter() == null) {
                        SmartScreenFunctionAdapter2.SmartScreenFunctionAdapter functionListAdapter = new SmartScreenFunctionAdapter2.SmartScreenFunctionAdapter();
                        rvFunction.setAdapter(functionListAdapter);
                        functionListAdapter.setList(functionBeanList);
                    } else {
                        ((BaseQuickAdapter) rvFunction.getAdapter()).setList(functionBeanList);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void updateBottomImageBanner(List<FunctionBean> bannerList, BaseViewHolder holder) {
        holder.findView(R.id.banner_layout).setVisibility(View.GONE);
        holder.findView(R.id.animation_layout).setVisibility(View.GONE);
        holder.findView(R.id.image_layout).setVisibility(View.VISIBLE);
        ImageView view = holder.findView(R.id.image_banner);

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

    private void updateBottomAnimalBanner(List<FunctionBean> bannerList, BaseViewHolder holder) {
        holder.findView(R.id.banner_layout).setVisibility(View.GONE);
        holder.findView(R.id.image_layout).setVisibility(View.GONE);
        holder.findView(R.id.animation_layout).setVisibility(View.VISIBLE);
        LottieAnimationView view = holder.findView(R.id.animation_banner);
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
                        view.setVisibility(View.VISIBLE);
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
                        holder.findView(R.id.animation_layout).setVisibility(View.GONE);
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

    private void updateNormalBanner(List<FunctionBean> bannerList, BaseViewHolder holder) {
        holder.findView(R.id.image_layout).setVisibility(View.GONE);
        holder.findView(R.id.animation_layout).setVisibility(View.GONE);
        holder.findView(R.id.banner_layout).setVisibility(View.VISIBLE);
        Banner<FunctionBean, SmartScreenFunctionAdapter2.SmartScreenBannerAdapter> banner = holder.findView(R.id.banner);
        if (banner != null) {
            if (banner.getAdapter() == null) {
                SmartScreenFunctionAdapter2.SmartScreenBannerAdapter bannerAdapter = new SmartScreenFunctionAdapter2.SmartScreenBannerAdapter(bannerList);
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


    final static class MyMultiTypeDelegate extends BaseMultiTypeDelegate<SmartScreenWrapBean> {

        public MyMultiTypeDelegate() {
            addItemType(SmartScreenWrapBean.TYPE_BANNER_LIST, R.layout.item_smartscreenw52_banner_list);
            addItemType(SmartScreenWrapBean.TYPE_FUNCTION_LIST, R.layout.item_smartscreenw52_function_list);
        }

        @Override
        public int getItemType(@NotNull List<? extends SmartScreenWrapBean> list, int position) {
            if (list.get(position) != null
                    && list.get(position).getBannerList() != null
                    && !list.get(position).getBannerList().isEmpty()
                    && list.get(position).getStyle() != 0) {
                return SmartScreenWrapBean.TYPE_BANNER_LIST;
            } else {
                return SmartScreenWrapBean.TYPE_FUNCTION_LIST;
            }
        }
    }


    class SmartScreenBannerAdapter extends BannerAdapter<FunctionBean, SmartScreenFunctionAdapter2.SmartScreenBannerAdapter.BannerViewHolder> {
        public SmartScreenBannerAdapter(List<FunctionBean> datas) {
            super(datas);
        }

        @Override
        public SmartScreenFunctionAdapter2.SmartScreenBannerAdapter.BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
            RoundedImageView imageView = new RoundedImageView(parent.getContext());
            //注意，必须设置为match_parent，这个是viewpager2强制要求的
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setCornerRadius(DimensUtils.dp2Px(getContext(), 12));
            return new SmartScreenFunctionAdapter2.SmartScreenBannerAdapter.BannerViewHolder(imageView);
        }

        @Override
        public void onBindView(SmartScreenFunctionAdapter2.SmartScreenBannerAdapter.BannerViewHolder holder, FunctionBean data, int position, int size) {
            GlideApp.with(getContext())
                    .load(data.icon)
                    .centerCrop()
                    .signature(new ObjectKey(SmartConstans.getBuildInfo().buildTimestamp))
                    .into(holder.imageView);

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TvpiClickUtil.onClick(getContext(), data.uri());
                }
            });
        }


        class BannerViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public BannerViewHolder(@NonNull ImageView view) {
                super(view);
                this.imageView = view;
            }
        }
    }

    class SmartScreenFunctionAdapter extends BaseQuickAdapter<FunctionBean, BaseViewHolder> {

        public SmartScreenFunctionAdapter() {
            super(R.layout.item_smartscreenw52_function);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, FunctionBean functionBean) {
            holder.setText(R.id.tv_function_name, functionBean.name);
            ImageView image = holder.findView(R.id.iv_function);
            if (image != null) {
                GlideApp.with(getContext())
                        .load(functionBean.icon)
                        .centerCrop()
                        .placeholder(R.drawable.bg_gray_e6e6e6_round_6)
                        .signature(new ObjectKey(SmartConstans.getBuildInfo().buildTimestamp))
                        .into(image);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TvpiClickUtil.onClick(getContext(), functionBean.uri());
                    submitAppletClick(functionBean.id, functionBean.name);
                }
            });
        }
    }

    private OnAnimalLoadListener onAnimalLoadListener;

    public interface OnAnimalLoadListener {
        void onLoadError();

        void onLoadSuccess();
    }

    public void setOnAnimalLoadListener(OnAnimalLoadListener onAnimalLoadListener) {
        this.onAnimalLoadListener = onAnimalLoadListener;
    }

    private void submitAppletClick(String appletId, String appletName) {
        Device device = SSConnectManager.getInstance().getDevice();
        CoocaaUserInfo coocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
        LogParams params = LogParams.newParams()
                .append("device_connected", device == null ? "false" : "true")
                .append("ss_device_id", device == null ? "disconnected" : device.getLsid())
                .append("ss_device_type", device == null ? "disconnected" : device.getZpRegisterType())
                .append("account", coocaaUserInfo == null ? "not_login" : coocaaUserInfo.getOpen_id())
                .append("applet_id", appletId)
                .append("applet_name", appletName);
        LogSubmit.event("mainpage_applet_clicked", params.getParams());
    }


}
