package com.coocaa.tvpi.module.local.album;

import android.net.Uri;

/**
 * desc
 */
public interface IRender {
    /**
     * publish its width and height after image loaded
     *
     * @param width
     * @param height
     */
    void onRender(int width, int height);

    /**
     * load image
     *
     * @param uri     图片Uri
     */
    void loadImage(Uri uri);
}
