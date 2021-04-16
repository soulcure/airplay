package com.coocaa.tvpi.module.local.utils;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.coocaa.tvpilib.R;

import androidx.core.content.ContextCompat;

public class LocalResourceHelper {
    private Context mContext;
    private View mRootView;
    private TextView pictureTv;
    private TextView videoTv;
    private TextView musicTv;

    private int countOfImage = 0;
    ObserverMusic observerMusic;
    ObserverImages observerImages;
    ObserverMovice observerMovice;

    private final int WHAT_PICTRUE = 0;
    private final int WHAT_MOVICE = 1;
    private final int WHAT_MUSIC = 2;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int count = msg.arg1;
            if (count > 0) {
                pictureTv.setVisibility(View.VISIBLE);
                videoTv.setVisibility(View.VISIBLE);
                musicTv.setVisibility(View.VISIBLE);
            }
            switch (msg.what) {
                case WHAT_PICTRUE:
                    countOfImage = count;
                    Intent dataIntent = new Intent();
                    dataIntent.putExtra("count", countOfImage);
                    SkyBroadcast.send(mContext, SkyBroadcast.SkyAction.COUNT_OF_LOCAL_IMAGE,
                            dataIntent);
                    pictureTv.setText(String.valueOf(count));
                    break;
                case WHAT_MOVICE:
                    videoTv.setText(String.valueOf(count));
                    break;
                case WHAT_MUSIC:
                    musicTv.setText(String.valueOf(count));
                    break;
                default:
                    break;
            }
        }
    };

    public LocalResourceHelper(Context context, View rootView) {
        super();
        mContext = context;
        mRootView = rootView;
        initView();
        initEvent();
        initData();
    }

    private void initView() {
        pictureTv = mRootView.findViewById(R.id.local_picture_num);
        videoTv = mRootView.findViewById(R.id.local_video_num);
        musicTv = mRootView.findViewById(R.id.local_music_num);
    }

    private void initEvent() {
        observerMusic = new ObserverMusic(new Handler());
        observerImages = new ObserverImages(new Handler());
        observerMovice = new ObserverMovice(new Handler());
        mContext.getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, false, observerMusic);
        mContext.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observerImages);
        mContext.getContentResolver().registerContentObserver(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, false, observerMovice);
    }

    public void initData() {
        new MediaThread(WHAT_PICTRUE).start();
        new MediaThread(WHAT_MOVICE).start();
        new MediaThread(WHAT_MUSIC).start();
    }

    public void destory() {
        if (observerMusic != null) {
            mContext.getContentResolver().unregisterContentObserver(observerMusic);
        }
        if (observerImages != null) {
            mContext.getContentResolver().unregisterContentObserver(observerImages);
        }
        if (observerMovice != null) {
            mContext.getContentResolver().unregisterContentObserver(observerMovice);
        }
        mHandler.removeCallbacksAndMessages(null);
    }


    private class MediaThread extends Thread {
        private int what;

        public MediaThread(int what) {
            this.what = what;
        }

        @Override
        public void run() {
            if (ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.d("LocalResourceHelper", "run: no permission");
                return;
            }
            int count = 0;
            switch (what) {
                case WHAT_PICTRUE:
                    count = getPictureNumbers(mContext);
                    break;
                case WHAT_MOVICE:
                    count = getMoviceNumbers(mContext);
                    break;
                case WHAT_MUSIC:
                    count = getMusicNumbers(mContext);
                    break;
                default:
                    break;
            }
            Message msg = mHandler.obtainMessage(what);
            msg.arg1 = count;
            msg.sendToTarget();
        }
    }

    /**
     * 本地照片的数量
     */
    public int getPictureNumbers(Context context) {
        String[] projection = new String[]{MediaStore.Images.Media._ID};
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        countOfImage = count;
        return count;
    }

    /**
     * 本地视频的数量
     */
    public int getMoviceNumbers(Context context) {
        String[] projection = new String[]{MediaStore.Images.Media._ID};
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
            if (cursor != null) {
                count = cursor.getCount();
                return count;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    /**
     * 本地音乐的数量
     */
    public int getMusicNumbers(Context context) {
        String[] projection = new String[]{MediaStore.Audio.Media.DURATION};
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null,
                    MediaStore.Audio.Media.DATE_ADDED + " desc");
            while (cursor.moveToNext()) {
                if (cursor.getLong(0) < 1000) {
                    continue;
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    public int getImageCount() {
        return countOfImage;
    }

    private class ObserverImages extends ContentObserver {

        public ObserverImages(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            new MediaThread(WHAT_PICTRUE).start();
        }
    }

    private class ObserverMovice extends ContentObserver {

        public ObserverMovice(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            new MediaThread(WHAT_MOVICE).start();
        }
    }

    private class ObserverMusic extends ContentObserver {

        public ObserverMusic(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            new MediaThread(WHAT_MUSIC).start();
        }
    }

}
