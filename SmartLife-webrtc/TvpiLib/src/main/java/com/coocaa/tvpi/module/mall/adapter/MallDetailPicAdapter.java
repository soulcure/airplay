package com.coocaa.tvpi.module.mall.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.smartmall.data.mobile.data.ProductDetailResult;
import com.coocaa.tvpi.util.ImageUtils;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

public class MallDetailPicAdapter extends BaseQuickAdapter<ProductDetailResult.DataBean.ProductDetailsBean, BaseViewHolder> {

    public MallDetailPicAdapter() {
        super(R.layout.item_detail_pic);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, ProductDetailResult.DataBean.ProductDetailsBean bean) {

        ImageUtils.load(holder.itemView.findViewById(R.id.cover),bean.getImage_details());
    }
}
