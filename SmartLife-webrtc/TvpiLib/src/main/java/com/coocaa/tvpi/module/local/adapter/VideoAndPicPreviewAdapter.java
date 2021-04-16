package com.coocaa.tvpi.module.local.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.coocaa.publib.data.local.ImageData;
import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpi.module.local.album.AlbumViewPager;
import com.coocaa.tvpi.module.local.album.PhotoView;
import com.coocaa.tvpilib.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 视频适配器，本地图片点击放大预览界面的适配器
 * AlbumAdapter是右上角相册点击后的列表界面的适配器
 */
public class VideoAndPicPreviewAdapter extends PagerAdapter {
    private Context mContext;
    private List<ImageData> imageDataList = new ArrayList<>();
    private List<VideoData> videoDataList = new ArrayList<>();
    private List<MediaData> mediaDataList = new ArrayList<>();
    private HashMap<Integer, PhotoView> mViewCache;

    public VideoAndPicPreviewAdapter(Context mContext) {
        this.mContext = mContext;
        mViewCache = new HashMap<>();
    }

    public void setData(List<VideoData> videoDataList, List<ImageData> imageDataList){
        if(imageDataList != null){
            this.imageDataList = imageDataList;
        }

        if(videoDataList != null){
            this.videoDataList = videoDataList;
        }

        sortMediaList();
        notifyDataSetChanged();
    }

    public void setMediaDataList(List<MediaData> mediaDataList){
        if(mediaDataList != null){
            this.mediaDataList = mediaDataList;
            notifyDataSetChanged();
        }
    }

    public void setVideoDataList(List<VideoData> mDataList) {
        if (mDataList != null) {
            this.videoDataList = mDataList;
            mediaDataList.clear();
            sortMediaList();
            notifyDataSetChanged();
        }
    }

    public void setImageData(List<ImageData> imageDataList) {
        if (imageDataList != null) {
            this.imageDataList = imageDataList;
            mediaDataList.clear();
            sortMediaList();
            notifyDataSetChanged();
        }
    }

    private void sortMediaList() {
        int imageIndex = 0;
        int videoIndex = 0;
        while(imageIndex < imageDataList.size() || videoIndex < videoDataList.size()){

            if(videoIndex == videoDataList.size()){
                mediaDataList.add(imageDataList.get(imageIndex));
                imageIndex++;
                continue;
            }

            if(imageIndex == imageDataList.size()){
                mediaDataList.add(videoDataList.get(videoIndex));
                videoIndex++;
                continue;
            }

            if(imageDataList.get(imageIndex).takeTime.after(videoDataList.get(videoIndex).takeTime)){
                mediaDataList.add(imageDataList.get(imageIndex));
                imageIndex++;
            }else {
                mediaDataList.add(videoDataList.get(videoIndex));
                videoIndex++;
            }
        }
    }

    @Override
    public int getCount() {
        return mediaDataList.size();
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        if(mediaDataList.get(position).type == MediaData.TYPE.VIDEO){
            FrameLayout frameLayout = new FrameLayout(mContext);
            ViewGroup.LayoutParams frameParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            ImageView photoView = new ImageView(mContext);
//        photoView.openPullToFinish();
//        photoView.loadImage(Uri.fromFile(new File(mDataList.get(position).thumbnailPath)));
            VideoData videoData = (VideoData) mediaDataList.get(position);
            Glide.with(mContext)
                    .load(videoData.thumbnailPath)
                    .into(photoView);
            FrameLayout.LayoutParams photoParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
            frameLayout.addView(photoView, photoParams);

            ImageView ivPlay = new ImageView(mContext);
            ivPlay.setBackgroundResource(R.drawable.icon_video_play);
            FrameLayout.LayoutParams playParams = new FrameLayout.LayoutParams(DimensUtils.dp2Px(mContext, 80),
                    DimensUtils.dp2Px(mContext, 80), Gravity.CENTER);
            frameLayout.addView(ivPlay, playParams);


            ivPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    videoPlayListener.onStartPlayClick(position);
                }
            });
            container.addView(frameLayout, frameParams);
            return frameLayout;
        } else {
            ImageData imageData = (ImageData) mediaDataList.get(position);
            PhotoView photoView = mViewCache.get(position);
            if (photoView == null) {
                photoView = new PhotoView(mContext);
                photoView.openPullToFinish();
                photoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                photoView.loadImage( Uri.fromFile(new File(imageData.url)));
                mViewCache.put(position, photoView);
            }
            container.addView(photoView);
            return photoView;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        PhotoView photoView = mViewCache.get(position);
        if (photoView != null) {
            mViewCache.remove(position);
        }
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        PhotoView photoView = mViewCache.get(position);
        if(photoView != null) {
            ((AlbumViewPager) container).setCurrPhotoView(photoView);
        }

    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public List<MediaData> getMediaDataList() {
        return mediaDataList;
    }

    private VideoPlayListener videoPlayListener;

    public void setVideoPlayListener(VideoPlayListener videoPlayListener) {
        this.videoPlayListener = videoPlayListener;
    }

    public interface VideoPlayListener {

        void onStartPlayClick(int position);
    }
}
