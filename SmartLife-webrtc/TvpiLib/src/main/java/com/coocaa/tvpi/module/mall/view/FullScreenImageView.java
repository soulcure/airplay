package com.coocaa.tvpi.module.mall.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
//保证图片比例横屏全屏来显示图片
public class FullScreenImageView extends androidx.appcompat.widget.AppCompatImageView {
    public FullScreenImageView(Context context) {
        super(context);
    }

    public FullScreenImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullScreenImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        initSize(bm);
        super.setImageBitmap(bm);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        if(drawable instanceof BitmapDrawable){
            BitmapDrawable bitmapDrawable= (BitmapDrawable) drawable;
            Bitmap bmp=bitmapDrawable.getBitmap();
            initSize(bmp);
        }
        super.setImageDrawable(drawable);
    }
    private void initSize(Bitmap bmp){
        if(bmp!=null){
            float bmpW=bmp.getWidth();
            float bmpH=bmp.getHeight();
            if(bmpW*bmpH>0){
                float scale=bmpW/bmpH;
                    ViewGroup.LayoutParams layoutParams= getLayoutParams();
                    layoutParams.height= (int) (getWidth()/scale);
                    requestLayout();
                }
        }
    }
}
