package com.coocaa.tvpi.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;


/**
 * @ClassName RotateTransformation
 * @Description TODO (write something)
 * @User heni
 * @Date 18-8-4
 */
public class RotateTransformation extends BitmapTransformation {


    //旋转默认0
    private float rotateRotationAngle = 0f;

    public RotateTransformation(Context context , float rotateRotationAngle)
    {
        this.rotateRotationAngle = rotateRotationAngle ;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        Matrix matrix = new Matrix();
        //旋转
        matrix.postRotate(rotateRotationAngle);
        //生成新的Bitmap
        return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);

        //return null;
    }

    public String getId() {
        return rotateRotationAngle+"";
    }

    /**
     * Adds all uniquely identifying information to the given digest.
     * <p>
     * <p> Note - Using {@link MessageDigest#reset()} inside of this method will result
     * in undefined behavior. </p>
     *
     * @param messageDigest
     */
    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {

    }
}
