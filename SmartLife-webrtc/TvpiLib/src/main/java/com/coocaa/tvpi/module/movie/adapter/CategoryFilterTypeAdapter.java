package com.coocaa.tvpi.module.movie.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.smartscreen.data.movie.CategoryFilterModel;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IceStorm on 2017/12/19.
 */

public class CategoryFilterTypeAdapter extends RecyclerView.Adapter <CategoryFilterTypeAdapter.CategoryViewHolder> implements View.OnClickListener{

    private static final String TAG = "CategoryFilterTypeAdapt";

    private int curSelectedPosition = -1;
    private List<CategoryFilterModel> dataList = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener = null;

    //define interface
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public CategoryFilterTypeAdapter(int count) {
    }

    public void addAll(List<CategoryFilterModel> videoList) {
        dataList.clear();
        dataList.addAll(videoList);
        notifyDataSetChanged();
    }

    public int getCurSelectedPosition() {
        return curSelectedPosition;
    }

    public void setSelected(int position) {
        if (curSelectedPosition != -1) {
            dataList.get(curSelectedPosition).isSelected = false;
            notifyItemChanged(curSelectedPosition);
        }
        if (dataList.size() > 0) {
            dataList.get(position).isSelected = true;
            curSelectedPosition = position;
        }

        notifyItemChanged(position);
    }

    public CategoryFilterModel getSelected() {
        try {
            return dataList.get(curSelectedPosition);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_type_list, parent, false);
        //????????????View??????????????????
        view.setOnClickListener(this);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder viewHolder, int position) {
        viewHolder.textView.setText(dataList.get(position).title + "");

        //???position?????????itemView???Tag?????????????????????????????????
        viewHolder.itemView.setTag(position);
        if(dataList.get(position).isSelected) {
            viewHolder.textView.setTextColor(viewHolder.textView.getResources().getColor(R.color.c_1));
            viewHolder.textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else {
            viewHolder.textView.setTextColor(viewHolder.textView.getResources().getColor(R.color.c_3));
            viewHolder.textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //??????????????????getTag????????????position
            mOnItemClickListener.onItemClick(v,(int)v.getTag());
        }
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        CategoryViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.item_filter_type_list_tv_type);
        }
    }
}
