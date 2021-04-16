/**
 * Copyright (C) 2012 The SkyTvOS Project
 * <p>
 * Version     Date           Author
 * ─────────────────────────────────────
 * 2013-7-4         lenovo
 */

package com.coocaa.tvpi.module.local.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.data.local.VideoData;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class VideoBrowseAsyncTask extends AsyncTask<Void, Void, List<VideoData>> {

    private String TAG = VideoBrowseAsyncTask.class.getSimpleName();
    private final Object lock = new Object();

    private Context mContext;
    private VideoBrowseCallback mVideoBrowseCallback;

    public interface VideoBrowseCallback {
        void onResult(List<VideoData> result);
    }

    public VideoBrowseAsyncTask(Context context, VideoBrowseCallback videoBrowseCallback) {
        mContext = context;
        mVideoBrowseCallback = videoBrowseCallback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<VideoData> doInBackground(Void... params) {
        List<VideoData> list = searchVideo(mContext);
        return list;
    }

    @Override
    protected void onPostExecute(List<VideoData> result) {
        if (null != mVideoBrowseCallback)
            mVideoBrowseCallback.onResult(result);
    }

    public List<VideoData> searchVideo(Context context) {
        String[] projection = new String[]{MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA, MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.Media.DATE_TAKEN};

        final String[] TNUMB_COLUMNS = {
                MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID
        };

        Cursor cursor = null;
        List<VideoData> list = new ArrayList<VideoData>();

        synchronized (lock) {
            try {
                cursor = context.getContentResolver().query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null,
                        MediaStore.Video.Media.DATE_ADDED + " desc");
                if (cursor == null)
                    return null;

                while (cursor.moveToNext()) {
                    VideoData video = new VideoData();
                    video.type = MediaData.TYPE.VIDEO;
                    video.id = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                    video.url = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                    video.tittle = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                    video.duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                    video.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                    video.resolution = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION));
                    long time = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
                    video.takeTime = new Date(time);

                    Cursor thumbCursor = context.getContentResolver().query(MediaStore.Video
                            .Thumbnails.EXTERNAL_CONTENT_URI, TNUMB_COLUMNS, MediaStore.Video
                            .Thumbnails.VIDEO_ID + "=" + video.id, null, null);
                    if (thumbCursor != null && thumbCursor.moveToFirst()) {
                        video.thumbnailPath = thumbCursor.getString(thumbCursor.getColumnIndexOrThrow
                                (MediaStore.Video.Thumbnails.DATA));
                    }

             /*   BitmapFactory.Options options = new BitmapFactory.Options();
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                try {
                    video.thumbnail = MediaStore.Video.Thumbnails.getThumbnail(mContext.getContentResolver(),
                            video.id, MediaStore.Images.Thumbnails.MICRO_KIND, options);
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                    if(video.duration > 0L) {
                        list.add(video);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) cursor.close();
            }
        }

        return list;
    }

}
