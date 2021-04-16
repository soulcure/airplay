package com.coocaa.tvpi.module.local.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.local.ImageData;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.tvpi.module.local.PictureAndVideoActivity;
import com.coocaa.tvpi.module.local.utils.MediaStoreHelper;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AlbumAdapter2 extends RecyclerView.Adapter {

    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_VIDEO = 2;

    private Context mContext;
    private List<String> groupList;
    private HashMap<String, ArrayList<ImageData>> allImageMap;
    private List<VideoData> videoDataList;

    public AlbumAdapter2(Context context, List<String> groupList, HashMap<String, ArrayList<ImageData>> data,List<VideoData> videoDataList) {
        mContext = context;
        this.groupList = groupList;
        this.allImageMap = data;
        this.videoDataList = videoDataList;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.local_item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_IMAGE) {
            ((ViewHolder) holder).setImageData(position);
            return;
        }
        if (getItemViewType(position) == TYPE_VIDEO) {
            ((ViewHolder) holder).setVideoData();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < groupList.size()) {
            return TYPE_IMAGE;
        } else {
            return TYPE_VIDEO;
        }
    }

    @Override
    public int getItemCount() {
        int size = groupList == null ? 0 : groupList.size();
        if (videoDataList != null && videoDataList.size() > 0) {
            size = size + 1;
        }
        return size;
    }

    public void addVideoData(List<VideoData> dataList) {
        this.videoDataList = dataList;
        notifyDataSetChanged();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTV;
        public TextView numTV;
        public ImageView coverIV;
        public View itemView;

        public ViewHolder(View view) {
            super(view);
            itemView = view;
            nameTV = view.findViewById(R.id.item_album_name);
            numTV = view.findViewById(R.id.item_album_num);
            coverIV = view.findViewById(R.id.item_album_cover);
        }

        public void setImageData(int positon) {
            try {
                final String nameStr = groupList.get(positon);
                nameTV.setText(nameStr);
                if (nameStr.equals(MediaStoreHelper.MAIN_ALBUM_NAME) && allImageMap.get(nameStr) != null) {
                    initItemAll(nameStr);
                    return;
                }
                ArrayList<ImageData> imageDataList = allImageMap.get(nameStr);
                numTV.setText(String.valueOf(imageDataList.size()));
                GlideApp.with(mContext)
                        .load(imageDataList.get(0).url)
                        .centerCrop()
                        .into(coverIV);
                itemView.setOnClickListener(v -> {
                    mContext.startActivity(new Intent(mContext, PictureAndVideoActivity.class)
                            .putExtra(PictureAndVideoActivity.KEY_ALBUM_NAME, nameStr)
                            .putExtra(PictureAndVideoActivity.KEY_SHOW_TYPE, PictureAndVideoActivity.SHOW_IMAGE));
                    ((Activity) mContext).finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void initItemAll(String nameStr) {
            nameTV.setText("最近项目");
            ArrayList<ImageData> imageDataList = allImageMap.get(nameStr);
            if (videoDataList != null) {
                numTV.setText(String.valueOf(imageDataList.size() + videoDataList.size()));
                initCoverTV(imageDataList);
            } else {
                numTV.setText(String.valueOf(imageDataList.size()));
            }
            itemView.setOnClickListener(v -> {
                mContext.startActivity(new Intent(mContext, PictureAndVideoActivity.class).putExtra(PictureAndVideoActivity.KEY_SHOW_TYPE, PictureAndVideoActivity.SHOW_ALL));
                ((Activity) mContext).finish();
            });
        }

        private void initCoverTV(ArrayList<ImageData> imageDataList) {
            if (videoDataList.size() == 0 && imageDataList.size() == 0) {
                return;
            }

            if (imageDataList.size() == 0) {
                GlideApp.with(mContext)
                        .load(videoDataList.get(0).thumbnailPath)
                        .centerCrop()
                        .into(coverIV);
                return;
            }

            if (videoDataList.size() == 0) {
                GlideApp.with(mContext)
                        .load(imageDataList.get(0).url)
                        .centerCrop()
                        .into(coverIV);
                return;
            }

            if (videoDataList.get(0).takeTime.before(imageDataList.get(0).takeTime)) {
                GlideApp.with(mContext)
                        .load(imageDataList.get(0).url)
                        .centerCrop()
                        .into(coverIV);
            } else {
                GlideApp.with(mContext)
                        .load(videoDataList.get(0).thumbnailPath)
                        .centerCrop()
                        .into(coverIV);
            }
        }

        public void setVideoData() {
            try {
                nameTV.setText("视频");
                numTV.setText(String.valueOf(videoDataList.size()));
                GlideApp.with(mContext)
                        .load(videoDataList.get(0).thumbnailPath)
                        .centerCrop()
                        .into(coverIV);
                itemView.setOnClickListener(v -> {
                    mContext.startActivity(new Intent(mContext, PictureAndVideoActivity.class).putExtra(PictureAndVideoActivity.KEY_SHOW_TYPE, PictureAndVideoActivity.SHOW_VIDEO));
                    ((Activity) mContext).finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
