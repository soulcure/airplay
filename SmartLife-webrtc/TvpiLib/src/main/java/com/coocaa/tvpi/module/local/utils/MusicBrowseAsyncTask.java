/**
 * Copyright (C) 2013 The SkyTvOS Project
 * <p>
 * Version     Date           Author
 * ─────────────────────────────────────
 * 2013-7-4         lenovo
 */

package com.coocaa.tvpi.module.local.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;


import com.coocaa.publib.data.local.AudioData;

import java.util.ArrayList;
import java.util.List;

public class MusicBrowseAsyncTask extends AsyncTask<Void, Void, List<AudioData>> {

    private Context mContext;
    private MusicBrowseCallback mMusicBrowseCallback;

    public interface MusicBrowseCallback {
        void onResult(List<AudioData> result);
    }

    public MusicBrowseAsyncTask(Context context, MusicBrowseCallback musicBrowseCallback) {
        mContext = context;
        mMusicBrowseCallback = musicBrowseCallback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<AudioData> doInBackground(Void... params) {
        List<AudioData> list = searchAudio(mContext);
        return list;
    }

    @Override
    protected void onPostExecute(List<AudioData> result) {
       if (null != mMusicBrowseCallback)
           mMusicBrowseCallback.onResult(result);
    }

    public List<AudioData> searchAudio(Context context) {
        String[] projection = new String[]{MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.ALBUM_ID};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null,
                MediaStore.Audio.Media.DATE_ADDED + " desc");

        if (cursor == null)
            return null;

        List<AudioData> list = new ArrayList<AudioData>();
        try {
            while (cursor.moveToNext()) {
                if (cursor.getLong(3) < 1000) {
                    continue;
                }
                AudioData audio = new AudioData();
                audio.id = cursor.getLong(0);
                audio.url = cursor.getString(1);
                audio.tittle = cursor.getString(2);
                audio.duration = cursor.getLong(3);
                audio.size = cursor.getLong(4);
                audio.singer = cursor.getString(5);
                audio.albumId = cursor.getInt(7);

                list.add(audio);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            return list;
        }
    }

}