package com.coocaa.tvpi.util;

import android.widget.ImageView;

import com.coocaa.publib.base.GlideApp;

public class ImageUtils {
    public static void load(ImageView view,String url){
        GlideApp.with(view.getContext())
                .load(url)
                .into(view);
    }
}
