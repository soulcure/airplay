package com.coocaa.tvpi.module.web.adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.tvpi.module.web.WebRecordBean;
import com.coocaa.tvpilib.R;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import org.jetbrains.annotations.NotNull;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 2020/10/21
 */
public class BrowserRecordAdapter extends BaseQuickAdapter<WebRecordBean, BaseViewHolder> {
    private ImageView iv;
    private ImageView ivSelect;
    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvDelete;
    private View rootView;
    private handleItemEventListener handleItemEventListener;

    public void setHandleItemEventListener(BrowserRecordAdapter.handleItemEventListener handleItemEventListener) {
        this.handleItemEventListener = handleItemEventListener;
    }

    public BrowserRecordAdapter() {
        super(R.layout.item_smart_browser_record);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, WebRecordBean webRecordBean) {
        rootView = baseViewHolder.getView(R.id.base_info_root_layout);
        iv = baseViewHolder.getView(R.id.iv);
        tvTitle = baseViewHolder.getView(R.id.tv_title);
        tvContent = baseViewHolder.getView(R.id.tv_content);
        ivSelect = baseViewHolder.getView(R.id.iv_select);
        tvDelete = baseViewHolder.getView(R.id.delete_btn);
        String title = webRecordBean.getTitle();
        if (TextUtils.isEmpty(title)) {
            tvTitle.setText(TextUtils.isEmpty(webRecordBean.getWebUrl()) ? "" : webRecordBean.getWebUrl());
        } else {
            tvTitle.setText(title);
        }

        if (!TextUtils.isEmpty(webRecordBean.getContent()) && !webRecordBean.getContent().equals("null")) {
            tvContent.setVisibility(View.VISIBLE);
            tvContent.setText(webRecordBean.getContent());
        } else {
            tvContent.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(webRecordBean.getImageUrl())) {
            Log.d("SmartWebInfo", "load icon : " + webRecordBean.getImageUrl());
            Glide.with(getContext()).load(webRecordBean.getImageUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(6)))
                    .error(R.drawable.smart_browser_default_icon)
                    .placeholder(R.drawable.smart_browser_default_icon)
                    .into(iv);
        } else {
            iv.setImageResource(R.drawable.smart_browser_default_icon);
        }

        if (webRecordBean.isShow()) {
            ivSelect.setVisibility(View.VISIBLE);
        } else {
            ivSelect.setVisibility(View.GONE);
        }
        if (webRecordBean.isSelect()) {
            ivSelect.setImageResource(R.drawable.smart_browser_selected_icon);
        } else {
            ivSelect.setImageResource(R.drawable.smart_browser_unselected_icon);
        }
        SwipeMenuLayout swipeMenuLayout = baseViewHolder.getView(R.id.swipe_menu_layout);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (handleItemEventListener != null) {
                    handleItemEventListener.onItemClick(getItemPosition(webRecordBean), webRecordBean);
                }
            }
        });

        tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (handleItemEventListener != null) {
                    swipeMenuLayout.smoothClose();
                    handleItemEventListener.onDeleteClick(getItemPosition(webRecordBean), webRecordBean);
                }
            }
        });
        ivSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (handleItemEventListener != null) {
                    handleItemEventListener.onSelectClick(getItemPosition(webRecordBean), webRecordBean);
                }
            }
        });
    }


    public interface handleItemEventListener {
        void onItemClick(int position, WebRecordBean data);

        void onDeleteClick(int position, WebRecordBean data);

        void onSelectClick(int position, WebRecordBean data);
    }

}
