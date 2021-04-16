package com.coocaa.tvpi.module.homepager.main.vy21m4.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.signature.ObjectKey;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.makeramen.roundedimageview.RoundedImageView;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

public class SmartScreenBannerAdapter extends BannerAdapter<FunctionBean, SmartScreenBannerAdapter.BannerViewHolder> {
    
    private Context context;
    
    public SmartScreenBannerAdapter(List<FunctionBean> datas, Context context) {
        super(datas);
        this.context = context;
    }

    @Override
    public SmartScreenBannerAdapter.BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        RoundedImageView imageView = new RoundedImageView(parent.getContext());
        //注意，必须设置为match_parent，这个是viewpager2强制要求的
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setCornerRadius(DimensUtils.dp2Px(context, 12));
        return new SmartScreenBannerAdapter.BannerViewHolder(imageView);
    }

    @Override
    public void onBindView(SmartScreenBannerAdapter.BannerViewHolder holder, FunctionBean data, int position, int size) {
        GlideApp.with(context)
                .load(data.icon)
                .centerCrop()
                .signature(new ObjectKey(SmartConstans.getBuildInfo().buildTimestamp))
                .into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TvpiClickUtil.onClick(context, data.uri());
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
