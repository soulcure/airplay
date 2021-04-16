package com.coocaa.tvpi.module.local.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.data.local.DocumentData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpilib.R;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import org.jetbrains.annotations.NotNull;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 12/30/20
 */
public class DocumentMainAdapter extends DocumentBaseAdapter {

    private OnItemClickListener mOnItemClickListener;

    public DocumentMainAdapter() {
        super(R.layout.item_document_main);
    }

    public void setOnDocItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    protected void init() {
        mDatePattern = "yyyy-MM-dd";
    }

    @Override
    protected View baseInfoRootView(BaseViewHolder holder) {
        return holder.getView(R.id.base_info_root_layout);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, DocumentData documentData) {
        super.convert(holder, documentData);
        SwipeMenuLayout swipeMenuLayout = holder.getView(R.id.swipe_menu_layout);
        View root = baseInfoRootView(holder);
        TextView sizeTv = root.findViewById(R.id.item_doc_size);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) sizeTv.getLayoutParams();
        params.leftMargin = DimensUtils.dp2Px(getContext(), 10);
        params.addRule(RelativeLayout.END_OF, R.id.item_doc_date);
        sizeTv.setLayoutParams(params);
        TextView titleTv = root.findViewById(R.id.item_doc_title);
        params = (RelativeLayout.LayoutParams) titleTv.getLayoutParams();
        params.rightMargin = DimensUtils.dp2Px(getContext(), 30);
        titleTv.setLayoutParams(params);

        ImageView moreBtn = root.findViewById(R.id.more_btn);
        moreBtn.setVisibility(View.VISIBLE);
        baseInfoRootView(holder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onItemClick(getItemPosition(documentData), documentData);
            }
        });
        holder.getView(R.id.delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swipeMenuLayout.smoothClose();
                mOnItemClickListener.onDeleteClick(getItemPosition(documentData), documentData);
            }
        });
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swipeMenuLayout.smoothExpand();
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(int position, DocumentData data);

        void onDeleteClick(int position, DocumentData data);
    }
}
