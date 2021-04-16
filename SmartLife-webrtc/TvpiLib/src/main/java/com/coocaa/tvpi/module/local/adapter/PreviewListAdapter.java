package com.coocaa.tvpi.module.local.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.local.ImageData;
import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PreviewListAdapter extends RecyclerView.Adapter {

    private static int TYPE_IMAGE = 0;
    private static int TYPE_VIDEO = 1;

    private Context mContext;
    private List<ImageData> imageDataList = new ArrayList<>();
    private List<VideoData> videoDataList = new ArrayList<>();
    private List<MediaData> mediaDataList = new ArrayList<>();
    private OnPictureItemClickListener mOnPictureItemClickListener;

    public interface OnPictureItemClickListener {
        void onPictureItemClick(int position, MediaData imageData);
    }

    public PreviewListAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (mediaDataList.get(position).type == MediaData.TYPE.IMAGE) {
            return TYPE_IMAGE;
        } else {
            return TYPE_VIDEO;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_preview2, parent, false);
            return new ImageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_preview2, parent, false);
            return new VideoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (mediaDataList.get(position).type == MediaData.TYPE.VIDEO) {
            ((VideoViewHolder) holder).setData(position);
        } else {
            ((ImageViewHolder) holder).setData(position);
        }
    }

    @Override
    public int getItemCount() {
        return mediaDataList == null ? 0 : mediaDataList.size();
    }

    public List<MediaData> getMediaDataList() {
        return mediaDataList;
    }

    public void setMediaDataList(List<MediaData> mediaDataList) {
        this.mediaDataList = mediaDataList;
        notifyDataSetChanged();
    }

    private class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgAlbumPreview;
        public LinearLayout lyTarget;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAlbumPreview = itemView.findViewById(R.id.album_preview_img);
            lyTarget = itemView.findViewById(R.id.album_preview_ly);
        }

        public void setData(final int positon) {
            ImageData imageData = (ImageData) mediaDataList.get(positon);

            GlideApp.with(mContext)
                    .asBitmap()
                    .load(imageData.url)
                    .centerCrop()
                    .override(DimensUtils.dp2Px(mContext,66))
                    .skipMemoryCache(true)
                    .into(imgAlbumPreview);

            lyTarget.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        if(positon >= mediaDataList.size()){
                            return;
                        }
                        mOnPictureItemClickListener.onPictureItemClick(positon, mediaDataList.get(positon));
                    }
                }
            });

        }
    }

    private class VideoViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgAlbumPreview;
        public LinearLayout lyTarget;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAlbumPreview = itemView.findViewById(R.id.album_preview_img);
            lyTarget = itemView.findViewById(R.id.album_preview_ly);
        }

        public void setData(final int positon) {
            VideoData videoData = (VideoData) mediaDataList.get(positon);
            GlideApp.with(mContext)
                    .load(videoData.thumbnailPath)
                    .centerCrop()
                    .skipMemoryCache(true)
                    .override(DimensUtils.dp2Px(mContext, 66))
                    .into(imgAlbumPreview);

            lyTarget.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        if(positon >= mediaDataList.size()){
                            return;
                        }
                        mOnPictureItemClickListener.onPictureItemClick(positon, mediaDataList.get(positon));
                    }
                }
            });

        }
    }

    public void setOnPictureItemClickListener(OnPictureItemClickListener onPictureItemClickListener) {
        this.mOnPictureItemClickListener = onPictureItemClickListener;
    }

}
