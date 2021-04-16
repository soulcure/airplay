package com.coocaa.tvpi.module.app.adapter;

import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.tvpi.module.app.widget.AppStateButton;
import com.coocaa.tvpi.util.SizeConverter;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

public class AppStoreChildAdapter extends BaseQuickAdapter<AppModel, BaseViewHolder> {
    private static final String TAG = AppStoreChildAdapter.class.getSimpleName();
    public ChildClickListener childClickListener;


    public AppStoreChildAdapter() {
        super(R.layout.item_app_store_list_child);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, AppModel appModel) {
        holder.setText(R.id.tvName,appModel.appName);
        holder.setText(R.id.tvSize, SizeConverter.BTrim.convert(Float.valueOf(appModel.fileSize)) );
        GlideApp.with(getContext())
                .load(appModel.icon)
                .into((ImageView) holder.getView(R.id.ivCover));
        AppStateButton stateButton = holder.getView(R.id.appStateButton);
        stateButton.setState(appModel.status);
        stateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(childClickListener != null){
                    childClickListener.onChildStateButtonClick(appModel);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(childClickListener != null){
                    childClickListener.onChildItemClick(appModel);
                }
            }
        });
    }

    public void setChildClickListener(ChildClickListener childClickListener){
        this.childClickListener = childClickListener;
    }

    public interface ChildClickListener{
        void onChildStateButtonClick(AppModel appModel);

        void onChildItemClick(AppModel appModel);
    }
}
