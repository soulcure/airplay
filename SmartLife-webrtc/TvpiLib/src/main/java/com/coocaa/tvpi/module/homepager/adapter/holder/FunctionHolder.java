package com.coocaa.tvpi.module.homepager.adapter.holder;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpi.module.homepager.adapter.FunctionAdapter;
import com.coocaa.tvpi.module.homepager.adapter.bean.SmartScreenHeaderFunctionBean;
import com.coocaa.tvpi.view.decoration.GridDividerItemDecoration;
import com.coocaa.tvpilib.R;

/**
 * 智屏页面头部同屏显示界面和功能按钮界面布局
 * Created by songxing on 2020/3/25
 */
public class FunctionHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "HeaderHolder";

    private Context context;
    private RecyclerView recyclerView;

    private FunctionAdapter functionAdapter;


    public FunctionHolder(@NonNull View itemView, final Context context,
                          final FunctionAdapter.FunctionClickListener functionClickListener) {
        super(itemView);
        this.context = context;
        recyclerView = itemView.findViewById(R.id.list_with_arrow);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 4));
        functionAdapter = new FunctionAdapter(context);
        GridDividerItemDecoration decoration = new GridDividerItemDecoration(context,
                DimensUtils.dp2Px(context, 15), true);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(functionAdapter);
        functionAdapter.setFunctionClickListener(functionClickListener);
    }


    public void onBind(final SmartScreenHeaderFunctionBean headerBean) {
        functionAdapter.setFunctionBeanLis(headerBean.functionBeanList);
    }
}
