package com.coocaa.tvpi.module.local.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.tvpilib.R;

import java.util.List;

/**
 * @Description: 下拉选择弹窗adapter
 * @Author: wzh
 * @CreateDate: 12/9/20
 */
public class CustomSpinnerAdapter extends BaseAdapter {

    private List<String> mDataList;
    private LayoutInflater mInflater;
    private LayoutInflater mDropDownInflater;
    private ImageView mIvArrow;
    private int mSelectPos = 0;

    public CustomSpinnerAdapter(Context context, List<String> list) {
        this.mInflater = LayoutInflater.from(context.getApplicationContext());
        this.mDropDownInflater = LayoutInflater.from(context.getApplicationContext());
        this.mDataList = list;
    }

    public void setSelect(int pos) {
        mSelectPos = pos;
    }

    public void setArrowIcon(int resId) {
        if (mIvArrow != null) {
            mIvArrow.setImageResource(resId);
        }
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int i) {
        return mDataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getDropDownView(int i, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.layout_doc_spinner_dropdown_item, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.title.setText((String) getItem(i));
        if (mSelectPos == i) {
            holder.selectIv.setVisibility(View.VISIBLE);
            holder.title.setTextColor(Color.parseColor("#2B5ADB"));
        } else {
            holder.selectIv.setVisibility(View.GONE);
            holder.title.setTextColor(Color.parseColor("#333333"));
        }
        if (i == getCount() - 1) {
            holder.line.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;
        if (convertView == null) {
            view = mDropDownInflater.inflate(R.layout.layout_doc_spinner_item, parent, false);
        } else {
            view = convertView;
        }
        TextView title = view.findViewById(R.id.btn_all_type);
        mIvArrow = view.findViewById(R.id.iv_arrow);
        mIvArrow.setImageResource(R.drawable.doc_icon_more_arrow_down);
        title.setText((String) getItem(position));
        return view;
    }

    static class ViewHolder {
        TextView title;
        ImageView selectIv;
        View line;

        ViewHolder(View view) {
            title = view.findViewById(R.id.tv_title);
            selectIv = view.findViewById(R.id.iv_select);
            line = view.findViewById(R.id.line);
        }
    }

}
