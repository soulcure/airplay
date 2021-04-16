package com.coocaa.tvpi.module.local.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.local.ImageData;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

public class VideoPreviewListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<VideoData> videoDataList = new ArrayList<>();
    private OnPictureItemClickListener mOnPictureItemClickListener;

    public interface OnPictureItemClickListener {
        void onPictureItemClick(int position, VideoData videoData);
    }

    public VideoPreviewListAdapter(Context context){
        this.mContext = context;
    }

    public void setVideoDataList(List<VideoData> videoDataList){
        if(videoDataList != null){
            this.videoDataList = videoDataList;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_preview2, parent, false);
        return new VideoPreviewListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((VideoPreviewListAdapter.ViewHolder) holder).setData(position);
    }

    @Override
    public int getItemCount() {
        return videoDataList == null ? 0 : videoDataList.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgAlbumPreview;
        public LinearLayout lyTarget;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAlbumPreview = itemView.findViewById(R.id.album_preview_img);
            lyTarget = itemView.findViewById(R.id.album_preview_ly);
        }

        public void setData(final int positon) {
            GlideApp.with(mContext)
                    .load(videoDataList.get(positon).thumbnailPath)
                    .centerCrop()
                    .into(imgAlbumPreview);

            lyTarget.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        mOnPictureItemClickListener.onPictureItemClick(positon,videoDataList.get(positon));
                    }
                }
            });

        }
    }

    public void setOnPictureItemClickListener(OnPictureItemClickListener onPictureItemClickListener) {
        this.mOnPictureItemClickListener = onPictureItemClickListener;
    }
}
