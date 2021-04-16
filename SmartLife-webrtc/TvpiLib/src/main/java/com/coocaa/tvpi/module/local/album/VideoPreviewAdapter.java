package com.coocaa.tvpi.module.local.album;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频适配器，本地图片点击放大预览界面的适配器
 * AlbumAdapter是右上角相册点击后的列表界面的适配器
 */
public class VideoPreviewAdapter extends PagerAdapter {
    private Context mContext;
    private List<VideoData> mDataList = new ArrayList<>();

    public VideoPreviewAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setDataList(List<VideoData> mDataList) {
        if (mDataList != null) {
            this.mDataList = mDataList;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        FrameLayout frameLayout = new FrameLayout(mContext);
        ViewGroup.LayoutParams frameParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        ImageView photoView = new ImageView(mContext);
//        photoView.openPullToFinish();
//        photoView.loadImage(Uri.fromFile(new File(mDataList.get(position).thumbnailPath)));
        Glide.with(mContext)
                .load(mDataList.get(position).thumbnailPath)
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
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    private VideoPlayListener videoPlayListener;

    public void setVideoPlayListener(VideoPlayListener videoPlayListener) {
        this.videoPlayListener = videoPlayListener;
    }

    public interface VideoPlayListener {

        void onStartPlayClick(int position);
    }
}
