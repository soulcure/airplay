package com.coocaa.tvpi.module.local.adapter;

import android.view.View;

import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.data.local.DocumentData;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 12/30/20
 */
public class DocumentSearchAdapter extends DocumentBaseAdapter {

    public DocumentSearchAdapter() {
        super(R.layout.item_document_search);
    }

    @Override
    protected void init() {

    }

    @Override
    protected View baseInfoRootView(BaseViewHolder holder) {
        return holder.getView(R.id.base_info_root_layout);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, DocumentData documentData) {
        super.convert(holder, documentData);
        View root = baseInfoRootView(holder);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOnItemClick(root, getItemPosition(documentData));
            }
        });
    }
}
