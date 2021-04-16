package com.coocaa.tvpi.module.share.intent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.tvpi.module.local.media.LocalMediaActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: yuzhan
 */
public class VideoIntent {

    private final static String TAG = "IntentActivity";

    public static boolean handleVideoIntent(Context context, Intent _intent, Uri uri, boolean isMulple) {
        try {
            List<VideoData> videoDataList = null;
            if(isMulple) {
                ArrayList<Uri> arrayList = _intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if(arrayList != null) {
                    videoDataList = new ArrayList<>(arrayList.size());
                }
                String tempRealPath = null;
                for(Uri u : arrayList) {
                    VideoData videoData = new VideoData();
                    tempRealPath = getRealPath((Activity) context, u);
                    Log.d(TAG, "img uri : " + videoData.url);
                    Log.d(TAG, "img real path : " + tempRealPath);
                    videoData.url = tempRealPath == null ? u.toString() : tempRealPath;
                    videoData.thumbnailPath = videoData.url;
                    videoData.bucketName = "分享";
                    videoData.type = MediaData.TYPE.VIDEO;
                    videoData.duration = getVideoDuration(videoData.url);
                    videoDataList.add(videoData);
                }
            } else {
                videoDataList = new ArrayList<>(1);
                Uri imgUri = _intent.getParcelableExtra(Intent.EXTRA_STREAM);
                String tempRealPath = getRealPath(context , imgUri);
                Log.d(TAG, "img uri : " + imgUri);
                Log.d(TAG, "img real path : " + tempRealPath);
                VideoData videoData = new VideoData();
                videoData.url = tempRealPath == null ? imgUri.toString() : tempRealPath;
                videoData.thumbnailPath = videoData.url;
                videoData.bucketName = "分享";
                videoData.type = MediaData.TYPE.VIDEO;
                videoData.duration = getVideoDuration(videoData.url);
                videoDataList.add(videoData);
            }
            if(videoDataList != null && !videoDataList.isEmpty()) {
                return startVideoActivity(context, videoDataList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean startVideoActivity(Context context, List<VideoData> videoDataList) throws Exception{
        /*Intent intent = new Intent(context, VideoPreviewActivity2.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("fromShare", true);
        bundle.putParcelable("VIDEODATA", videoDataList.get(0)); //暂时只支持一个
        intent.putExtras(bundle);
        if(! (context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("np");
        builder.authority("com.coocaa.smart.localvideo");
        builder.path("preview");
        String uri = builder.build().toString();
        bundle.putString("applet", uri);
        intent.putExtra("swaiotos.applet", bundle);

        context.startActivity(intent);*/

        Intent intent = new Intent(context, LocalMediaActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("fromShare", true);
        bundle.putParcelableArrayList("VIDEODATA", (ArrayList<? extends Parcelable>) videoDataList);
        bundle.putString("type", "video");
        intent.putExtras(bundle);

        if(! (context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
        return true;
    }

    //需要投到远程，所以需要获取真实图片地址
    private static String getRealPath(Context context, Uri contentUri) {
        if(! (context instanceof Activity))
            return null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = ((Activity) context).managedQuery(contentUri, proj, // Which columns to return
                null, // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        if (cursor==null) {
            return contentUri.getPath();
        }

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);

    }

    //获取视频总时长
    private static int getVideoDuration(String path){
        int duration = 0;
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(path);
            duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            Log.d(TAG, "getVideoDuration error : " + e.toString());
            e.printStackTrace();
        }
        Log.d(TAG, "getVideoDuration path=" + path + ", duration=" + duration);
        return duration;
    }
}
