package com.coocaa.tvpi.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

/**
 * Created by WHY on 2018/5/8.
 */

public class ScreenShotUtil {
    /**
     * 图片的上下拼接
     * 以第一个图为准
     * 优化算法  1.图片不需要铺满，只需要以统一合适的宽度。然后让imageview自己去铺满，不然长图合成长图会崩溃，这里以第一张图为例
     *2.只缩放不相等宽度的图片。已经缩放过的不需要再次缩放
     * @param bit1
     * @param bit2
     * @return
     */
    public static Bitmap newBitmap(Bitmap bit1, Bitmap bit2) {
        Bitmap newBit = null;
        int width = bit1.getWidth();
        if (bit2.getWidth() != width) {
            int h2 = bit2.getHeight() * width / bit2.getWidth();
            newBit = Bitmap.createBitmap(width, bit1.getHeight() + h2, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBit);
            Bitmap newSizeBitmap2 = getNewSizeBitmap(bit2, width, h2);
            canvas.drawBitmap(bit1, 0, 0, null);
            canvas.drawBitmap(newSizeBitmap2, 0, bit1.getHeight(), null);
        } else {
            newBit = Bitmap.createBitmap(width, bit1.getHeight() + bit2.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBit);
            canvas.drawBitmap(bit1, 0, 0, null);
            canvas.drawBitmap(bit2, 0, bit1.getHeight(), null);
        }
        return newBit;
    }

    //右下角加水印
    public static Bitmap newBitmap2(Bitmap bit1, Bitmap bit2) {
        Bitmap newBit = null;
        try {
            int width = bit1.getWidth();
            int height = bit1.getHeight();

            newBit = Bitmap.createBitmap(bit1.getWidth(), height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBit);
            canvas.drawBitmap(bit1, 0, 0, null);
            canvas.drawBitmap(bit2, width - bit2.getWidth(), height - bit2.getHeight(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newBit;
    }

    public static Bitmap getNewSizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        float scaleWidth = ((float) newWidth) / bitmap.getWidth();
        float scaleHeight = ((float) newHeight) / bitmap.getHeight();
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap bit1Scale = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                true);
        return bit1Scale;
    }
}
