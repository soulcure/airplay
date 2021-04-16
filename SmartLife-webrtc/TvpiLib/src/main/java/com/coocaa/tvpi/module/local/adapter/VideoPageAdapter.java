package com.coocaa.tvpi.module.local.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.coocaa.publib.data.local.VideoData;
import com.coocaa.tvpi.module.local.album.AlbumViewPager;
import com.coocaa.tvpi.module.local.album.PhotoView;
import com.coocaa.tvpilib.R;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

/**
 * @ClassName VideoPageAdapter
 * @User heni
 * @Date 2019/5/22
 */
public class VideoPageAdapter extends PagerAdapter {

    private static String TAG = VideoPageAdapter.class.getSimpleName();

    private Context mContext;
    private List<VideoData> mDataList;
    private HashMap<Integer, View> mViewCache;

    public VideoPageAdapter(Context mContext, List<VideoData> mDataList) {
        this.mContext = mContext;
        this.mDataList = mDataList;
        mViewCache = new HashMap<>();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container, position, object);
        View mainView = mViewCache.get(position);
        PhotoView photoView = mainView.findViewById(R.id.local_video_player_cover_img);
        ImageView imageView = mainView.findViewById(R.id.local_video_player_start);
        ((AlbumViewPager) container).setCurrPhotoView(photoView);
        ((AlbumViewPager) container).setCurrStartView(imageView);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View mainView = mViewCache.get(position);
        if (mainView == null) {
            mainView = View.inflate(mContext, R.layout.local_video_player_page_item, null);
            PhotoView photoView = mainView.findViewById(R.id.local_video_player_cover_img);
            photoView.openPullToFinish();

            String pathName = mDataList.get(position).thumbnailPath;
            Log.d(TAG, "instantiateItem: pathName: " + pathName);
            if (TextUtils.isEmpty(pathName)) {
                photoView.loadImage(null);
            } else {
                try {
                    Uri imgUri = Uri.fromFile(new File(pathName));
                    // loadIntoUseFitWidth(mContext, imgUri.toString(), R.drawable.ic_load_error, photoView);
                    photoView.loadImage(imgUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mViewCache.put(position, mainView);
        }
        container.addView(mainView);
        return mainView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
        View mainView = mViewCache.get(position);
        if (mainView != null) {
            mViewCache.remove(position);
            Log.d(TAG, "destroyItem: position:" + position + " , " + mDataList.get(position).tittle);
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    /*public void loadIntoUseFitWidth(Context context, String imgUrl, int
            errorImageId, final ImageView imageView) {
        //我这里是先获取屏幕的宽高，然后把屏幕的宽设为imageView的宽。
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.width = width;
        imageView.setLayoutParams(params);//glide是在listener()
        // 方法中传入一个RequestListener来设置当图片资源准备好了以后自定义的操作的。
        GlideApp.with(context).load(imgUrl).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .listener(new RequestListener<Drawable>() {

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                        Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable>
                    target, DataSource dataSource, boolean isFirstResource) {
                if (imageView == null) {
                    return false;
                }
                //首先设置imageView的ScaleType属性为ScaleType.FIT_XY，让图片不按比例缩放，把图片塞满整个View。
                if (imageView.getScaleType() != ImageView.ScaleType.FIT_XY) {
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }
                //得到当前imageView的宽度（我设置的是屏幕宽度），获取到imageView与图片宽的比例，然后通过这个比例去设置imageView的高
                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                int vw = imageView.getWidth() - imageView.getPaddingLeft() -
                        imageView.getPaddingRight();
                float scale = (float) vw / (float) resource.getIntrinsicWidth();
                int vh = Math.round(resource.getIntrinsicHeight() * scale);
                params.height = vh + imageView.getPaddingTop() + imageView
                        .getPaddingBottom();
                imageView.setLayoutParams(params);
                return false;
            }
        }).placeholder(errorImageId).error(errorImageId).into(imageView);
    }*/
}
