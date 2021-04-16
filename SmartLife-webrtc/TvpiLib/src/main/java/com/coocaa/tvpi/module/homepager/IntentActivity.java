package com.coocaa.tvpi.module.homepager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.tvpi.module.share.intent.FileIntent;
import com.coocaa.tvpi.module.share.intent.ImageIntent;
import com.coocaa.tvpi.module.share.intent.TextIntent;
import com.coocaa.tvpi.module.share.intent.VideoIntent;
import com.coocaa.tvpi.util.share.ShareUriTool;

import java.util.Set;

import swaiotos.runtime.Applet;
import swaiotos.runtime.AppletRuntimeManager;
import swaiotos.runtime.base.AppletRunner;
import swaiotos.runtime.h5.H5AppletRunner;
import swaiotos.runtime.np.NPAppletRunner;

import static swaiotos.runtime.Applet.APPLET_H;
import static swaiotos.runtime.Applet.APPLET_MP;
import static swaiotos.runtime.Applet.APPLET_NP;

/**
 * @ClassName IntentActivity
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/11/12
 * @Version TODO (write something)
 */
public class IntentActivity extends BaseActivity {

    private static final String TAG = IntentActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "IntentActivity onCreate."+getIntent().toURI());
        boolean parse = false;
        try {
            parse = parseData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!parse) {
            Log.d(TAG, "parse intent fail, start main activity.");
            Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            try {
                Log.d("SmartApi", "start launch intent");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(intent);
            } catch (Exception e) {
                Log.d(TAG, "start fail : " + e.toString());
            }
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private boolean parseData() throws Exception{
        boolean ret = false;
        if (null != getIntent()) {
            Uri uri = parseUri();
            String type = getIntent().getType();
            Log.d(TAG, "parseData, uri=" + uri + ", type=" + type);
            if(!TextUtils.isEmpty(type) && (Intent.ACTION_SEND.equals(getIntent().getAction()) || Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction()))) {
                if(type.startsWith("image/")){ //图片分享
                    return ImageIntent.handleImageIntent(this, getIntent(), uri, Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction()));
                } else if(type.startsWith("video/")) {
                    return VideoIntent.handleVideoIntent(this, getIntent(), uri, Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction()));
                } else if("text/plain".equals(type)) {
                    return TextIntent.handleTextIntent(this, getIntent());
                }
            }
            if (null != uri) {
                String scheme = uri.getScheme();
                if (!TextUtils.isEmpty(scheme)) {
                    if(scheme.startsWith("ccsmartscreen")) { //分享跳转
                        ret = handleShareIntent();
                    } else {
                        if (scheme.startsWith(ContentResolver.SCHEME_FILE) || scheme.startsWith(ContentResolver.SCHEME_CONTENT)) {
                            ret = handleFileIntent(uri);
                        }
                    }
                }
            }
        } else {
            Log.d(TAG, "onCreate: data is null !!!");
        }
        return ret;
    }

    private Uri parseUri() {
        Uri uri = getIntent().getData();
        String type = getIntent().getType();
        Log.d(TAG, "uri = " + uri);
        Log.d(TAG, "type = " + type);
        if(uri == null) {
            try {
                uri = getIntent().getClipData().getItemAt(0).getUri();
                Log.d(TAG, "clip uri = " + uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return uri;
    }

    private boolean handleFileIntent(Uri uri) {
        String filePath = null;
        try {
            filePath = ShareUriTool.getFilePathByUri(this, uri);
            Log.d(TAG, "filePath=" + filePath);
            if (!TextUtils.isEmpty(filePath)) {
                Intent fileIntent = FileIntent.handleFilePath(this, filePath);
                if(fileIntent != null) {
                    try {
                        startActivity(fileIntent);
                        finish();
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean handleShareIntent() {
        if(handleShareIntent(this, getShareUri(getIntent()))) {
            finish();
            return true;
        }
        return false;
    }

    public static boolean handleShareIntent(Context context, final Uri shareUri) {
        if(shareUri != null) {
            boolean runtimeInitDone = AppletRuntimeManager.get(context.getApplicationContext()).initDone();
            Log.d(TAG, "runtimeInitDone=" + runtimeInitDone);
            if(runtimeInitDone) {
                if(AppletRuntimeManager.get(context.getApplicationContext()).startApplet(shareUri)) {
                    return true;
                }
            } else if(startActivitySelf(context, shareUri) == 0){
                return true;
            }
        }
        return false;
    }

    private static Uri getShareUri(Intent intent) {
        try {
            if (intent.getData() != null) {
                Set<String> paramsSet = intent.getData().getQueryParameterNames();
                String applet = intent.getData().getQueryParameter("applet");
//                if(applet != null && applet.startsWith("http")) {
                if (applet != null) {
                    Uri appletUri = Uri.parse(applet);
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme(appletUri.getScheme()).authority(appletUri.getAuthority()).path(appletUri.getPath());
                    //追加applet里的参数
                    Set<String> appletParamSet = appletUri.getQueryParameterNames();
                    for (String key : appletParamSet) {
                        builder.appendQueryParameter(key, appletUri.getQueryParameter(key));
                    }

                    //追加原始url里的参数
                    for (String key : paramsSet) {
                        if (!TextUtils.equals(key, "applet")) {
                            builder.appendQueryParameter(key, intent.getData().getQueryParameter(key));
                        }
                    }
                    Uri uri = builder.build();
                    Log.d(TAG, "handleShare applet=" + uri);
                    return uri;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    //AppletManager没有初始化好，只能自己解析启动
    public static int startActivitySelf(Context context, Uri applet) {
        Applet applet1 = Applet.Builder.parse(applet);
        String scheme = applet1.getType();
        if (TextUtils.isEmpty(scheme)) {
            return -1;
        }
        AppletRunner runner = null;
        if (APPLET_H.contains(scheme)) {
            runner = H5AppletRunner.get();
        } else if (APPLET_MP.equals(scheme)) {
//                runner = MPAppletRunner.get();
        } else if (APPLET_NP.equals(scheme)) {
            runner = NPAppletRunner.get();
        } else {
            return -2;
        }
        if (runner != null) {
            try {
                runner.start(context, applet1, null);
                return 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -3;
    }
}
