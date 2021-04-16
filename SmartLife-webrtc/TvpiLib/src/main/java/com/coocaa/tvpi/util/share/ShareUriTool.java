package com.coocaa.tvpi.util.share;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.coocaa.smartscreen.repository.utils.SmartScreenKit;
import com.coocaa.tvpi.util.FileChooseUtils;

import java.io.File;

import androidx.core.content.FileProvider;

public class ShareUriTool {

    private final static String TAG = "IntentActivity";

    public static String getFilePathByUri(Context context, Uri uri) throws Exception {
        String path = null;
        String scheme = uri.getScheme();

        // 以 file:// 开头的
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            path = uri.getPath();
            return path;
        }
        // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (columnIndex > -1) {
                        path = cursor.getString(columnIndex);
                    }
                }
                cursor.close();
            }
            return path;
        }
        // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                return parseDocumentPath(context, uri);
            } else if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
                return parseContentPath(context, uri);
            } else {
                String[] paths = uri.getPath().split("/0/");
                if (paths.length == 2) {
                    return Environment.getExternalStorageDirectory() + "/" + paths[1];
                }
            }
        }
        return null;
    }

    private static String parseContentPath(Context context, Uri uri) throws Exception {
        //Android 11 的处理
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !uri.getPath().contains(context.getPackageName())) {
            Log.i(TAG, "parseContentPath uri:" + uri.toString());
            return FileChooseUtils.uriToFileApiQ(context, uri);
        }

        // Return the remote address
        if (isGooglePhotosUri(uri)) {
            Log.d(TAG, "isGooglePhotosUri paths=" + uri.getLastPathSegment());
            return uri.getLastPathSegment();
        }

        //content://com.tencent.mobileqq.fileprovider/external_files/storage/emulated/0/Android/data/com.tencent.mobileqq/cache/share/%E7%AE%A1%E6%8E%A7%E5%B9%B3%E5%8F%B0.pptx
        if (isQQMediaDocument(uri)) {
            String paths = uri.getPath();
            Log.d(TAG, "isQQMediaDocument paths=" + paths);
            File file = new File(paths.substring("/external_files".length(), paths.length()));
            return file.exists() ? file.toString() : null;
        }

        //content://com.tencent.mm.external.fileprovider/external/Android/data/com.tencent.mm/MicroMsg/Download/%E7%AE%A1%E6%8E%A7%E5%B9%B3%E5%8F%B0.pptx
        if (isWXMediaDocument(uri)) {
            String paths = uri.getPath();
            Log.d(TAG, "isWXMediaDocument paths=" + paths);
            File fileDir = Environment.getExternalStorageDirectory();
            File file = new File(fileDir, paths.substring("/external".length(), paths.length()));
            return file.exists() ? file.toString() : null;
        }

        //content://com.huawei.hidisk.fileprovider/root/storage/emulated/0/Android/data/com.tencent.mm/MicroMsg/Download/%E3%80%90%E7%8E%8B%E8%91.pptx
        if (isHuaweiDocument(uri)) {
            String paths = uri.getPath();
            Log.d(TAG, "isHuaweiDocument paths=" + paths);
//            File fileDir = Environment.getExternalStorageDirectory();
//            File file = new File(fileDir, paths.substring("/external".length(), paths.length()));
//            return file.exists() ? file.toString() : null;
        }

        //content://com.android.filemanager.fileprovider/extfiles/Android/data/com.tencent.mm/MicroMsg/Download/B9%E5%BD%A2PPT%E6%A8%A1%E6%9D%BF.pptx
        if (isVivoFileManager(uri)) {
            String paths = uri.getPath();
            Log.d(TAG, "isVivoFileManager paths=" + paths);
            if (paths.contains("extfiles")) {
                File fileDir = Environment.getExternalStorageDirectory();
                File file = new File(fileDir, paths.substring("/extfiles".length(), paths.length()));
                return file.exists() ? file.toString() : null;
            }
        }

        String path = getDataColumn(context, uri, null, null);
        Log.d(TAG, "getDataColumn paths=" + uri.getLastPathSegment());
        if (path == null) {
            path = uri.getPath();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                    && path != null && path.startsWith("/external")) {
                path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + path.replace("/external", "")).getPath();
            } else {
                String[] paths = uri.getPath().split("/0/");
                if (paths.length == 2) {
                    path = Environment.getExternalStorageDirectory() + "/" + paths[1];
                }
            }
            Log.d(TAG, "getDataColumn fail, split path to : " + path);
        }
        return path;
    }

    private static String parseDocumentPath(Context context, Uri uri) throws Exception {
        String path = null;
        if (isExternalStorageDocument(uri)) {
            // ExternalStorageProvider
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];
            if ("primary".equalsIgnoreCase(type)) {
                path = Environment.getExternalStorageDirectory() + "/" + split[1];
                return path;
            }
        } else if (isDownloadsDocument(uri)) {
            // DownloadsProvider
            final String id = DocumentsContract.getDocumentId(uri);
            final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                    Long.valueOf(id));
            path = getDataColumn(context, contentUri, null, null);
            return path;
        } else if (isMediaDocument(uri)) {
            // MediaProvider
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
            }
            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{split[1]};
            path = getDataColumn(context, contentUri, selection, selectionArgs);
            return path;
        }
        return path;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) throws Exception {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    public static Uri PathToUri(String path) {
        Uri uri;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            uri = Uri.fromFile(new File(path));
        } else {
            /**
             * 7.0 调用系统相机拍照不再允许使用Uri方式，应该替换为FileProvider
             * 并且这样可以解决MIUI系统上拍照返回size为0的情况
             */
            uri = FileProvider.getUriForFile(SmartScreenKit.getContext(), "com.xinmang.videoeffect.fileprovider", new File(path));
        }

        return uri;
    }

    /**
     * 使用第三方qq文件管理器打开
     *
     * @param uri
     * @return
     */
    public static boolean isQQMediaDocument(Uri uri) {
        return "com.tencent.mobileqq.fileprovider".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    //content://com.tencent.mm.external.fileprovider/external/Android/data/com.tencent.mm/MicroMsg/Download/%E7%AE%A1%E6%8E%A7%E5%B9%B3%E5%8F%B0.pptx
    public static boolean isWXMediaDocument(Uri uri) {
        return "com.tencent.mm.external.fileprovider".equals(uri.getAuthority());
    }

    //content://com.huawei.hidisk.fileprovider/root/storage/emulated/0/Android/data/com.tencent.mm/MicroMsg/Download/%E3%80%90%E7%8E%8B%E8%91.pptx
    public static boolean isHuaweiDocument(Uri uri) {
        return "com.huawei.hidisk.fileprovider".equals(uri.getAuthority());
    }

    /**
     * Vivo手机从文件管理器选择微信、QQ分类下的文档分享到智屏，路径带有extfiles开头
     *
     * @param uri
     * @return
     */
    //content://com.android.filemanager.fileprovider/extfiles/Android/data/com.tencent.mm/MicroMsg/Download/B9%E5%BD%A2PPT%E6%A8%A1%E6%9D%BF.pptx
    public static boolean isVivoFileManager(Uri uri) {
        return "com.android.filemanager.fileprovider".equals(uri.getAuthority());
    }
}

