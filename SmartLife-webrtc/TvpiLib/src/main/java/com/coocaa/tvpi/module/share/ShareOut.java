package com.coocaa.tvpi.module.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * 分享出去
 */
public class ShareOut {

    public static void shareWeb(Context context, String url) {
        if(context == null || TextUtils.isEmpty(url))
            return ;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        if(!(context instanceof Activity)) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(shareIntent);
    }
}
