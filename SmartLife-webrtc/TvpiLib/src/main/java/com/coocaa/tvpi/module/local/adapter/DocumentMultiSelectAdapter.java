package com.coocaa.tvpi.module.local.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.data.local.DocumentData;
import com.coocaa.tvpilib.R;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 12/30/20
 */
public class DocumentMultiSelectAdapter extends DocumentBaseAdapter {

    private OnSelectListener mOnSelectListener;
    private LinkedHashMap<String, DocSelectData> mSelectList = new LinkedHashMap<>();

    public DocumentMultiSelectAdapter() {
        super(R.layout.item_document_delete);
    }

    public void setOnSelectListener(OnSelectListener onSelectListener) {
        mOnSelectListener = onSelectListener;
    }

    public LinkedHashMap<String, DocSelectData> getSelectList() {
        return mSelectList;
    }

    public void clearSelectList() {
        mSelectList.clear();
        if (mOnSelectListener != null) {
            mOnSelectListener.onSelectChange(mSelectList);
        }
    }

    @Override
    public void setList(@Nullable Collection<? extends DocumentData> list) {
        super.setList(list);
        clearSelectList();
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
        ImageView selectIcon = holder.getView(R.id.select_icon);
        if (mSelectList.containsKey(documentData.url)) {
            selectIcon.setImageResource(R.drawable.doc_icon_selected);
        } else {
            selectIcon.setImageResource(R.drawable.doc_icon_unselected);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSelectList.containsKey(documentData.url)) {
                    mSelectList.remove(documentData.url);
                    selectIcon.setImageResource(R.drawable.doc_icon_unselected);
                } else {
                    mSelectList.put(documentData.url, new DocSelectData(documentData.url, documentData));
                    selectIcon.setImageResource(R.drawable.doc_icon_selected);
                }
                if (mOnSelectListener != null) {
                    mOnSelectListener.onSelectChange(mSelectList);
                }
            }
        });
        View root = baseInfoRootView(holder);
        TextView title = root.findViewById(R.id.item_doc_title);
        title.setPadding(0, 0, UIUtil.dip2px(getContext(), 30), 0);
        root.findViewById(R.id.item_doc_size).setVisibility(View.GONE);
    }

    public class DocSelectData {
        public String filePath;
        public DocumentData data;

        public DocSelectData(String path, DocumentData data) {
            this.filePath = path;
            this.data = data;
        }
    }

    public interface OnSelectListener {
        void onSelectChange(LinkedHashMap<String, DocSelectData> selectDatas);
    }
}
