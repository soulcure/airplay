package com.coocaa.tvpi.module.local.adapter;

import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.data.local.DocumentData;
import com.coocaa.tvpi.module.local.document.FormatEnum;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 2020/10/21
 */
public abstract class DocumentBaseAdapter extends BaseQuickAdapter<DocumentData, BaseViewHolder> {

    protected String mDatePattern = "yyyy-MM-dd HH:mm:ss";
    protected SimpleDateFormat mDateFormat;

    public DocumentBaseAdapter(int layoutId) {
        super(layoutId);
        init();
        mDateFormat = new SimpleDateFormat(mDatePattern);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, DocumentData documentData) {
        View root = baseInfoRootView(holder);
        setBaseInfo(root, documentData);
    }

    private void setBaseInfo(View root, DocumentData documentData) {
        ImageView icon = root.findViewById(R.id.item_doc_default_icon);
        TextView titleTV = root.findViewById(R.id.item_doc_title);
        TextView dateTv = root.findViewById(R.id.item_doc_date);
        TextView sizeTv = root.findViewById(R.id.item_doc_size);
        icon.setImageResource(FormatEnum.getFormat(documentData.suffix).icon);
        titleTV.setText(documentData.tittle);
        String time = mDateFormat.format(new Date(documentData.lastModifiedTime));
        dateTv.setText(time);
        sizeTv.setText(Formatter.formatFileSize(getContext(), documentData.size));
    }

    protected abstract void init();

    protected abstract View baseInfoRootView(BaseViewHolder holder);
}
