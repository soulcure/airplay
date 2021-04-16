package com.coocaa.tvpi.module.app.adapter;

import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.tvpi.module.app.AppDetailActivity;
import com.coocaa.tvpi.module.app.widget.AppStateButton;
import com.coocaa.tvpi.util.SizeConverter;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

public class AppStoreListAdapter extends BaseQuickAdapter<AppModel, BaseViewHolder> {
    private StateButtonClickListener stateButtonClickListener;

    public AppStoreListAdapter() {
        super(R.layout.item_app_store_list);
    }


    @Override
    protected void convert(@NotNull BaseViewHolder holder, AppModel appModel) {
        holder.setText(R.id.tvName, appModel.appName);
        holder.setText(R.id.tvSize, SizeConverter.BTrim.convert((float) appModel.fileSize));
        holder.getView(R.id.ivCover).setTag(R.id.ivCover, holder.getAdapterPosition());
        GlideApp.with(getContext())
                .load(appModel.icon)
                .into((ImageView) holder.getView(R.id.ivCover));
        AppStateButton stateButton = holder.getView(R.id.appStateButton);
        stateButton.setState(appModel.status);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDetailActivity.start(getContext(),appModel);
            }
        });

        stateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stateButtonClickListener != null) {
                    stateButtonClickListener.onStateButtonClick(appModel, holder.getAdapterPosition());
                }
            }
        });
    }

    public void setStateButtonClickListener(StateButtonClickListener stateButtonClickListener) {
        this.stateButtonClickListener = stateButtonClickListener;
    }

    public interface StateButtonClickListener {
        void onStateButtonClick(AppModel appModel, int pos);
    }
}
