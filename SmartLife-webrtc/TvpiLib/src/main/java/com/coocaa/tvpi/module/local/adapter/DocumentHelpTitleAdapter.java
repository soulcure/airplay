package com.coocaa.tvpi.module.local.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 3/31/21
 */
public class DocumentHelpTitleAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    private int mCurSelectPosition = 0;

    public DocumentHelpTitleAdapter() {
        super(R.layout.item_document_help_title);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, String s) {
        TextView title = holder.getView(R.id.title);
        View line = holder.getView(R.id.line);
        title.setText(s);
        if (getItemPosition(s) == mCurSelectPosition) {
            title.getPaint().setFakeBoldText(true);
            title.setTextColor(Color.parseColor("#387AFF"));
            line.setVisibility(View.VISIBLE);
        } else {
            title.getPaint().setFakeBoldText(false);
            title.setTextColor(Color.BLACK);
            line.setVisibility(View.INVISIBLE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurSelectPosition = getItemPosition(s);
                setOnItemClick(view, mCurSelectPosition);
            }
        });
    }

}
