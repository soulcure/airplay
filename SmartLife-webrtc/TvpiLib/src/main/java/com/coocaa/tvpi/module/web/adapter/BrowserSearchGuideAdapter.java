package com.coocaa.tvpi.module.web.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.tvpilib.R;

public class BrowserSearchGuideAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int[] dataList = new int[]{R.drawable.browser_guide_0, R.drawable.browser_guide_1,
            R.drawable.browser_guide_2, R.drawable.browser_guide_3, R.drawable.browser_guide_4};
    private Context context;

    public BrowserSearchGuideAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .item_browser_guide, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).onBind(position);
    }

    @Override
    public int getItemCount() {
        return dataList.length;
    }

    public class Holder extends RecyclerView.ViewHolder {
        private Context mContext;
        private ImageView iv;

        public Holder(final View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            iv = itemView.findViewById(R.id.iv);
        }


        public void onBind(final int position) {
            GlideApp.with(mContext).load(dataList[position]).into(iv);
        }
    }


}

