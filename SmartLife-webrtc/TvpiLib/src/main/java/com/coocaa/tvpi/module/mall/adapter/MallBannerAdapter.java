package com.coocaa.tvpi.module.mall.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.smartmall.data.mobile.data.BannerResult;
import com.coocaa.tvpi.module.mall.MallDetailActivity;
import com.coocaa.tvpi.util.ImageUtils;
import com.coocaa.tvpilib.R;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

public class MallBannerAdapter extends BannerAdapter<BannerResult.DataBean, MallBannerAdapter.BannerViewHolder> {
    public MallBannerAdapter(Context context, List<BannerResult.DataBean> datas) {
        super(datas);
    }

    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        View view=View.inflate(parent.getContext(), R.layout.mall_banner_layout,null);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindView(BannerViewHolder holder,final BannerResult.DataBean data, int position, int size) {
        ImageUtils.load(holder.imageView,data.getImage_url());
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MallDetailActivity.start(holder.imageView.getContext(),data.getProduct_id());
            }
        });
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public BannerViewHolder(@NonNull View view) {
            super(view);
            this.imageView = view.findViewById(R.id.icon);
        }
    }
}
