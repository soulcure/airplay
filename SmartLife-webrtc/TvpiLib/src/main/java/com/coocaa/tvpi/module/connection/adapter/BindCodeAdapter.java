package com.coocaa.tvpi.module.connection.adapter;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BindCodeAdapter extends RecyclerView.Adapter {

    private List<String> bindCodeList = new ArrayList<>();
    private HashMap<String, Integer> bindCodeMap = new HashMap<>();

    public BindCodeAdapter() {
        bindCodeMap.put("0", R.drawable.icon_input_code_zero);
        bindCodeMap.put("1", R.drawable.icon_input_code_one);
        bindCodeMap.put("2", R.drawable.icon_input_code_two);
        bindCodeMap.put("3", R.drawable.icon_input_code_three);
        bindCodeMap.put("4", R.drawable.icon_input_code_four);
        bindCodeMap.put("5", R.drawable.icon_input_code_five);
        bindCodeMap.put("6", R.drawable.icon_input_code_six);
        bindCodeMap.put("7", R.drawable.icon_input_code_seven);
        bindCodeMap.put("8", R.drawable.icon_input_code_eight);
        bindCodeMap.put("9", R.drawable.icon_input_code_nine);
    }

    public void addBindCode(String bindCode) {
        if(bindCodeList != null && bindCodeList.size() < 8){
            this.bindCodeList.add(bindCode);
            notifyDataSetChanged();
        }
    }

    public void deleteBindCode() {
        if(bindCodeList != null&&this.bindCodeList.size() > 0){
            this.bindCodeList.remove(bindCodeList.size()-1);
            notifyDataSetChanged();
        }
    }

    public int getLength(){
        if(bindCodeList != null){
            return bindCodeList.size();
        }
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType ==0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bind_code, parent, false);
            return new BindCodeViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bind_code_cursor, parent, false);
            return new CursorHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BindCodeViewHolder) {
            ((BindCodeViewHolder) holder).setData(position);
        }
    }

    @Override
    public int getItemCount() {
        return bindCodeList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == bindCodeList.size() ? 1 : 0;
    }

    public String getBindCode() {
        StringBuffer sb = new StringBuffer();
        for (String bindCode : bindCodeList) {
            sb.append(bindCode);
        }
        return sb.toString();
    }

    private class BindCodeViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgBIndCode;

        public BindCodeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBIndCode = itemView.findViewById(R.id.tv_bind_code);
        }

        public void setData(int position) {
            String key = bindCodeList.get(position);
            if(TextUtils.isEmpty(key)){
                return;
            }
            Integer resourceId = bindCodeMap.get(key);
            if(resourceId != null){
                imgBIndCode.setImageResource(resourceId);
            }

        }
    }

    private class CursorHolder extends RecyclerView.ViewHolder {

        public CursorHolder(@NonNull View itemView) {
            super(itemView);
            View cursor = itemView.findViewById(R.id.cursor);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(cursor, "alpha", 1, 0, 0, 1);
            alpha.setDuration(1000);
            alpha.setRepeatCount(ValueAnimator.INFINITE);
            alpha.start();
        }

    }
}
