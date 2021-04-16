package com.coocaa.tvpi.module.newmovie.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.movie.PushHistoryModel;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class PushHistoryAdapter extends BaseQuickAdapter<PushHistoryModel.PushHistoryVideoModel, BaseViewHolder> {
    public PushHistoryAdapter() {
        super(R.layout.item_movie_push_history);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, PushHistoryModel.PushHistoryVideoModel pushHistoryVideoModel) {
        holder.setText(R.id.name,pushHistoryVideoModel.title);
        View view = holder.getView(R.id.root);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = (int) ((DimensUtils.getDeviceWidth(getContext()) - DimensUtils.dp2Px(getContext(),50)) / 3f);

        GlideApp.with(getContext())
                .load(pushHistoryVideoModel.poster_h)
                .into((ImageView) holder.getView(R.id.cover));
    }
}
