package com.coocaa.tvpi.module.onlineservice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.qiyukf.unicorn.api.ImageLoaderListener;
import com.qiyukf.unicorn.api.UnicornImageLoader;

import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @ClassName: QiyuImageLoader
 * @Author: AwenZeng
 * @CreateDate: 2021/3/29 14:45
 * @Description: 网易七鱼在线客服图片加载类
 */
public class QiyuImageLoader implements UnicornImageLoader {


    private Context context;

    public QiyuImageLoader(Context context) {
        this.context = context.getApplicationContext();
    }
    @Nullable
    @Override
    public Bitmap loadImageSync(String uri, int i, int i1) {
        Bitmap bitmap = null;
        try {
            bitmap = Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .submit().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    public void loadImage(String uri, int width, int height, final ImageLoaderListener listener) {
        if (width <= 0) {
            width = Integer.MIN_VALUE;
        }
        if (height <= 0) {
            height = Integer.MIN_VALUE;
        }
        Glide.with(context).
                asBitmap()
                .load(uri)
                .into(new CustomTarget<Bitmap>(width, height) {

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {

                    }

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        if (listener != null) {
                            listener.onLoadComplete(resource);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }
}
