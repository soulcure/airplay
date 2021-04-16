package com.coocaa.tvpi.module.newmovie.adapter.holder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.tvpilib.R;


/**
 * @ClassName PushHistoryTitleHolder
 * @Description
 * @User heni
 * @Date 18-8-31
 */
public class PushHistoryTitleHolder extends RecyclerView.ViewHolder {

    private Context mContext;
    private TextView title;

    public PushHistoryTitleHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();
        title = itemView.findViewById(R.id.item_push_history_title);
    }

    public void onBind(String strTititle) {
        if(!TextUtils.isEmpty(strTititle)) {
            title.setText(strTititle);
        }
    }
}
