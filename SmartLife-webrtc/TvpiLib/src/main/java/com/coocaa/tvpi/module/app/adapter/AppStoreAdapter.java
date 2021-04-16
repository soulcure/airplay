package com.coocaa.tvpi.module.app.adapter;

import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.tvpi.module.app.bean.AppStoreWrapBean;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

public class AppStoreAdapter extends BaseQuickAdapter<AppStoreWrapBean, BaseViewHolder> {
    public AppStoreListener appStoreListener;


    public AppStoreAdapter() {
        super(R.layout.item_app_store);
    }


    @Override
    protected void convert(@NotNull BaseViewHolder holder, AppStoreWrapBean bean) {
        holder.setText(R.id.tvType, bean.classifyName);

        RecyclerView rvAppStore = holder.getView(R.id.rvAppStore);
        if (rvAppStore.getLayoutManager() == null) {
            rvAppStore.setLayoutManager(new GridLayoutManager(getContext(), 3, RecyclerView.HORIZONTAL, false));
        }

        if (rvAppStore.getAdapter() != null && holder.getAdapterPosition() == (int) rvAppStore.getTag()) {
            rvAppStore.getAdapter().notifyDataSetChanged();
        } else {
            AppStoreChildAdapter childAdapter = new AppStoreChildAdapter();
            rvAppStore.setAdapter(childAdapter);
            childAdapter.setList(bean.getAppList());
            childAdapter.setChildClickListener(new AppStoreChildAdapter.ChildClickListener() {
                @Override
                public void onChildStateButtonClick(AppModel appModel) {
                    if (appStoreListener != null) {
                        appStoreListener.onChildStateButtonClick(holder.getAdapterPosition(), appModel);
                    }
                }

                @Override
                public void onChildItemClick(AppModel appModel) {
                    if (appStoreListener != null) {
                        appStoreListener.onChildItemClick(appModel);
                    }
                }
            });
            rvAppStore.setTag(holder.getAdapterPosition());
        }


        holder.getView(R.id.ivMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appStoreListener != null) {
                    appStoreListener.onMoreListClick(bean.classifyId, bean.classifyName);
                }
            }
        });
    }

    public void setAppStoreListener(AppStoreListener appStoreListener) {
        this.appStoreListener = appStoreListener;
    }

    public interface AppStoreListener {
        void onMoreListClick(String classId, String className);

        void onChildStateButtonClick(int pos, AppModel appModel);

        void onChildItemClick(AppModel appModel);
    }
}
