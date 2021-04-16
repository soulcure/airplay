package com.coocaa.tvpi.module.local.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.coocaa.publib.data.local.ImageData;
import com.coocaa.publib.data.local.LocalImageThumbnail;
import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.smartscreen.utils.SpUtil;
import com.coocaa.tvpi.event.LocalAlbumLoadEvent;
import com.coocaa.tvpi.module.io.HomeIOThread;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wuhaiyuan
 */
public class LocalMediaHelper {

    public static final String TAG = LocalMediaHelper.class.getSimpleName();

    public static final String MAIN_ALBUM_NAME = "所有照片";
    public static final String COLLECTION_IMAGE_ALBUM_NAME = "图片";
    public static final String COLLECTION_VIDEO_ALBUM_NAME = "视频";

    /**
     * 所有数据
     */
    public ArrayList<MediaData> mMediaDataList = new ArrayList<>();

    /**
     * 所有照片
     */
    public ArrayList<ImageData> mImageList = new ArrayList<>();

    /**
     * 所有视频
     */
    public List<VideoData> mVideoList = new ArrayList<>();

    /**
     * 相册名
     */
    private List<String> mAlbumNameList = new ArrayList<>();

    /**
     * 按照相册名存储所有本地图片视频
     */
    public ConcurrentHashMap<String, ArrayList<MediaData>> mAllMediaDataMap = new ConcurrentHashMap<>();

    /**
     * 按照时间存储所有本地图片视频
     */
    public HashMap<String, ArrayList<MediaData>> mAllMediaDataByDateMap = new HashMap<>();

    /**
     * 缩略图*
     */
    public HashMap<Integer, LocalImageThumbnail> mapThumbnails = new HashMap<>();

    private static LocalMediaHelper mInstance;
    private LocalMediaHelper() {
    }

    public static synchronized LocalMediaHelper getInstance() {
        if (null == mInstance) {
            mInstance = new LocalMediaHelper();
        }
        return mInstance;
    }

    public List<MediaData> getMediaDataList() {
        return mMediaDataList;
    }

    public List<String> getAlbumNameList() {
        return mAlbumNameList;
    }

    public ConcurrentHashMap<String, ArrayList<MediaData>> getAllMediaDataMap() {
        return mAllMediaDataMap;
    }

    private Callback mCallback;

    public interface Callback {
        /**
         * 回调返回所有数据
         * @param mediaDataList 回调返回所有数据List
         */
        void onResult(List<MediaData> mediaDataList);
    }

    /**
     * 开始缓存数据
     * @param context
     */
    public void getLocalAlbumData(Context context) {
        if (isLoading.get()) {
            return;
        }
        isLoading.set(true);
        HomeIOThread.execute(new CacheDataRunnable(context));
    }

    /**
     * 开始重新缓存数据
     * @param context
     */
    public void getReLocalAlbumData(Context context) {
        clear();
        if (isLoading.get()) {
            return;
        }
        isLoading.set(true);
        HomeIOThread.execute(new CacheDataRunnable(context));
    }

    public void clear() {
        mAllMediaDataMap.clear();
        mAlbumNameList.clear();
        mMediaDataList.clear();
        mImageList.clear();
        mVideoList.clear();
        isLoading.set(false);
    }

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    class CacheDataRunnable implements Runnable {

        private Context mContext;

        public CacheDataRunnable(Context context) {
            mContext = context;
        }

        @Override
        public void run() {

            if (null != mMediaDataList && !mMediaDataList.isEmpty()) {
                EventBus.getDefault().post(new LocalAlbumLoadEvent());
                isLoading.set(false);
                return;
            }

            cachePicture(mContext);
            cacheVideo(mContext);

            sortMediaList();

            EventBus.getDefault().post(new LocalAlbumLoadEvent());

//            HomeUIThread.execute(() -> {
//                if (mCallback != null) {
//                    mCallback.onResult(mMediaDataList);
//                    mCallback = null;
//                }
//            });

            isLoading.set(false);
        }
    };

    //缓存照片
    private void cachePicture(Context context) {
        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                /*MediaStore.Images.Media.DATE_TAKEN*/};
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
//            long time = cursor.getLong(cursor
//                    .getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
//            image.takeTime = new Date(time);
            //修复部分图片时间不准的问题
            File file = new File(image.url);
            image.takeTime = new Date(file.lastModified());

            if (image.url != null && image.url.toLowerCase().endsWith(".pdf")) {
                continue;
            }

            if(image.bucketName == null)
                continue;
            //新建相册
            if (!mAlbumNameList.contains(image.bucketName)) {
                Log.d(TAG, "cacheVideo: video.bucketName = " + image.bucketName);
                mAllMediaDataMap.put(image.bucketName, new ArrayList<>());
                mAlbumNameList.add(image.bucketName);
            }
            //添加到同名相册
            if (mAllMediaDataMap.get(image.bucketName) != null) {
                mAllMediaDataMap.get(image.bucketName).add(image);
            }
            mImageList.add(image);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    //缓存视频
    private void cacheVideo(Context context) {
        String[] projection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.Media.DATE_TAKEN};

