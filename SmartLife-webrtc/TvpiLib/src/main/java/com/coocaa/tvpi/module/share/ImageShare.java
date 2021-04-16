package com.coocaa.tvpi.module.share;

import android.app.Activity;
import android.text.TextUtils;

import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;

import swaiotos.share.api.define.ShareObject;

/**
 * @Author: yuzhan
 */
public class ImageShare extends MyShare {
    public ImageShare(ShareObject shareObject) {
        super(shareObject);
    }

    @Override
    public void share(Activity activity, SHARE_MEDIA shareMedia) {
        UMImage image = null;
        if(thumbBitmap != null) {
            image = new UMImage(activity, thumbBitmap);
        } else if(!TextUtils.isEmpty(shareObject.thumb)) {
            image = new UMImage(activity, shareObject.thumb);
        } else if(thumbResId != 0) {
            image = new UMImage(activity, thumbResId);
        }
        new ShareAction(activity)
                .setPlatform(shareMedia)//传入平台
                .withText(shareObject.text)//分享内容
                .withMedia(image)
                .setCallback(shareListener)//回调监听器
                .share();

        //        以下注释这些都是在加水印做处理，不需要水印的话，只写一句bitmap转成UMImage，分享即可
//        Bitmap bitmapScreen = screenBitmap;
//        boolean watermark = SpUtil.getBoolean(this, SpUtil.Keys.SCREENSHOT_WATERMARK, true);
//        Bitmap bitmapQR = null;
//        if (watermark) {
//            bitmapQR = BitmapFactory.decodeResource(getResources(), R.drawable.bg_tvpi_watermark);//新的水印
//        }
//        Bitmap bitmap = ScreenShotUtil.newBitmap2(bitmapScreen, bitmapQR);
//        Bitmap bitmapThumb = ScreenShotUtil.newBitmap2(bitmapScreen, bitmapQR);
//        bitmapThumb = ScreenShotUtil.getNewSizeBitmap(bitmapThumb, bitmapThumb.getWidth() / 10,
//                bitmapThumb.getHeight() / 10);
//        UMImage image = new UMImage(this, bitmap);
//        UMImage thumb = new UMImage(this, bitmapThumb);
//        image.setThumb(thumb);
//        Log.d(TAG, "onUmengShare: finish compress");
    }
}
