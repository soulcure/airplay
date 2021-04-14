package com.threesoft.webrtc.webrtcroom.activity;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.threesoft.webrtc.webrtcroom.R;

import java.util.ArrayList;
import java.util.List;


public class LocalVideoListActivity extends AppCompatActivity {
    private static final String TAG = LocalVideoListActivity.class.getSimpleName();

    private List<VideoInfo> mVideoInfos;
    private static final String[] sLocalVideoColumns = {
            MediaStore.Video.Media._ID, // 视频id
            MediaStore.Video.Media.DATA, // 视频路径
            MediaStore.Video.Media.SIZE, // 视频字节大小
            MediaStore.Video.Media.DISPLAY_NAME, // 视频名称 xxx.mp4
            MediaStore.Video.Media.TITLE, // 视频标题
            MediaStore.Video.Media.DATE_ADDED, // 视频添加到MediaProvider的时间
            MediaStore.Video.Media.DATE_MODIFIED, // 上次修改时间，该列用于内部MediaScanner扫描，外部不要修改
            MediaStore.Video.Media.MIME_TYPE, // 视频类型 video/mp4
            MediaStore.Video.Media.DURATION, // 视频时长
            MediaStore.Video.Media.ARTIST, // 艺人名称
            MediaStore.Video.Media.ALBUM, // 艺人专辑名称
            MediaStore.Video.Media.RESOLUTION, // 视频分辨率 X x Y格式
            MediaStore.Video.Media.DESCRIPTION, // 视频描述
            MediaStore.Video.Media.IS_PRIVATE,
            MediaStore.Video.Media.TAGS,
            MediaStore.Video.Media.CATEGORY, // YouTube类别
            MediaStore.Video.Media.LANGUAGE, // 视频使用语言
            MediaStore.Video.Media.LATITUDE, // 拍下该视频时的纬度
            MediaStore.Video.Media.LONGITUDE, // 拍下该视频时的经度
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.MINI_THUMB_MAGIC,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.BOOKMARK // 上次视频播放的位置
    };
    private static final String[] sLocalVideoThumbnailColumns = {
            MediaStore.Video.Thumbnails.DATA, // 视频缩略图路径
            MediaStore.Video.Thumbnails.VIDEO_ID, // 视频id
            MediaStore.Video.Thumbnails.KIND,
            MediaStore.Video.Thumbnails.WIDTH, // 视频缩略图宽度
            MediaStore.Video.Thumbnails.HEIGHT // 视频缩略图高度
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        initVideoData();

        ListView lvLocalVideoList = findViewById(R.id.lv_local_video_list);
        lvLocalVideoList.setAdapter(new VideoAdapter(this, mVideoInfos));
        lvLocalVideoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                VideoInfo video = mVideoInfos.get(i);
                Log.d(TAG,"video:"+video);
                Intent intent = getIntent();
                intent.putExtra("path",video.data);
                intent.putExtra("width",video.width+"");
                intent.putExtra("height",video.height+"");
                setResult(RESULT_OK,intent);
                finish();

            }
        });
    }

    private static class VideoAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<VideoInfo> mVideoInfos;

        VideoAdapter(Context context, List<VideoInfo> videoInfos) {
            this.mVideoInfos = videoInfos;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mVideoInfos.size();
        }

        @Override
        public VideoInfo getItem(int position) {
            return mVideoInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            VideoInfoHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.local_video_list_item, parent, false);
                holder = new VideoInfoHolder();
                holder.ivData = convertView.findViewById(R.id.iv_data);
                holder.tvArtist = convertView.findViewById(R.id.tv_artist);
                holder.tvAlbum = convertView.findViewById(R.id.tv_album);
                convertView.setTag(holder);
            } else {
                holder = (VideoInfoHolder) convertView.getTag();
            }

            VideoInfo videoInfo = getItem(position);
            holder.ivData.setImageBitmap(BitmapFactory.decodeFile(videoInfo.thumbnailData));
            holder.tvArtist.setText(videoInfo.artist);
            holder.tvAlbum.setText(videoInfo.album);

            return convertView;
        }

        private static final class VideoInfoHolder {
            ImageView ivData;
            TextView tvArtist;
            TextView tvAlbum;
        }
    }

    private void initVideoData() {
        mVideoInfos = new ArrayList<>();

        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, sLocalVideoColumns,
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                VideoInfo videoInfo = new VideoInfo();

                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                long dateAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                long dateModified = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED));
                String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));
                long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ALBUM));
                String resolution = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION));
                String description = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DESCRIPTION));
                int isPrivate = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.IS_PRIVATE));
                String tags = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TAGS));
                String category = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.CATEGORY));
                double latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Video.Media.LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Video.Media.LONGITUDE));
                int dateTaken = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN));
                int miniThumbMagic = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.MINI_THUMB_MAGIC));
                String bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID));
                String bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                int bookmark = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.BOOKMARK));

                Cursor thumbnailCursor = getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, sLocalVideoThumbnailColumns,
                        MediaStore.Video.Thumbnails.VIDEO_ID + "=" + id, null, null);
                if (thumbnailCursor != null && thumbnailCursor.moveToFirst()) {
                    do {
                        String thumbnailData = thumbnailCursor.getString(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                        int kind = thumbnailCursor.getInt(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.KIND));
                        long width = thumbnailCursor.getLong(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.WIDTH));
                        long height = thumbnailCursor.getLong(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.HEIGHT));

                        videoInfo.thumbnailData = thumbnailData;
                        videoInfo.kind = kind;
                        videoInfo.width = width;
                        videoInfo.height = height;
                    } while (thumbnailCursor.moveToNext());

                    thumbnailCursor.close();
                }

                videoInfo.id = id;
                videoInfo.data = data;
                videoInfo.size = size;
                videoInfo.displayName = displayName;
                videoInfo.title = title;
                videoInfo.dateAdded = dateAdded;
                videoInfo.dateModified = dateModified;
                videoInfo.mimeType = mimeType;
                videoInfo.duration = duration;
                videoInfo.artist = artist;
                videoInfo.album = album;
                videoInfo.resolution = resolution;
                videoInfo.description = description;
                videoInfo.isPrivate = isPrivate;
                videoInfo.tags = tags;
                videoInfo.category = category;
                videoInfo.latitude = latitude;
                videoInfo.longitude = longitude;
                videoInfo.dateTaken = dateTaken;
                videoInfo.miniThumbMagic = miniThumbMagic;
                videoInfo.bucketId = bucketId;
                videoInfo.bucketDisplayName = bucketDisplayName;
                videoInfo.bookmark = bookmark;

                Log.v(TAG, "videoInfo = " + videoInfo.toString());

                mVideoInfos.add(videoInfo);
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    private static final class VideoInfo   {
        private int id;
        private String data;
        private long size;
        private String displayName;
        private String title;
        private long dateAdded;
        private long dateModified;
        private String mimeType;
        private long duration;
        private String artist;
        private String album;
        private String resolution;
        private String description;
        private int isPrivate;
        private String tags;
        private String category;
        private double latitude;
        private double longitude;
        private int dateTaken;
        private int miniThumbMagic;
        private String bucketId;
        private String bucketDisplayName;
        private int bookmark;

        private String thumbnailData;
        private int kind;
        private long width;
        private long height;

        @Override
        public String toString() {
            return "VideoInfo{" +
                    "id=" + id +
                    ", data='" + data + '\'' +
                    ", size=" + size +
                    ", displayName='" + displayName + '\'' +
                    ", title='" + title + '\'' +
                    ", dateAdded=" + dateAdded +
                    ", dateModified=" + dateModified +
                    ", mimeType='" + mimeType + '\'' +
                    ", duration=" + duration +
                    ", artist='" + artist + '\'' +
                    ", album='" + album + '\'' +
                    ", resolution='" + resolution + '\'' +
                    ", description='" + description + '\'' +
                    ", isPrivate=" + isPrivate +
                    ", tags='" + tags + '\'' +
                    ", category='" + category + '\'' +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", dateTaken=" + dateTaken +
                    ", miniThumbMagic=" + miniThumbMagic +
                    ", bucketId='" + bucketId + '\'' +
                    ", bucketDisplayName='" + bucketDisplayName + '\'' +
                    ", bookmark=" + bookmark +
                    ", thumbnailData='" + thumbnailData + '\'' +
                    ", kind=" + kind +
                    ", width=" + width +
                    ", height=" + height +
                    '}';
        }
    }
}

