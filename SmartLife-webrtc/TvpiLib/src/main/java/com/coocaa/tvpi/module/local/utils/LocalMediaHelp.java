package com.coocaa.tvpi.module.local.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.coocaa.publib.data.local.ImageData;
import com.coocaa.publib.data.local.LocalImageThumbnail;
import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author chenaojun
 */
public class LocalMediaHelp {

//    private static AtomicBoolean isMusicSearched = new AtomicBoolean(false);
//    private static AtomicBoolean isVideoSearched = new AtomicBoolean(false);

    public static final String MAIN_ALBUM_NAME = "所有照片";

    /**
     * 所有数据
     */
    public static List<MediaData> mMediaDataList = new ArrayList<>();

    /**
     * 所有视频
     */
    public static List<VideoData> mVideoList = new ArrayList<>();

    /**
     * 相册名
     */
    private static List<String> mAlbumNameList = new ArrayList<>();

    /**
     * 所有本地图片*
     */
    public static HashMap<String, ArrayList<ImageData>> mAllImagesMap = new HashMap<>();

    /**
     * 所有照片
     */
    public static ArrayList<ImageData> mAllImages = new ArrayList<>();

    /**
     * 缩略图*
     */
    public static HashMap<Integer, LocalImageThumbnail> mapThumbnails = new HashMap<>();

    @SuppressLint("StaticFieldLeak")
    public static Context mContext = null;

    public static Callback mCallback;


    public interface Callback {

        /**
         * 回调返回照片
         * @param imageMap 回调返回照片map
         */
        void onImageResult(HashMap<String, ArrayList<ImageData>> imageMap);

        /**
         * 回调返回视频
         * @param videoDataList 回调返回视频List
         */
        void onVideoResult(List<VideoData> videoDataList);

        /**
         * 回调返回所有数据
         * @param mediaDataList 回调返回所有数据List
         */
        void onAllResult(List<MediaData> mediaDataList);

    }

    public static void init(@NonNull Context context, Callback callback) {
        //需要刷新数据的时候调用这个方法，不需要的时候调用getXXX
        mAlbumNameList.clear();
        mAllImagesMap.clear();
        mAllImages.clear();
        mapThumbnails.clear();
        mVideoList.clear();

        mContext = context;
        mCallback = callback;
        HomeIOThread.execute(cacheDataRunnable);
    }

    public static List<String> getImageGroup() {
        return mAlbumNameList;
    }

    public static HashMap<String, ArrayList<ImageData>> getImageCacheMap() {
        return mAllImagesMap;
    }

    public static List<VideoData> getVideoList() {
        return mVideoList;
    }

    public static List<MediaData> getMediaDataList() {
        return mMediaDataList;
    }

