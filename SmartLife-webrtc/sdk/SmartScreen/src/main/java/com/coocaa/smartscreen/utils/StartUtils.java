package com.coocaa.smartscreen.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

/**
 * @Author: yuzhan
 */
public class StartUtils {

    public static void startLocalPicture(Context context) {
        startActivity(context, new Intent().setData(Uri.parse("np://com.coocaa.smart.localpicture/index")));
    }

    public static void startLocalVideo(Context context) {
        startActivity(context, new Intent().setData(Uri.parse("np://com.coocaa.smart.localvideo/index")));
    }

    public static void startMusic(Context context) {
        startActivity(context, new Intent().setData(Uri.parse("np://com.coocaa.smart.localmusic/index")));
    }

    public static void startDoc(Context context) {
        startActivity(context, new Intent().setData(Uri.parse("np://com.coocaa.smart.localdoc_guide/index")));
    }

    public static void startVoice(Context context) {
        startActivity(context, new Intent().setData(Uri.parse("np://com.coocaa.smart.voicecontrol/index")));
    }

    public static void startActivity(Context context, String uri) {
        if(TextUtils.isEmpty(uri))
            return ;
        startActivity(context, new Intent().setData(Uri.parse(uri)));
    }

    public static void startActivity(Context context, Intent intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            if(!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
