package com.coocaa.tvpi.module.homepager.adapter.holder;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpi.module.homepager.adapter.AppAdapter;
import com.coocaa.tvpi.module.homepager.adapter.bean.SmartScreenAppBean;
import com.coocaa.tvpi.module.app.AppHomeActivity;
import com.coocaa.tvpi.view.decoration.CommonHorizontalItemDecoration;
import com.coocaa.tvpilib.R;

/**
 * 智屏首页应用列表Holder
 * Created by songxing on 2020/8/31
 */
public class AppHolder extends RecyclerView.ViewHolder {
    private RecyclerView rvApp;
    private RelativeLayout root;
    private AppAdapter adapter;

    public AppHolder(@NonNull View itemView, Context context) {
        super(itemView);
        rvApp = itemView.findViewById(R.id.rv_app);
        rvApp.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        CommonHorizontalItemDecoration decoration = new CommonHorizontalItemDecoration(
                DimensUtils.dp2Px(context, 15), DimensUtils.dp2Px(context, 10));
        rvApp.addItemDecoration(decoration);
        rvApp.setNestedScrollingEnabled(false);
        adapter = new AppAdapter(context);
        rvApp.setAdapter(adapter);
        root = itemView.findViewById(R.id.root);
        root.setVisibility(View.GONE);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppHomeActivity.start(context);
            }
        });
    }

    public void onBind(final SmartScreenAppBean smartScreenAppBean) {
        if (smartScreenAppBean == null
                || smartScreenAppBean.appModelList == null
                || smartScreenAppBean.appModelList.isEmpty()) {
            root.setVisibility(View.GONE);
        }else {
            root.setVisibility(View.VISIBLE);
            adapter.setData(smartScreenAppBean.appModelList);
        }
    }
}