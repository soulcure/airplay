package com.coocaa.tvpi.module.logcat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.tvpilib.R;

import java.io.File;
import java.util.ArrayList;


public class ScanFileAdapter extends RecyclerView.Adapter<ScanFileAdapter.ScanFileHolder> {
    private Context mContext;
    private ArrayList<File> mDataList;

    public ScanFileAdapter(Context mContext) {
        this.mContext = mContext;
        mDataList = new ArrayList<>();
    }

    public void setDataList(ArrayList<File> dataList) {
        mDataList = dataList;
    }

    @Override
    public ScanFileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new ScanFileHolder(view);
    }

    @Override
    public void onBindViewHolder(final ScanFileHolder holder, final int position) {
        if (position == 0) {
            holder.fileNameTv.setTextColor(Color.RED);
        } else {
            holder.fileNameTv.setTextColor(Color.BLACK);
        }
        holder.fileNameTv.setText(mDataList.get(position).getName());
        holder.fileNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ShowLogActivity.class);
                intent.putExtra(ShowLogActivity.FILE_PAHT, mDataList.get(position).getPath());
                ((Activity) mContext).startActivity(intent);
            }
        });
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkFileUriExposure();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mDataList.get(position)));
                sendIntent.setType("*/*");
                ((Activity) mContext).startActivity(sendIntent);
            }
        });
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileUtil.deleteFile(mDataList.get(position).getPath());
                mDataList.remove(mDataList.get(position));
                notifyDataSetChanged();
            }
        });
    }

    /**
     * 分享前必须执行本代码，主要用于兼容SDK18以上的系统
     * 否则会报android.os.FileUriExposedException: file:///xxx.pdf exposed beyond app through ClipData.Item.getUri()
     */
    private void checkFileUriExposure() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class ScanFileHolder extends RecyclerView.ViewHolder {
        private TextView fileNameTv;
        private Button shareBtn, deleteBtn;

        public ScanFileHolder(View itemView) {
            super(itemView);
            fileNameTv = (TextView) itemView.findViewById(R.id.itemContent);
            shareBtn = (Button) itemView.findViewById(R.id.shareBtn);
            deleteBtn = (Button) itemView.findViewById(R.id.deleteBtn);
        }
    }
}
