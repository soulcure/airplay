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
import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

public class AlbumAndVideoPreviewListAdapter extends RecyclerView.Adapter {

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

    public AlbumAndVideoPreviewListAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(List<ImageData> imageDataList, List<VideoData> videoDataList) {
        if (imageDataList != null) {
            this.imageDataList = imageDataList;
        }
        if (videoDataList != null) {
            this.videoDataList = videoDataList;
        }
        sortMediaList();
        notifyDataSetChanged();
    }

    public void setVideoData(List<VideoData> videoDataList) {
        if (videoDataList != null) {
            this.videoDataList = videoDataList;
            sortMediaList();
            notifyDataSetChanged();
        }
    }

    public void setImageData(List<ImageData> imageDataList) {
        if (imageDataList != null) {
            this.imageDataList = imageDataList;
            sortMediaList();
            notifyDataSetChanged();
        }
    }

    private void sortMediaList() {
        int imageIndex = 0;
        int videoIndex = 0;
        mediaDataList.clear();
        while (imageIndex < imageDataList.size() || videoIndex < videoDataList.size()) {

            if (videoIndex == videoDataList.size()) {
                mediaDataList.add(imageDataList.get(imageIndex));
                imageIndex++;
                continue;
            }

            if (imageIndex == imageDataList.size()) {
                mediaDataList.add(videoDataList.get(videoIndex));
                videoIndex++;
                continue;
            }

            if (imageDataList.get(imageIndex).takeTime.after(videoDataList.get(videoIndex).takeTime)) {
                mediaDataList.add(imageDataList.get(imageIndex));
                imageIndex++;
            } else {
                mediaDataList.add(videoDataList.get(videoIndex));
                videoIndex++;
            }
        }
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
                    .load(imageData.url)
                    .centerCrop()
                    .into(imgAlbumPreview);

            lyTarget.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
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
                    .into(imgAlbumPreview);

            lyTarget.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
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
