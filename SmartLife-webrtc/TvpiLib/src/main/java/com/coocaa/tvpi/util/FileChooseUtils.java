package com.coocaa.tvpi.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author chenaojun
 */
public class FileChooseUtils {

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else if ("home".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/documents/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                // DownloadsProvider
                final String id = DocumentsContract.getDocumentId(uri);
                if (TextUtils.isEmpty(id)) {
                    return null;
                }
                if (id.startsWith("raw:")) {
                    return id.substring(4);
                }
                if (id.startsWith("msf:") && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return uriToFileApiQ(context, uri);
                } else {
                    String[] contentUriPrefixesToTry = new String[]{
                            "content://downloads/public_downloads",
                            "content://downloads/my_downloads",
                            "content://downloads/all_downloads"
                    };
                    for (String contentUriPrefix : contentUriPrefixesToTry) {
                        try {
                            Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));
                            String path = getDataColumn(context, contentUri, null, null);
                            if (path != null) {
                                return path;
                            }
                        } catch (Exception ignore) {
                        }
                    }
                    try {
                        String path = getDataColumn(context, uri, null, null);
                        if (path != null) {
                            return path;
                        }
                    } catch (Exception ignore) {
                    }
                }
                // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                return null;
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else if ("document".equals(type)) {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        return uriToFileApiQ(context, uri);
                    }
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return uriToFileApiQ(context, uri);
            } else {
                return getDataColumn(context, uri, null, null);
            }
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Android 10 以上适配 另一种写法
     *
     * @param context
     * @param uri
     * @return
     */
    private static String getFileFromContentUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        String filePath;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, filePathColumn, null,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            try {
                filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                return filePath;
            } catch (Exception e) {
            } finally {
                cursor.close();
            }
        }
        return "";
    }

    /**
     * Android 10 以上适配 （Android 11无法读取其他应用的内部数据，只能复制到沙盒目录）
     *
     * @param context
     * @param uri
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static String uriToFileApiQ(Context context, Uri uri) {
        File file = null;
        //android10以上转换
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                try {
                    InputStream is = contentResolver.openInputStream(uri);
//                    File cache = new File(context.getExternalCacheDir().getAbsolutePath(), Math.round((Math.random() + 1) * 1000) + displayName);
                    File cache = new File(context.getExternalCacheDir().getAbsolutePath(), displayName);
                    FileOutputStream fos = new FileOutputStream(cache);
                    android.os.FileUtils.copy(is, fos);
                    file = cache;
                    fos.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (file == null) {
            file = new File(uri.getPath());
        }
        return file.getAbsolutePath();
    }

}
