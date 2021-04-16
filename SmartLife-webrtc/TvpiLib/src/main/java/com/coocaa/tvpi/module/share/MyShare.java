package com.coocaa.tvpi.module.share;

import android.app.Activity;
import android.graphics.Bitmap;

import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.HashMap;
import java.util.Map;

import swaiotos.share.api.define.ShareObject;

/**
 * @Author: yuzhan
 */
public abstract class MyShare {
    protected ShareObject shareObject;
    protected Bitmap thumbBitmap;
    protected int thumbResId = 0;
    protected UMShareListener shareListener;

    protected final static String MODE = "fx";

    public MyShare(ShareObject shareObject) {
        this.shareObject = shareObject;
    }

    public void setText(String text) {
        shareObject.text = text;
    }

    public void setTitle(String title) {
        shareObject.title = title;
    }

    public void setDescription(String description) {
        shareObject.description = description;
    }

    public void setThumb(String thumb) {
        shareObject.thumb = thumb;
    }

    public void setUrl(String url) {
        shareObject.url = url;
    }

    public void setExtra(Map<String, String> extra) {
        shareObject.extra = extra;
    }

    public void setThumbBitmap(Bitmap thumbBitmap) {
        this.thumbBitmap = thumbBitmap;
    }

    public void setThumbResId(int thumbResId) {
        this.thumbResId = thumbResId;
    }

    public void setFrom(String from) {
        shareObject.from = from;
    }

    public void putExtra(String key, String value) {
        if(shareObject.extra == null) {
            shareObject.extra = new HashMap<>();
        }
        shareObject.extra.put(key, value);
    }

    public void setShareListener(UMShareListener shareListener) {
        this.shareListener = shareListener;
    }

    public abstract void share(Activity activity, SHARE_MEDIA shareMedia);
}
