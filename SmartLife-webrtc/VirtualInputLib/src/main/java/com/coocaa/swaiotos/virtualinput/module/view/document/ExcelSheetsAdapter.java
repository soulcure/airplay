package com.coocaa.swaiotos.virtualinput.module.view.document;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.smartsdk.SmartApi;

import java.util.ArrayList;
import java.util.List;

import swaiotos.runtime.h5.core.os.H5RunType;

/**
 * @Description: Excel表格控制器表名列表
 * @Author: wzh
 * @CreateDate: 3/15/21
 */
public class ExcelSheetsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> mDatas = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private String mCurSheetName;

    public ExcelSheetsAdapter() {

    }

    public void setData(List<String> data) {
        mDatas.clear();
        mDatas.addAll(data);
        notifyDataSetChanged();
    }

    public void setCurSheetName(String curSheetName) {
        mCurSheetName = curSheetName;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_doc_sheets_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).setData(mDatas.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private Context mContext;
        private View mFocusView;
        private TextView mSheetName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            mFocusView = itemView.findViewById(R.id.focus_view);
            mSheetName = itemView.findViewById(R.id.sheet_name);
        }

        public void setData(final String sheetName, int position) {
            mSheetName.setText(sheetName);
            if (sheetName.equals(mCurSheetName)) {
                mSheetName.setTextColor(Color.parseColor("#F86239"));
                mSheetName.setBackgroundColor(Color.parseColor("#1af86239"));
                mFocusView.setBackgroundResource(R.drawable.bg_doc_preview_focus);
            } else {
                mSheetName.setTextColor(mContext.getResources().getColor(R.color.color_white_60));
                mSheetName.setBackgroundColor(mContext.getResources().getColor(R.color.color_white_10));
                mFocusView.setBackgroundResource(0);
            }
            mSheetName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (sheetName.equals(mCurSheetName)) {
                        return;
                    }
                    //发布会需求，不在同一wifi，弹连接wifi弹窗，防止被误点
                    if (!SmartApi.isSameWifi()) {
                        SmartApi.startConnectSameWifi(H5RunType.RUNTIME_NETWORK_FORCE_LAN);
                        return;
                    }
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(sheetName);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String sheetName);
    }
}