        final String[] thumbcolumns = {
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
                video.bucketName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                video.tittle = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                video.duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                video.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                video.resolution = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION));
                long time = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
                video.takeTime = new Date(time);
                Cursor thumbCursor = context.getContentResolver().query(MediaStore.Video
                        .Thumbnails.EXTERNAL_CONTENT_URI, thumbcolumns, MediaStore.Video
                        .Thumbnails.VIDEO_ID + "=" + video.id, null, null);
                if (thumbCursor != null && thumbCursor.moveToFirst()) {
                    video.thumbnailPath = thumbCursor.getString(thumbCursor.getColumnIndexOrThrow
                            (MediaStore.Video.Thumbnails.DATA));
                }
                if (video.duration > 0L) {
                    //新建相册
                    if(video.bucketName == null)
                        continue;
                    if (!mAlbumNameList.contains(video.bucketName)) {
                        Log.d(TAG, "cacheVideo: video.bucketName = " + video.bucketName);
                        mAllMediaDataMap.put(video.bucketName, new ArrayList<>());
                        mAlbumNameList.add(video.bucketName);
                    }
                    //添加到同名相册
                    if (mAllMediaDataMap.get(video.bucketName) != null) {
                        mAllMediaDataMap.get(video.bucketName).add(video);
                    }
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

    private synchronized void sortMediaList() {
        if (mImageList.size() > 0) {
            mMediaDataList.addAll(mImageList);
        }
        if (mVideoList.size() > 0) {
            mMediaDataList.addAll(mVideoList);
        }
        //过滤空数据，修复友盟报错
        mMediaDataList = filterNull(mMediaDataList);
        //给所有数据排序
        Collections.sort(mMediaDataList);

        mAllMediaDataMap.put(MAIN_ALBUM_NAME, mMediaDataList);
        mAlbumNameList.add(0, MAIN_ALBUM_NAME);
    }

    public ArrayList<MediaData> filterNull(ArrayList<MediaData> list) {
        ArrayList<MediaData> filterList = new ArrayList<MediaData>();
        for (MediaData mediaData : list) {
            if (mediaData != null) {
                filterList.add(mediaData);
            }
        }
        return filterList;
    }

    private boolean containsSameDateAlbum(List<Date> dateList, Date date) {
        if(null != dateList) {
            for (Date dateTemp :
                    dateList) {
                if (dateTemp.toString().equals(date.toString())) {
                    Log.d(TAG, "hasSameDateAlbum: 有同一天的数据");
                    return true;
                }
            }
        }
        return false;
    }

    private static final String KEY_COLLECTED_MEDIA_DATA = "KEY_COLLECTED_MEDIA_DATA";
    private static final String KEY_COLLECTED_MEDIA_DATA_IMAGE = "KEY_COLLECTED_MEDIA_DATA_IMAGE";
    private static final String KEY_COLLECTED_MEDIA_DATA_VIDEO = "KEY_COLLECTED_MEDIA_DATA_VIDEO";

    /**
     * 获取MediaData
     * @param context
     * @return
     */
    public List<MediaData> getCollectedMediaData(Context context) {
        List<MediaData> c = SpUtil.getObject(context, KEY_COLLECTED_MEDIA_DATA);
        if(c != null) {
            return new CopyOnWriteArrayList<>(c);
        }
        return null;
    }

    public List<MediaData> getCollectedMediaData_Image(Context context) {
        List<MediaData> c = SpUtil.getObject(context, KEY_COLLECTED_MEDIA_DATA_IMAGE);
        if(c != null) {
            return new CopyOnWriteArrayList<>(c);
        }
        return null;
    }

    public List<MediaData> getCollectedMediaData_Video(Context context) {
        List<MediaData> c = SpUtil.getObject(context, KEY_COLLECTED_MEDIA_DATA_VIDEO);
        if(c != null) {
            return new CopyOnWriteArrayList<>(c);
        }
        return null;
    }

    /**
     * 收藏MediaData
     * @param context
     * @param mediaDataList
     */
    public void collectMediaData(Context context, List<MediaData> mediaDataList) {
        if(mediaDataList == null)return;
        for (MediaData mediaData : mediaDataList) {
            collectMediaData(context,mediaData);
        }
    }

    /**
     * 收藏MediaData
     * @param context
     * @param mediaData
     */
    public void collectMediaData(Context context, MediaData mediaData) {
        if (mediaData == null)
            return;
        if(mediaData instanceof ImageData) {
            List<MediaData> collectedMediaDataList = getCollectedMediaData_Image(context);
            if (null == collectedMediaDataList) {
                collectedMediaDataList = new ArrayList<>();
            }
            //遍历列表如果有相同的移动到第一个
            if (collectedMediaDataList.size() > 0) {
                for (MediaData collectedMediaData : collectedMediaDataList) {
                    if (collectedMediaData.url.equals(mediaData.url)) {
                        collectedMediaDataList.set(0, collectedMediaData);
                        return;
                    }
                }
            }
            collectedMediaDataList.add(0, mediaData);
            Log.d("heni", "collectMediaData: put image....");
            SpUtil.putList(context, KEY_COLLECTED_MEDIA_DATA_IMAGE, collectedMediaDataList);
        }else if(mediaData instanceof MediaData){
            List<MediaData> collectedMediaDataList = getCollectedMediaData_Video(context);
            if (null == collectedMediaDataList) {
                collectedMediaDataList = new ArrayList<>();
            }
            //遍历列表如果有相同的移动到第一个
            if (collectedMediaDataList.size() > 0) {
                for (MediaData collectedMediaData : collectedMediaDataList) {
                    if (collectedMediaData.url.equals(mediaData.url)) {
                        collectedMediaDataList.set(0, collectedMediaData);
                        return;
                    }
                }
            }
            collectedMediaDataList.add(0, mediaData);
            Log.d("heni", "collectMediaData: put video....");
            SpUtil.putList(context, KEY_COLLECTED_MEDIA_DATA_VIDEO, collectedMediaDataList);
        }

        List<MediaData> collectedMediaDataList = getCollectedMediaData(context);
        if (null == collectedMediaDataList) {
            collectedMediaDataList = new ArrayList<>();
        }
        //遍历列表如果有相同的移动到第一个
        if (collectedMediaDataList.size() > 0) {
            for (MediaData collectedMediaData :
                    collectedMediaDataList) {
                if (collectedMediaData.url.equals(mediaData.url)) {
                    collectedMediaDataList.set(0, collectedMediaData);
                    return;
                }
            }
        }
        collectedMediaDataList.add(0, mediaData);
        Log.d("heni", "collectMediaData: put all....");
        SpUtil.putList(context, KEY_COLLECTED_MEDIA_DATA, collectedMediaDataList);
    }

    public void removeMediaData(Context context, MediaData mediaData) {
        if(mediaData == null)
            return;
        if (mediaData instanceof ImageData) {
            List<MediaData> collectedMediaDataList = getCollectedMediaData_Image(context);
            if (null != collectedMediaDataList && collectedMediaDataList.size() > 0) {
                for (MediaData collectedMediaData : collectedMediaDataList) {
                    if (collectedMediaData.url.equals(mediaData.url)) {
                        collectedMediaDataList.remove(collectedMediaData);
                        SpUtil.putList(context, KEY_COLLECTED_MEDIA_DATA_IMAGE, collectedMediaDataList);
                    }
                }
            }
        } else if (mediaData instanceof VideoData) {
            List<MediaData> collectedMediaDataList = getCollectedMediaData_Video(context);
            if (null != collectedMediaDataList && collectedMediaDataList.size() > 0) {
                for (MediaData collectedMediaData : collectedMediaDataList) {
                    if (collectedMediaData.url.equals(mediaData.url)) {
                        collectedMediaDataList.remove(collectedMediaData);
                        SpUtil.putList(context, KEY_COLLECTED_MEDIA_DATA_VIDEO, collectedMediaDataList);
                    }
                }
            }
        }

        List<MediaData> collectedMediaDataList = getCollectedMediaData(context);
        if (null != collectedMediaDataList && collectedMediaDataList.size() > 0) {
            if (collectedMediaDataList.size() > 0) {
                for (MediaData collectedMediaData : collectedMediaDataList) {
                    if (collectedMediaData.url.equals(mediaData.url)) {
                        collectedMediaDataList.remove(collectedMediaData);
                        SpUtil.putList(context, KEY_COLLECTED_MEDIA_DATA, collectedMediaDataList);
                        return;
                    }
                }
            }
        }
    }

    public void removeMediaData(Context context, List<MediaData> mediaDataList) {
        if (mediaDataList == null) return;
        for (MediaData mediaData : mediaDataList) {
            removeMediaData(context, mediaData);
        }
    }

    public boolean hasCollected(Context context, MediaData mediaData) {
        boolean hasCollected = false;
        List<MediaData> mediaDataList = getCollectedMediaData(context);
        if (null != mediaDataList && mediaDataList.size() > 0) {
            for (MediaData data :
                    mediaDataList) {
                if (data.url .equals(mediaData.url)) {
                    hasCollected = true;
                    break;
                }
            }
        }
        return hasCollected;
    }

}