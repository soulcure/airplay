package com.coocaa.tvpi.module.local.album;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.tvpilib.R;

import androidx.annotation.Nullable;

/**
 * 提供加载图片宽高
 *
 * @author zhufeng on 2017/10/26
 */
@SuppressLint("AppCompatCustomView")
public class GlideImageView extends ImageView implements IRender {
    private static final String TAG = "GlideImageView";
    private Context mContext;
    private int mWidth = -1;
    private int mHeight = -1;

    private RequestListener<Drawable> mRequestListener = new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable>
                target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                       DataSource dataSource, boolean isFirstResource) {
            if (resource == null) {
                return false;
            }
            int preWidth = resource.getIntrinsicWidth();
            int preHeight = resource.getIntrinsicHeight();
            if (preWidth != mWidth || preHeight != mHeight) {
                mWidth = preWidth;
                mHeight = preHeight;
                onRender(mWidth, mHeight);
            }
            return false;
        }
    };

    public GlideImageView(Context context) {
        this(context, null);
    }

    public GlideImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GlideImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void loadImage(int resizeX, int resizeY, Uri uri) {
        GlideApp.with(mContext)
                .load(uri)
                .override(resizeX, resizeY)
                .placeholder(R.drawable.ic_load_error)
                .error(R.drawable.ic_load_error)
                .priority(Priority.HIGH)
                .skipMemoryCache(true)
                .listener(mRequestListener)
                .into(this);
    }

    /**
     * load 1080P image
     *
     * @param uri
     */
    @Override
    public void loadImage(Uri uri) {
        loadImage(1080, 1920, uri);
    }

    @Override
    public void onRender(int width, int height) {

    }
}
