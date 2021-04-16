package com.coocaa.tvpi.module.homepager.main.vy21m4.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.tvpi.module.homepager.main.vy21m4.beans.FuncData;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @ClassName SharedSpaceAdapter
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 4/7/21
 * @Version TODO (write something)
 */
public class FuncAdapter extends RecyclerView.Adapter {

    private Context mContext;

    private List<FunctionBean> mDataList = new ArrayList<>();

    public FuncAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.func_item_holder_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).onBind(mDataList.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void addAll(List<FunctionBean> dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ImageView icon;
        TextView title;
        TextView subtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            icon = itemView.findViewById(R.id.func_item_icon);
            title = itemView.findViewById(R.id.func_item_title);
            subtitle = itemView.findViewById(R.id.func_item_subtitle);
        }

        public void onBind(FunctionBean data) {
            if (data == null)
                return;

            GlideApp.with(icon).load(data.icon).into(icon);
            title.setText(data.name);
            subtitle.setText(data.subtitle);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TvpiClickUtil.onClick(itemView.getContext(),data.uri());
                }
            });
        }
    }
}