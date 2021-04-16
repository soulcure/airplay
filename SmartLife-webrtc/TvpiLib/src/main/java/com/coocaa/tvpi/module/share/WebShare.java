package com.coocaa.tvpi.module.share;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.coocaa.tvpilib.R;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.util.Iterator;
import java.util.Map;

import swaiotos.share.api.define.ShareObject;

/**
 * @Author: yuzhan
 */
public class WebShare extends MyShare {

    public WebShare(ShareObject shareObject) {
        super(shareObject);
    }

    @Override
    public void share(Activity activity, SHARE_MEDIA shareMedia) {
        String appName = getAppName(activity);
        if(TextUtils.isEmpty(shareObject.title)) {
            shareObject.title = "安利你一款敲好用的大屏互动神器 - 稳定专业极速！";
        }
        if(TextUtils.isEmpty(shareObject.description)) {
            shareObject.description = "高效办公会议、家庭影音中心、移动智慧大屏等多种大屏互动场景等你来玩！";
        }
        if(TextUtils.isEmpty(shareObject.text)) {
            shareObject.text = shareObject.description;
        }
        if(TextUtils.isEmpty(shareObject.url)) {
            shareObject.url = "";
        }

        String url = getUrl();
        UMWeb web = new UMWeb(url);
        web.setTitle(shareObject.title);
        if(!TextUtils.isEmpty(shareObject.thumb)) {
            Glide.with(activity).asBitmap().load(shareObject.thumb).into(new SimpleTarget<Bitmap>(144, 144) {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    Log.d("SmartShare", "onResourceReady : " + resource);
                    if(resource == null || resource.isRecycled()) {
                        web.setThumb(new UMImage(activity, shareObject.thumb));
                    } else {
                        web.setThumb(new UMImage(activity, drawableBitmapOnWhiteBg(activity, resource)));
                    }
                    doShare(activity, web, shareMedia, url);
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    Log.d("SmartShare", "onLoadFailed : " + errorDrawable);
                    super.onLoadFailed(errorDrawable);
                    web.setThumb(new UMImage(activity, shareObject.thumb));
                    doShare(activity, web, shareMedia, url);
                }
            });
        } else {
            if(thumbResId == 0) {
                thumbResId = R.drawable.logo;
            }
            Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), thumbResId);
            web.setThumb(new UMImage(activity, drawableBitmapOnWhiteBg(activity, bitmap)));
            doShare(activity, web, shareMedia, url);
        }
    }

    private void doShare(Activity activity, UMWeb web, SHARE_MEDIA shareMedia, String url) {
        web.setDescription(shareObject.description);
        Log.d("SmartShare", "share : url=" + url);
        Log.d("SmartShare", "share : obj=" + shareObject.toString() + ", media=" + shareMedia);
        new ShareAction(activity)
                .setPlatform(shareMedia)//传入平台
                .withText(shareObject.text)//分享内容
                .withMedia(web)
                .setCallback(shareListener)//回调监听器
                .share();
    }

    //分享到微信，添加一个白色的底，修复四周黑色块块问题
    private Bitmap drawableBitmapOnWhiteBg(Context context, Bitmap bitmap){
        Bitmap newBitmap = Bitmap.createBitmap(144, 144, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(context.getResources().getColor(android.R.color.white));
        Paint paint=new Paint();
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return newBitmap;
    }

    private String getUrl() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http");
        builder.authority("ccss.tv");

        builder.appendQueryParameter("m", MODE);
        builder.appendQueryParameter("yw", "gxp");
        builder.appendQueryParameter("ct", "and");
        boolean hasBc = false;
        if(shareObject.extra != null && !shareObject.extra.isEmpty()) {
            Iterator<Map.Entry<String, String>> iter = shareObject.extra.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
                if(TextUtils.equals(entry.getKey(), "bc")) {
                    hasBc = true;
                }
            }
            if(!hasBc) {
                builder.appendQueryParameter("bc", shareObject.extra.get("bc"));
            }
        }

        builder.appendQueryParameter("applet", appletUrl());
        String url = builder.build().toString();
        return url;
    }

    private String appletUrl() {
        String url = shareObject.from == null ? "" : shareObject.from;
        if(TextUtils.isEmpty(url)) {
            url = shareObject == null ? "" : shareObject.url;
        }
        return url;
//        StringBuilder sb = new StringBuilder("ccsmartscreen://tvpi.coocaa.com/swaiot/index.html?");

//        //sub applet
//        StringBuilder sub = new StringBuilder(shareObject.url);
//        if(!TextUtils.isEmpty(shareObject.from)) {
//            sub.append("?applet=").append(shareObject.from);
//        }

//        sb.append("applet=").append(Uri.parse(sub.toString()));

//        return sb.toString();
    }

    private String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "共享屏";
    }
}
