package com.coocaa.tvpi.module.share.intent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import com.coocaa.publib.data.local.ImageData;
import com.coocaa.tvpi.module.local.media.LocalMediaActivity;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import androidx.core.content.FileProvider;

/**
 * @Author: yuzhan
 */
public class ImageIntent {

    private final static String TAG = "IntentActivity";

    public static boolean handleImageIntent(Context context, Intent _intent, Uri uri, boolean isMulple) {
        try {
            List<ImageData> imageDataList = null;
            if(isMulple) {
                ArrayList<Uri> arrayList = _intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if(arrayList != null) {
                    imageDataList = new ArrayList<>(arrayList.size());
                }
                String tempRealPath = null;
                for(Uri u : arrayList) {
                    ImageData imageData = new ImageData();
                    tempRealPath = getRealPath((Activity) context, u);
                    Log.d(TAG, "img uri : " + imageData.url);
                    Log.d(TAG, "img real path : " + tempRealPath);
                    imageData.url = tempRealPath == null ? u.toString() : tempRealPath;
                    imageDataList.add(imageData);
                }
            } else {
                imageDataList = new ArrayList<>(1);
                Uri imgUri = _intent.getParcelableExtra(Intent.EXTRA_STREAM);
                String tempRealPath = getRealPath(context , imgUri);
                Log.d(TAG, "img uri : " + imgUri);
                Log.d(TAG, "img real path : " + tempRealPath);
                ImageData imageData = new ImageData();
                imageData.url = tempRealPath == null ? imgUri.toString() : tempRealPath;
                imageDataList.add(imageData);
            }
            if(imageDataList != null && !imageDataList.isEmpty()) {
                return startImageActivity(context, imageDataList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean startImageActivity(Context context, List<ImageData> imageDataList) throws Exception{
        /*Intent intent = new Intent(context, AlbumPreviewActivity2.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("fromShare", true);
        bundle.putParcelable("IMAGEDATA", imageDataList.get(0)); //暂时只支持一个
        intent.putExtras(bundle);
        if(! (context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("np");
        builder.authority("com.coocaa.smart.localpicture");
        builder.path("preview");
        String uri = builder.build().toString();
        bundle.putString("applet", uri);
        intent.putExtra("swaiotos.applet", bundle);

        context.startActivity(intent);*/

        Intent intent = new Intent(context, LocalMediaActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("fromShare", true);
        bundle.putParcelableArrayList("IMAGEDATA", (ArrayList<? extends Parcelable>) imageDataList);
        bundle.putString("type", "picture");
        intent.putExtras(bundle);

        if(! (context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
        return true;
    }

    //需要投到远程，所以需要获取真实图片地址
    private static String getRealPath(Context context, Uri contentUri) {
        if(! (context instanceof Activity))
            return null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = ((Activity) context).managedQuery(contentUri, proj, // Which columns to return
                null, // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        if (cursor==null) {
            return contentUri.getPath();
        }

        try {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            e.printStackTrace();
            return getFileProviderUriToPath(context, contentUri);
        }

    }


    private static String getFileProviderUriToPath(Context context, Uri uri) {
        try {
            List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
            if (packs != null) {
                String fileProviderClassName = FileProvider.class.getName();
                for (PackageInfo pack : packs) {
                    ProviderInfo[] providers = pack.providers;
                    if (providers != null) {
                        for (ProviderInfo provider : providers) {
                            if (uri.getAuthority().equals(provider.authority)) {
                                if (provider.name.equalsIgnoreCase(fileProviderClassName)) {
                                    Class<FileProvider> fileProviderClass = FileProvider.class;
                                    try {
                                        Method getPathStrategy = fileProviderClass.getDeclaredMethod("getPathStrategy", Context.class, String.class);
                                        getPathStrategy.setAccessible(true);
                                        Object invoke = getPathStrategy.invoke(null, context, uri.getAuthority());
                                        if (invoke != null) {
                                            String PathStrategyStringClass = FileProvider.class.getName() + "$PathStrategy";
                                            Class<?> PathStrategy = Class.forName(PathStrategyStringClass);
                                            Method getFileForUri = PathStrategy.getDeclaredMethod("getFileForUri", Uri.class);
                                            getFileForUri.setAccessible(true);
                                            Object invoke1 = getFileForUri.invoke(invoke, uri);
                                            if (invoke1 instanceof File) {
                                                String filePath = ((File) invoke1).getAbsolutePath();
                                                Log.d(TAG, "getFileProviderUriToPath, uri=" + uri + ", path=" + filePath);
                                                return filePath;
                                            }
                                        }
                                    } catch (NoSuchMethodException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
