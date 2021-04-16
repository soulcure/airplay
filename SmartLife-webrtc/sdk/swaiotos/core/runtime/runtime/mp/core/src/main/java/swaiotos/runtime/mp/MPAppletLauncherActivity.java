package swaiotos.runtime.mp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.dcloud.common.DHInterface.ICallBack;
import io.dcloud.feature.sdk.DCSDKInitConfig;
import io.dcloud.feature.sdk.DCUniMPSDK;
import io.dcloud.feature.sdk.MenuActionSheetItem;

public class MPAppletLauncherActivity extends Activity {
    public static void launch(Context context, Uri applet) {
        Intent intent = new Intent();
        intent.setClass(context, MPAppletLauncherActivity.class);
        intent.putExtra("applet", applet.toString());
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    interface Callback {
        void onSuccess(Uri applet);

        void onFailed(Uri applet, Throwable throwable);
    }

    static {
//        try {
//            WXSDKEngine.registerModule("TestModule", Ex1Module.class);
//        } catch (WXException e) {
//            e.printStackTrace();
//        }
//        try {
//            WXSDKEngine.registerComponent("myText", Ex1Component.class);
//        } catch (WXException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        final Uri applet = Uri.parse(intent.getStringExtra("applet"));

        if (!DCUniMPSDK.getInstance().isInitialize()) {
            MenuActionSheetItem item = new MenuActionSheetItem("关于", "gy");
            List<MenuActionSheetItem> sheetItems = new ArrayList<>();
            sheetItems.add(item);
            DCSDKInitConfig config = new DCSDKInitConfig.Builder()
                    .setCapsule(true)
                    .setEnableBackground(true)
                    .setMenuDefFontSize("16px")
                    .setMenuDefFontColor("#ff00ff")
                    .setMenuDefFontWeight("normal")
                    .setMenuActionSheetItems(sheetItems)
                    .build();

            DCUniMPSDK.getInstance().initialize(this, config, new DCUniMPSDK.IDCUNIMPPreInitCallback() {
                @Override
                public void onInitFinished(boolean isSuccess) {
                    Log.e("unimp", "onInitFinished-----------" + isSuccess);
//                    File path = new File(getFilesDir(), "apps");
//                    String f = copyAssetsSingleFile(MainActivity.this, "apps/ex2.wgt", path.getAbsolutePath(), appid + ".wgt");
//
//                    DCUniMPSDK.getInstance().releaseWgtToRunPathFromePath(appid, f, new ICallBack() {
//                        @Override
//                        public Object onCallBack(int i, Object o) {
//                            try {
//                                DCUniMPSDK.getInstance().startApp(MPAppletLauncherActivity.this, appid);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            return null;
//                        }
//                    });
//
                    preload(applet, callback);
                }
            });
        } else {
            preload(applet, callback);
        }
    }

    private Callback callback = new Callback() {
        @Override
        public void onSuccess(Uri applet) {
            start(applet);
        }

        @Override
        public void onFailed(Uri applet, Throwable throwable) {

        }
    };

    private void start(Uri applet) {
        String appid = applet.getEncodedAuthority();
        String path = applet.getEncodedPath();

        JSONObject arguments = null;
        Set<String> keys = applet.getQueryParameterNames();
        if (keys != null && keys.size() > 0) {
            arguments = new JSONObject();
            for (String key : keys) {
                try {
                    arguments.put(key, applet.getQueryParameter(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        DCUniMPSDK.getInstance().closeCurrentApp();
        try {
            DCUniMPSDK.getInstance().startApp(MPAppletLauncherActivity.this, appid, null, path, arguments);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    private static final String MP_SERVER = "http://172.20.151.160:8081/luyuxiang/";

    private void preload(final Uri applet, Callback callback) {
        final String appid = applet.getEncodedAuthority();
        if (DCUniMPSDK.getInstance().isExistsApp(appid)) {
            callback.onSuccess(applet);
        } else {
            final String url = MP_SERVER + appid;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Downloader.get().download(url, new File(getCacheDir(), "mp-download").getAbsolutePath(), appid + ".wgt", new Downloader.OnDownloadListener() {
                        @Override
                        public void onDownloadSuccess(File file) {
                            DCUniMPSDK.getInstance().releaseWgtToRunPathFromePath(appid, file.getAbsolutePath(), new ICallBack() {
                                @Override
                                public Object onCallBack(int i, Object o) {
                                    start(applet);
//                            try {
//                                DCUniMPSDK.getInstance().startApp(MPAppletLauncherActivity.this, appid);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
                                    return null;
                                }
                            });
                        }

                        @Override
                        public void onDownloading(int progress) {

                        }

                        @Override
                        public void onDownloadFailed(Exception e) {

                        }
                    });
                }
            }).start();

        }
    }
}