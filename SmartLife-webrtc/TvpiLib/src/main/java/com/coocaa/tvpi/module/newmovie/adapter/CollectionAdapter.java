package com.coocaa.tvpi.module.newmovie.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.UIHelper;
import com.coocaa.smartscreen.data.movie.CollectionModel;
import com.coocaa.tvpi.module.movie.LongVideoDetailActivity2;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

public class CollectionAdapter extends BaseQuickAdapter<CollectionModel,BaseViewHolder> {
    public CollectionAdapter() {
        super(R.layout.item_movie_collection);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, CollectionModel collectionModel) {
        holder.setText(R.id.name,collectionModel.video_title);

        View view = holder.getView(R.id.root);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = (int) ((DimensUtils.getDeviceWidth(getContext()) - DimensUtils.dp2Px(getContext(),50)) / 3f);

        GlideApp.with(getContext())
                .load(collectionModel.video_poster)
                .into((ImageView) holder.getView(R.id.cover));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIHelper.startActivityByURL(getContext(), collectionModel.router);
            }
        });
    }
}
