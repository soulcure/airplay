package com.coocaa.tvpi.module.homepager.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.tvpi.module.homepager.adapter.bean.HeaderFunctionBean;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 智屏首页的功能按钮
 * Created by songxing on 2020/3/18
 */
public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.FunctionHolder> {
    private List<HeaderFunctionBean> functionBeanLis = new ArrayList<>();
    private Context context;
    private FunctionClickListener listener;

    public FunctionAdapter(Context context) {
        this.context = context;
    }

    public void setFunctionBeanLis(List<HeaderFunctionBean> functionBeanLis) {
        if (functionBeanLis != null) {
            this.functionBeanLis = functionBeanLis;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public FunctionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_function, parent, false);
        return new FunctionHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull FunctionHolder holder, final int position) {
        HeaderFunctionBean functionBean = functionBeanLis.get(position);
        holder.name.setText(functionBean.name);

//        GlideApp.with(context)
//                .load(functionBean.icon)
//                .centerCrop()
//                .into(holder.icon);
        holder.icon.setBackgroundResource(functionBean.icon);

        if (functionBean.type == 0) {
            holder.name.setTextColor(context.getColor(R.color.c_1));
            holder.name.setBackground(null);
        } else {
            holder.name.setTextColor(context.getColor(R.color.c_6));
            holder.name.setBackgroundResource(R.drawable.bg_text_mirror_screen);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onFunctionClick(functionBean.functionType);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return functionBeanLis.size();
    }

    class FunctionHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView name;

        public FunctionHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_function);
            name = itemView.findViewById(R.id.tv_function_name);
        }
    }

    public void setFunctionClickListener(FunctionClickListener listener) {
        this.listener = listener;
    }

    public interface FunctionClickListener {
        void onFunctionClick(@HeaderFunctionBean.FunctionType int functionType);
    }
}
