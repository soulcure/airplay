package com.coocaa.tvpi.module.local.album;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.viewpager.widget.ViewPager;

/**
 * @ClassName AlbumViewPager
 * @Description 相册ViewPager
 * @User heni
 * @Date 2020-04-23
 */
public class AlbumViewPager extends ViewPager {
    private PhotoView mCurrentView;
    private ImageView mStartImg;
    private View.OnClickListener mOnClickListener;
    /**
     * 下拉监听
     */
    private OnPullProgressListener mProgressListener;

    public AlbumViewPager(Context context) {
        super(context);
    }

    public AlbumViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof PhotoView) {
            return ((PhotoView) v).canScroll(dx) || super.canScroll(v, checkV, dx, x, y);
        }
        return super.canScroll(v, checkV, dx, x, y);
    }

    public void setCurrPhotoView(PhotoView currentView) {
        this.mCurrentView = null;
        this.mCurrentView = currentView;
        mCurrentView.setOnPullProgressListener(new OnPullProgressListener() {
            @Override
            public void startPull() {
                if (mProgressListener != null) {
                    mProgressListener.startPull();
                    if (mStartImg != null) {
                        mStartImg.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onProgress(float progress) {
                if (mProgressListener != null) {
                    mProgressListener.onProgress(progress);
                }
            }

            @Override
            public void stopPull(boolean isFinish) {
                if (mProgressListener != null) {
                    mProgressListener.stopPull(isFinish);
                    if (!isFinish && mStartImg != null) {
                        mStartImg.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    /**
     * 本地视频放大后有覆盖的图片，共用了此viewpage
     */
    public void setCurrStartView(ImageView imageView) {
        mStartImg = imageView;
        if (mStartImg != null) {
            mStartImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnClickListener != null) {
                        mOnClickListener.onClick(v);
                    }
                }
            });
        }
    }

    @Override
    public void setOnClickListener(View.OnClickListener l) {
        this.mOnClickListener = l;
    }

    public void setOnPullProgressListener(OnPullProgressListener l) {
        this.mProgressListener = l;
    }
}
