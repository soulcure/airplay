package com.coocaa.tvpi.module.local.album;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;

import androidx.viewpager.widget.PagerAdapter;

/**
 * 相册适配器，本地图片点击放大预览界面的适配器
 * AlbumAdapter是右上角相册点击后的列表界面的适配器
 */
public class AlbumPreviewAdapter extends PagerAdapter {
    private Context mContext;
    private List<Uri> mDataList;
    private HashMap<Integer, PhotoView> mViewCache;

    public AlbumPreviewAdapter(Context mContext, List<Uri> mDataList) {
        this.mContext = mContext;
        this.mDataList = mDataList;
        mViewCache = new HashMap<>();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        PhotoView photoView = mViewCache.get(position);
        ((AlbumViewPager) container).setCurrPhotoView(photoView);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = mViewCache.get(position);
        if (photoView == null) {
            photoView = new PhotoView(mContext);
            photoView.openPullToFinish();
            photoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            photoView.loadImage(mDataList.get(position));
            mViewCache.put(position, photoView);
        }
        container.addView(photoView);
        return photoView;
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
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
