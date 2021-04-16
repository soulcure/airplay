package com.coocaa.tvpi.module.local

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import com.coocaa.publib.data.local.ImageData
import com.coocaa.publib.data.local.MediaData
import com.coocaa.publib.data.local.VideoData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.sql.Date
import java.util.*


/**
 * @Author: yuzhan
 */
object LocalMediaManager {

    var context: Context? = null
    var imageList : MutableList<ImageData> = LinkedList<ImageData>()
    var videoList : MutableList<VideoData> = LinkedList<VideoData>()
    var thread = HandlerThread("local-media")
    var handler: Handler? = null

    val TAG = "LocalMedia"

    fun init(context: Context) {
        Log.d(TAG, "init in thread : ${Thread.currentThread().name}")
        this.context = context

        imageList.clear()
        videoList.clear()

        GlobalScope.launch(Dispatchers.IO) {
            Log.d(TAG, "launch init in thread : ${Thread.currentThread().name}")
            val img = async {
                loadImageList()
            }
            val video = async {
                loadVideoList()
            }
            img.await()
            video.await()
            Log.d(TAG, "after load image and video data,  in thread : ${Thread.currentThread().name}")

            if(handler == null) {
                thread.start()
                handler = Handler(thread.looper)
                register()
            }
        }
    }

    private fun loadImageList() {
        Log.d(TAG, "loadImageList start in thread : ${Thread.currentThread().name}")
        val columns = arrayOf<String>(MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_TAKEN)
        val cursor = context?.getContentResolver()?.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, MediaStore.Images.Media.DATE_TAKEN + " desc")

        cursor?.apply {
            try {
                while (cursor.moveToNext())
                {
                    val image = ImageData()
                    image.type = MediaData.TYPE.IMAGE
                    image.id = cursor.getLong(cursor
                            .getColumnIndex(MediaStore.Images.Media._ID))
                    image.url = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Images.Media.DATA))
                    image.tittle = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Images.Media.TITLE))
                    image.bucketName = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
                    image.size = cursor.getLong(cursor
                            .getColumnIndex(MediaStore.Images.Media.SIZE))
                    val time = cursor.getLong(cursor
                            .getColumnIndex(MediaStore.Images.Media.DATE_TAKEN))
                    image.takeTime = Date(time)
                    // 过滤非图片数据

                    //image.thumb = getImageThumbnailPath(context,image.id,image.data);
                    // 过滤非图片数据
                    if (image.url != null && image.url.toLowerCase().endsWith(".pdf")) {
                        continue
                    }
                    imageList.add(image)
                }
                Log.d(TAG, "loadImageList finish in thread : ${Thread.currentThread().name}")
            } finally {
                cursor.close()
            }
        }
    }

    private fun loadVideoList() {
        Log.d(TAG, "loadVideoList start in thread : ${Thread.currentThread().name}")
        val columns = arrayOf(MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA, MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.Media.DATE_TAKEN)

        val TNUMB_COLUMNS = arrayOf(
                MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID
        )

        val cursor = context?.contentResolver?.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns, null, null,
                MediaStore.Video.Media.DATE_ADDED + " desc")
        cursor?.apply {
            try {
                while (cursor.moveToNext()) {
                    val video = VideoData()
                    video.type = MediaData.TYPE.VIDEO
                    video.id = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    video.url = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    video.tittle = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    video.duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                    video.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    video.resolution = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION))
                    val time = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN))
                    video.takeTime = Date(time)

                    val thumbCursor = context!!.contentResolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, TNUMB_COLUMNS, MediaStore.Video.Thumbnails.VIDEO_ID + "=" + video.id, null, null)
                    if (thumbCursor != null && thumbCursor.moveToFirst()) {
                        video.thumbnailPath = thumbCursor.getString(thumbCursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA))
                    }

                    if (video.duration > 0L) {
                        videoList.add(video)
                    }
                }
                Log.d(TAG, "loadVideoList finish in thread : ${Thread.currentThread().name}")
            } finally {
                cursor.close()
            }
        }
    }

    private fun register() {
        val photoObserver = MyObserver(handler!!)
        val photoUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        context?.contentResolver?.registerContentObserver(photoUri, false, photoObserver)

        val videoObserver = MyObserver(handler!!)
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        context?.contentResolver?.registerContentObserver(videoUri, false, videoObserver)
    }

    interface MediaChangeListener {
        fun onChanged()
    }

    class MyObserver(handler: Handler) : ContentObserver(handler) {

        override fun onChange(selfChange: Boolean) {
            onChange(selfChange, null)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            Log.d(TAG, "监听到图库变化：uri=$uri")
        }

    }
}