package com.coocaa.tvpi.module.app.adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.data.app.TvAppModel;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.AppRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

/**
 * 电视应用适配器
 * Created by songxing on 2020/7/16
 */
public class AppTvAdapter extends BaseQuickAdapter<TvAppModel, BaseViewHolder> {
    private static final String TAG = AppTvAdapter.class.getSimpleName();
    private boolean isEditState = false;

    public AppTvAdapter() {
        super(R.layout.item_app_tv);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, TvAppModel tvAppModel) {
        holder.setText(R.id.tvName, tvAppModel.appName);

        GlideApp.with(getContext())
                .load(tvAppModel.coverUrl)
                .placeholder(R.drawable.place_holder_app)
                .error(R.drawable.place_holder_app)
                .into((ImageView) holder.getView(R.id.ivCover));

        ImageView check = holder.getView(R.id.ivSelect);
        if (isEditState) {
            check.setVisibility(View.VISIBLE);
            if (tvAppModel.isSelected) {
                check.setBackgroundResource(R.drawable.app_tvapp_check);
            } else {
                check.setBackgroundResource(R.drawable.app_tvapp_uncheck);
            }
        } else {
            check.setVisibility(View.GONE);
        }

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvAppModel.isSelected = !tvAppModel.isSelected;
                notifyItemChanged(holder.getAdapterPosition());

                if (itemSelectListener != null) {
                    itemSelectListener.onItemSelect();
                }
            }
        });
    }

    public void setEditState(boolean editState) {
        isEditState = editState;
        notifyDataSetChanged();
    }


    private ItemSelectListener itemSelectListener;

    public void setItemSelectListener(ItemSelectListener itemSelectListener) {
        this.itemSelectListener = itemSelectListener;
    }

    public interface ItemSelectListener {
        void onItemSelect();
    }

}
