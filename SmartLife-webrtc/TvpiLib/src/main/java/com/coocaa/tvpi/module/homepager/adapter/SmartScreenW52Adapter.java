package com.coocaa.tvpi.module.homepager.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class SmartScreenW52Adapter extends BaseDelegateMultiAdapter<SmartScreenWrapBean, BaseViewHolder> {
    public SmartScreenW52Adapter() {
        super();
        setMultiTypeDelegate(new MyMultiTypeDelegate());
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, SmartScreenWrapBean smartScreenWrapBean) {
        switch (holder.getItemViewType()) {
            case SmartScreenWrapBean.TYPE_BANNER_LIST:
                List<FunctionBean> bannerList = smartScreenWrapBean.getBannerList();
                Banner<FunctionBean, SmartScreenBannerAdapter> banner = holder.findView(R.id.banner);
                if (banner != null) {
                    if (banner.getAdapter() == null) {
                        SmartScreenBannerAdapter bannerAdapter = new SmartScreenBannerAdapter(bannerList);
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
                        SmartScreenFunctionAdapter functionListAdapter = new SmartScreenFunctionAdapter();
                        rvFunction.setAdapter(functionListAdapter);
                        functionListAdapter.setList(functionBeanList);
                    } else {
                        rvFunction.getAdapter().notifyDataSetChanged();
                    }
                }
                break;
            default:
                break;
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
                    && !list.get(position).getBannerList().isEmpty()) {
                return SmartScreenWrapBean.TYPE_BANNER_LIST;
            } else {
                return SmartScreenWrapBean.TYPE_FUNCTION_LIST;
            }
        }
    }


    class SmartScreenBannerAdapter extends BannerAdapter<FunctionBean, SmartScreenBannerAdapter.BannerViewHolder> {
        public SmartScreenBannerAdapter(List<FunctionBean> datas) {
            super(datas);
        }

        @Override
        public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
            RoundedImageView imageView = new RoundedImageView(parent.getContext());
            //注意，必须设置为match_parent，这个是viewpager2强制要求的
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setCornerRadius(DimensUtils.dp2Px(getContext(), 12));
            return new BannerViewHolder(imageView);
        }

        @Override
        public void onBindView(BannerViewHolder holder, FunctionBean data, int position, int size) {
            GlideApp.with(getContext())
                    .load(data.icon)
                    .centerCrop()
                    .signature(new ObjectKey(SmartConstans.getBuildInfo().buildTimestamp))
                    .into(holder.imageView);

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TvpiClickUtil.onClick(getContext(),data.uri());
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