    public static void clear() {
        mAlbumNameList.clear();
        mAllImagesMap.clear();
        mAllImages.clear();
        mapThumbnails.clear();
        mVideoList.clear();
        mContext = null;
        mCallback = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void cachePicture(Context context) {
        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_TAKEN};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                null, MediaStore.Images.Media.DATE_TAKEN + " desc");
        if (cursor == null) {
            return;
        }
        while (cursor.moveToNext()) {
            ImageData image = new ImageData();
            image.type = MediaData.TYPE.IMAGE;
            image.id = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Images.Media._ID));
            image.url = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Images.Media.DATA));
            image.tittle = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Images.Media.TITLE));
            image.bucketName = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
            image.size = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Images.Media.SIZE));
            long time = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
            image.takeTime = new Date(time);

            if (image.url != null && image.url.toLowerCase().endsWith(".pdf")) {
                continue;
            }
            if (!mAlbumNameList.contains(image.bucketName)) {
                mAllImagesMap.put(image.bucketName, new ArrayList<>());
                mAlbumNameList.add(image.bucketName);
            }
            if (mAllImagesMap.get(image.bucketName) != null) {
                mAllImagesMap.get(image.bucketName).add(image);
            }
            mAllImages.add(image);
        }
        mAllImagesMap.put(MAIN_ALBUM_NAME, mAllImages);
        mAlbumNameList.add(0,MAIN_ALBUM_NAME);
        if (cursor != null) {
            cursor.close();
        }
    }


    public static void cacheThumbnailsPicture(Context context) {
        String[] projection = new String[]{
                MediaStore.Images.Thumbnails.IMAGE_ID,
                MediaStore.Images.Thumbnails.DATA,
                MediaStore.Images.Thumbnails.KIND};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, null,
                null, null);
        if (cursor == null) {
            return;
        }
        while (cursor.moveToNext()) {
            LocalImageThumbnail image = new LocalImageThumbnail();
            image.id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Images.Thumbnails.IMAGE_ID));
            image.path = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Images.Thumbnails.DATA));
            image.kind = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Images.Thumbnails.KIND));
            if (!mapThumbnails.containsKey(image.id)) {
                mapThumbnails.put(image.id, image);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static void cacheVideo(Context context) {
        String[] projection = new String[]{MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA, MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.Media.DATE_TAKEN};

        final String[] tnumbcolumns = {
                MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID
        };
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null,
                    MediaStore.Video.Media.DATE_ADDED + " desc");
            if (cursor == null) {
                return;
            }
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
                        .Thumbnails.EXTERNAL_CONTENT_URI, tnumbcolumns, MediaStore.Video
                        .Thumbnails.VIDEO_ID + "=" + video.id, null, null);
                if (thumbCursor != null && thumbCursor.moveToFirst()) {
                    video.thumbnailPath = thumbCursor.getString(thumbCursor.getColumnIndexOrThrow
                            (MediaStore.Video.Thumbnails.DATA));
                }
                if (video.duration > 0L) {
                    mVideoList.add(video);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    private static void sortMediaList() {
        int imageIndex = 0;
        int videoIndex = 0;
        mMediaDataList.clear();
        while (imageIndex < mAllImages.size() || videoIndex < mVideoList.size()) {

            if (videoIndex == mVideoList.size()) {
                mMediaDataList.add(mAllImages.get(imageIndex));
                imageIndex++;
                continue;
            }

            if (imageIndex == mAllImages.size()) {
                mMediaDataList.add(mVideoList.get(videoIndex));
                videoIndex++;
                continue;
            }

            if (mAllImages.get(imageIndex).takeTime.after(mVideoList.get(videoIndex).takeTime)) {
                mMediaDataList.add(mAllImages.get(imageIndex));
                imageIndex++;
            } else {
                mMediaDataList.add(mVideoList.get(videoIndex));
                videoIndex++;
            }
        }
    }

    private static final Runnable cacheDataRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void run() {
            cachePicture(mContext);
            cacheThumbnailsPicture(mContext);
            cacheVideo(mContext);
//            尝试用两个线程分别进行图片与视频的搜索发现时间变长了，产生了负优化
//            HomeIOThread.execute(cachePictureRunnable);
//            HomeIOThread.execute(cacheVideoRunnable);
//            while(!isMusicSearched.get() || !isVideoSearched.get()){
//                continue;
//            }
            sortMediaList();
            mContext = null;
            HomeUIThread.execute(() -> {
                if (mCallback != null) {
                    mCallback.onImageResult(mAllImagesMap);
                    mCallback.onVideoResult(mVideoList);
                    mCallback.onAllResult(mMediaDataList);
                    mCallback = null;
                }
            });
        }
    };

//    private static final Runnable cachePictureRunnable = new Runnable() {
//        @RequiresApi(api = Build.VERSION_CODES.Q)
//        @Override
//        public void run() {
//            cachePicture(mContext);
//            cacheThumbnailsPicture(mContext);
//            isMusicSearched.set(true);
//        }
//    };
//
//    private static final Runnable cacheVideoRunnable = new Runnable() {
//        @RequiresApi(api = Build.VERSION_CODES.Q)
//        @Override
//        public void run() {
//            cacheVideo(mContext);
//            isVideoSearched.set(true);
//        }
//    };
}
