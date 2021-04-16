package com.coocaa.tvpi.module.homepager.adapter;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.function.homepage.SSHomePageBlock;
import com.coocaa.tvpi.view.decoration.PictureItemDecoration;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

public class BigScreenPlayAdapter extends BaseQuickAdapter<SSHomePageBlock, BaseViewHolder> {
    public BigScreenPlayAdapter() {
        super(R.layout.item_big_screen_play);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, SSHomePageBlock block) {
        holder.setText(R.id.tv_title, block.title);
        RecyclerView rvFunction = holder.findView(R.id.rv_function);
        if (rvFunction != null) {
            if (rvFunction.getLayoutManager() == null) {
                LinearLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
                rvFunction.setLayoutManager(layoutManager);
            }

            if (rvFunction.getAdapter() == null) {
                BigScreenPlayFunctionAdapter innerAdapter = new BigScreenPlayFunctionAdapter();
                innerAdapter.setList(block.contents);
                rvFunction.setAdapter(innerAdapter);
            } else {
                ((BaseQuickAdapter) rvFunction.getAdapter()).setList(block.contents);
            }
        }
    }
}
