package swaiotos.share.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.Map;

import swaiotos.share.api.define.ShareObject;

/**
 * @Author: yuzhan
 */
public class ShareApi {

    public static void share(Context context, ShareObject shareObject) {
        share(context, shareObject, null);
    }

    public static void share(Context context, Map<String, String> params) {
        share(context, null, params);
    }

    public static void share(Context context, ShareObject shareObject, Map<String, String> params) {
        if(context == null) {
            return ;
        }
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("np").authority("com.coocaa.smart.share").path("index").appendQueryParameter("type", "web");
        if(shareObject != null) {
            if(!TextUtils.isEmpty(shareObject.from)) {
                builder.appendQueryParameter("from", shareObject.from);
            }
            if(!TextUtils.isEmpty(shareObject.thumb)) {
                builder.appendQueryParameter("thumb", shareObject.thumb);
            }
            if(!TextUtils.isEmpty(shareObject.text)) {
                builder.appendQueryParameter("text", shareObject.text);
            }
            if(!TextUtils.isEmpty(shareObject.title)) {
                builder.appendQueryParameter("title", shareObject.title);
            }
            if(!TextUtils.isEmpty(shareObject.description)) {
                builder.appendQueryParameter("description", shareObject.description);
            }
            if(!TextUtils.isEmpty(shareObject.version)) {
                builder.appendQueryParameter("version", shareObject.version);
            }
        }
        if(params != null) {
            appendParams(builder, params);
        }
        Intent intent = new Intent();
        intent.setData(builder.build());
        intent.setPackage(context.getPackageName());
        if(!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "该页面暂不支持分享", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private static void appendParams(Uri.Builder builder, Map<String, String> params) {
        if(builder == null || params == null)
            return ;
        if(!TextUtils.isEmpty(params.get("from"))) {
            builder.appendQueryParameter("from", params.get("from"));
        }
        if(!TextUtils.isEmpty(params.get("thumb"))) {
            builder.appendQueryParameter("thumb", params.get("thumb"));
        }
        if(!TextUtils.isEmpty(params.get("text"))) {
            builder.appendQueryParameter("text", params.get("text"));
        }
        if(!TextUtils.isEmpty(params.get("title"))) {
            builder.appendQueryParameter("title", params.get("title"));
        }
        if(!TextUtils.isEmpty(params.get("description"))) {
            builder.appendQueryParameter("description", params.get("description"));
        }
        if(!TextUtils.isEmpty(params.get("version"))) {
            builder.appendQueryParameter("version", params.get("version"));
        }
    }
}
