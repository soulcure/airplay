package com.coocaa.tvpi.module.local.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.coocaa.publib.data.local.ImageData;
import com.coocaa.publib.data.local.LocalImageBean;
import com.coocaa.publib.data.local.LocalImageThumbnail;
import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.utils.SpUtil;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dvlee on 3/19/15.
 */
public class MediaStoreHelper {

    public static final String MAIN_ALBUM_NAME = "所有照片";
//    private static final String SECOND_ALBUM_NAME = "Camera";

    // 相册名
    private static List<String> mAlbumNameList = new ArrayList<String>();

    /**
     * 本地
     **/
    public static final String ALBUM_NAME_LAST_OPEN = "ALBUM_NAME_LAST_OPEN";

    // 所有本地图片
    public static HashMap<String, ArrayList<ImageData>> mAllImagesMap =
            new HashMap<String, ArrayList<ImageData>>();
    public static ArrayList<ImageData> mAllImages = new ArrayList<>();
    // 缩略图
    public static HashMap<Integer, LocalImageThumbnail> mapThumbnails =
            new HashMap<Integer, LocalImageThumbnail>();

    public static List<String> getImageGroup() {
        return mAlbumNameList;
    }

    public static HashMap<String, ArrayList<ImageData>> getImageCacheMap() {
        return mAllImagesMap;
    }

    public static void init(Context context) {
//        if(mAllImagesMap.size() != 0 || mapThumbnails.size() != 0)
//            return;
        mAlbumNameList.clear();
        mAllImagesMap.clear();
        mAllImages.clear();
        mapThumbnails.clear();
        cachePicture(context);
        cacheThumbnailsPicture(context);
    }

    /**
     * 缓存sd卡里所有的照片信息
     *
     * @param context
     * @return
     */
    public static HashMap<String, List<LocalImageBean>> cachePicture(Context context) {
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

        if (cursor == null)
            return null;
        HashMap<String, List<LocalImageBean>> monthMap = new HashMap<String, List<LocalImageBean>>();
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

            //image.thumb = getImageThumbnailPath(context,image.id,image.data);
            // 过滤非图片数据
            if (image.url != null && image.url.toLowerCase().endsWith(".pdf")) {
                continue;
            }

            if (!mAlbumNameList.contains(image.bucketName)) {
                mAllImagesMap.put(image.bucketName, new ArrayList<ImageData>());
                mAlbumNameList.add(image.bucketName);
            }

            //Log.i("dvlee", image.thumb + "\n" + image.id);

            mAllImagesMap.get(image.bucketName).add(image);
            mAllImages.add(image);

        }
        mAllImagesMap.put(MAIN_ALBUM_NAME, mAllImages);
        mAlbumNameList.add(MAIN_ALBUM_NAME);
        if (cursor != null) {
            cursor.close();
        }

        final String lastOpenAlbum = SpUtil.getString(context, ALBUM_NAME_LAST_OPEN);
        // 排序，把数量多的相册排在前面
      /*  Collections.sort(mAlbumNameList, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                // MAIN_ALBUM_NAME 优先，SECOND_ALBUM_NAME 其次
                if (lhs.equals(lastOpenAlbum)) {
                    return -1;
                }
                if (rhs.equals(lastOpenAlbum)) {
                    return 1;
                }
                if (lhs.equals(MAIN_ALBUM_NAME)) {
                    return -1;
                }
                if (rhs.equals(MAIN_ALBUM_NAME)) {
                    return 1;
                }
                *//*if (lhs.equals(SECOND_ALBUM_NAME)) {
                    return -1;
                }
                if (rhs.equals(SECOND_ALBUM_NAME)) {
                    return 1;
                }*//*

                int lCount = mAllImagesMap.get(lhs).size();
                int rCount = mAllImagesMap.get(rhs).size();
                return rCount - lCount;
            }
        });*/

        return monthMap;
    }


    public static HashMap<Integer, LocalImageThumbnail> cacheThumbnailsPicture(Context context) {
        String[] projection = new String[]{
                MediaStore.Images.Thumbnails.IMAGE_ID,
                MediaStore.Images.Thumbnails.DATA,
                MediaStore.Images.Thumbnails.KIND};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, null,
                null, null);

        if (cursor == null)
            return null;
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

            //Log.i("dvlee", image.thumb + "\n" + image.id);
        }
        if (cursor != null) {
            cursor.close();
        }
        return mapThumbnails;
    }


    /**
     * @param context
     * @param id
     * @param path，若获取不到缩略图，返回该path
     * @return
     */
    public static String getImageThumbnailPath(Context context, int id, String path) {
        final String thumb_DATA = MediaStore.Images.Thumbnails.DATA;
        final String thumb_IMAGE_ID = MediaStore.Images.Thumbnails.IMAGE_ID;
        Uri uri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
        String[] projection = {thumb_DATA, thumb_IMAGE_ID};
        String selection = thumb_IMAGE_ID + "=" + id + " AND " + MediaStore.Images.Thumbnails
                .KIND + "=" + MediaStore.Images.Thumbnails.MINI_KIND;
        Cursor thumbCursor = context.getContentResolver().query(uri, projection, selection,
                null, null);

        String thumbPath = null;
        Bitmap thumbBitmap = null;
        if (thumbCursor != null && thumbCursor.getCount() > 0) {
            thumbCursor.moveToFirst();
            int thCulumnIndex = thumbCursor.getColumnIndex(thumb_DATA);

            thumbPath = thumbCursor.getString(thCulumnIndex);
            //  thumbBitmap = BitmapFactory.decodeFile(thumbPath);
        }
        if (TextUtils.isEmpty(thumbPath)) {
            thumbPath = path;
        }
        return thumbPath;
    }


    public static String getThumbnailPath(ImageData bean) {
        int id = (int) bean.id;
        LocalImageThumbnail thumbnail = mapThumbnails.get(id);
        if (thumbnail == null) {
            return bean.url;
        }
        return thumbnail.path;

    }

    public static String getThumbnailPath(long id) {
        LocalImageThumbnail thumbnail = mapThumbnails.get(id);
        if (thumbnail == null) {
            return null;
        }
        return thumbnail.path;
    }
}
