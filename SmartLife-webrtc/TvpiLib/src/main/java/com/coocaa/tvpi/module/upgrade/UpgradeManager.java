package com.coocaa.tvpi.module.upgrade;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder;
import com.allenliu.versionchecklib.v2.builder.NotificationBuilder;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.allenliu.versionchecklib.v2.callback.CustomDownloadingDialogListener;
import com.allenliu.versionchecklib.v2.callback.CustomVersionDialogListener;
import com.allenliu.versionchecklib.v2.callback.RequestVersionListener;
import com.allenliu.versionchecklib.v2.callback.UpdateListener;
import com.coocaa.publib.PublibHelper;
import com.coocaa.publib.utils.SpUtil;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.data.BaseData;
import com.coocaa.smartscreen.data.upgrade.UpgradeData;
import com.coocaa.smartscreen.data.upgrade.UpgradeResp;
import com.coocaa.smartscreen.network.NetWorkManager;
import com.coocaa.smartscreen.repository.utils.IOTServerUtil;
import com.coocaa.smartscreen.repository.utils.SmartScreenKit;
import com.coocaa.tvpi.util.SizeConverter;
import com.coocaa.tvpilib.R;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * @ClassName UpgradeManager
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/11/1
 * @Version TODO (write something)
 */
public class UpgradeManager {

    private static final String TAG = UpgradeManager.class.getSimpleName();
    //上次显示升级弹窗的时间
    public static String KEY_LAST_SHOW_UPDATE_DIALOG_TIME = "key_last_show_update_dialog_time";
    //上次是否同意升级
    public static String KEY_LAST_AGREE_UPDATE = "key_last_agree_update";

    private static final String UPGRADE_APP_ID = "13";//应用ID, 12为智屏开发版，13为智屏线上版

    private static UpgradeManager mInstance;

    private String versionStr = null;

    private DownloadBuilder builder;

    private Activity activity;

    private UpgradeData upgradeData;

    public synchronized static UpgradeManager getInstance() {
        if (mInstance == null) {
            mInstance = new UpgradeManager();
        }
        return mInstance;
    }

    public interface UpgradeCallback {
        void onSuccess(UpgradeData upgradeData);

        void onFailed(Throwable e);
    }

    public void upgradeLatest(final UpgradeCallback callback) {
        HashMap<String, String> map = new HashMap<>();
        map.put("app_id", UPGRADE_APP_ID); //应用ID, 12为智屏开发版，13为智屏线上版

        map = IOTServerUtil.getQueryMap(map);
        Log.d(TAG, "upgradeLatest: " + map);

        NetWorkManager.getInstance()
                .getSkyworthIotService()
                .upgradeLatest(map)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String response = "";
                        try {
                            response = responseBody.string();
                            Log.d(TAG, "onNext: " + response);
                            UpgradeResp upgradeResp = new Gson().fromJson(response, UpgradeResp.class);

                            if (null != callback)
                                callback.onSuccess(upgradeResp.data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.toString());
                        if (null != callback)
                            callback.onFailed(e);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public void downloadLatestAPK() {
        downloadLatestAPK(false);
    }

    public void downloadLatestAPK(boolean isFromAutoCheckUpdate) {
        HashMap<String, String> map = new HashMap<>();
        map.put("app_id", UPGRADE_APP_ID); //应用ID, 12为智屏开发版，13为智屏线上版
        map = IOTServerUtil.getQueryMap(map);
        String urlParams = getQueryString(map);
        String url = "https://api.skyworthiot.com/otaupdate/app/upgrade/latest" + urlParams;

        Log.d(TAG, "downloadLatestAPK: " + url);
        builder = AllenVersionChecker
                .getInstance()
                .requestVersion()
                .setRequestUrl(url)
                .request(new RequestVersionListener() {
                    @Nullable
                    @Override
                    public UIData onRequestVersionSuccess(String result) {
                        Log.d(TAG, "onRequestVersionSuccess: " + result);
                        if (!TextUtils.isEmpty(result)) {
                            UpgradeResp upgradeResp = BaseData.load(result, UpgradeResp.class);
                            if (null == upgradeResp) {
                                return null;
                            }
                            upgradeData = upgradeResp.data;
                            if (upgradeData != null) {
                                // 是否要升级(是否要升级，1：是，2：否)
                                long versionCode = getAppVersionCode(SmartScreenKit.getContext());
                                if (upgradeData.version_code > versionCode) {
                                    /*if (refusedUpdate()) {
                                        Log.d(TAG, "用户忽略该版本升级");
                                        return null;
                                    }*/
                                    builder.setNotificationBuilder(createCustomNotification());
                                    return crateUIData();
                                }
                            }
                        }
                        return null;
                    }

                    @Override
                    public void onRequestVersionFailure(String message) {
//                        ToastUtils.showGlobalShort(MyApplication.getContext().getResources().getString(R.string.download_apk_fail), true);
                    }
                });

        String sdpath = getAPKPath();
        builder.setDownloadAPKPath(sdpath);
        builder.setForceRedownload(true);
        Log.d(TAG, "downloadLatestAPK: sdpath：" + sdpath);
        builder.setShowDownloadingDialog(true);
        builder.excuteMission(SmartScreenKit.getContext());
        builder.setCustomVersionDialogListener(new CustomVersionDialogListener() {
            @Override
            public Dialog getCustomVersionDialog(Context context, UIData versionBundle) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.custom_version_dialog_layout);
                TextView title = dialog.findViewById(R.id.tvTitle);
                TextView content = dialog.findViewById(R.id.tvContent);
                TextView commit = dialog.findViewById(R.id.versionchecklib_version_dialog_commit);
                title.setText("发现新版本" + upgradeData.app_version);
                content.setText(upgradeData.update_log.replace("；", "\n"));
                commit.setText("立即更新（" + SizeConverter.BTrim.convert(Float.valueOf(upgradeData.filesize)) + "）");
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
                    dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                }

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        //更新上次显示的时间到sp
                        if (isFromAutoCheckUpdate) {
                            SpUtil.putLong(context, KEY_LAST_SHOW_UPDATE_DIALOG_TIME, System.currentTimeMillis());
                        }
                    }
                });

                dialog.setCancelable(false);
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                        return keyCode == KeyEvent.KEYCODE_BACK;
                    }
                });

                return dialog;
            }
        });
        builder.setCustomDownloadingDialogListener(new CustomDownloadingDialogListener() {
            @Override
            public Dialog getCustomDownloadingDialog(Context context, int progress, UIData versionBundle) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.custom_loading_dialog_layout);
                TextView title = dialog.findViewById(R.id.tvTitle);
                TextView content = dialog.findViewById(R.id.tvContent);
                title.setText("发现新版本" + upgradeData.app_version);
                content.setText(upgradeData.update_log.replace("；", "\n"));
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
                    dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                }

                dialog.setCancelable(false);
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                        return keyCode == KeyEvent.KEYCODE_BACK;
                    }
                });

                return dialog;
            }

            @Override
            public void updateUI(Dialog dialog, int progress, UIData versionBundle) {
                ProgressBar pb = dialog.findViewById(com.allenliu.versionchecklib.R.id.pb);
                pb.setProgress(progress);
                TextView tvProgress = dialog.findViewById(com.allenliu.versionchecklib.R.id.tv_progress);
                tvProgress.setText(progress + "%");
            }
        });
        builder.setUpdateListener(new UpdateListener() {
            @Override
            public void onPositive() {
                Log.d(TAG, "onPositive: ");
                ToastUtils.getInstance().showGlobalShort("开始下载,可在通知栏查看进度");
                if (isFromAutoCheckUpdate) {
                    SpUtil.putBoolean(PublibHelper.getContext(), KEY_LAST_AGREE_UPDATE, true);
                }

            }

            @Override
            public void onNegative() {
                Log.d(TAG, "onNegative: ");
                if (isFromAutoCheckUpdate) {
                    SpUtil.putBoolean(PublibHelper.getContext(), KEY_LAST_AGREE_UPDATE, false);
                }
            }
        });
    }

    private NotificationBuilder createCustomNotification() {
        return NotificationBuilder.create()
                .setRingtone(true)
                .setIcon(R.drawable.logo)
                .setTicker("酷开智屏下载更新")
                .setContentTitle("共享屏")
                .setContentText(upgradeData.app_version);
    }

    private UIData crateUIData() {
        if (!TextUtils.isEmpty(upgradeData.update_log)) {
            String version_name = "V" + upgradeData.app_version + "\n";
            String update_log = upgradeData.update_log.replace("；", "\n");

            UIData uiData = UIData.create();
            uiData.setTitle("下载并更新");
            uiData.setDownloadUrl(upgradeData.download_url);
            uiData.setContent(version_name + update_log);
            return uiData;
        } else {
            UIData uiData = UIData.create();
            uiData.setTitle("下载并更新");
            uiData.setDownloadUrl(upgradeData.download_url);
            uiData.setContent("更多功能等待探索");
            return uiData;
        }
    }

    private String getAPKPath() {
        String appCachePath = null;
        if (checkSDCard()) {
            appCachePath = Environment.getExternalStorageDirectory() + "/smartscreen/";
        } else {
            appCachePath = Environment.getDataDirectory().getPath() + "/smartscreen/";
        }
        File file = new File(appCachePath);
        if (!file.exists()) {
            file.mkdirs();
        }

        return appCachePath;
    }

    private boolean checkSDCard() {
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);

        return sdCardExist;
    }

    private boolean refusedUpdate() {
        boolean refused = false;
        int localVersionCode = SpUtil.getInt(SmartScreenKit.getContext(), "RefusedVersionCode", 0);
        refused = localVersionCode == upgradeData.version_code;
        return refused;
    }

    private void saveRefusedVersionCode() {
        if (null != upgradeData)
            SpUtil.putLong(SmartScreenKit.getContext(), "RefusedVersionCode", upgradeData.version_code);
    }

    private static String getQueryString(Map<String, String> queryMap) {
        String other = "";
        if (queryMap != null && !queryMap.isEmpty()) {
            for (Map.Entry<String, String> entry : queryMap.entrySet()) {
                other += entry.getKey() + "=" + entry.getValue() + "&";
            }
        }
        Log.d(TAG, "getQueryString: " + other);
        return "?" + other;
    }


    /**
     * 返回当前程序版本号
     */
    public long getAppVersionCode(Context context) {
        long versionCode = 0;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);

            versionCode = pi.versionCode;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        Log.d(TAG, "getAppVersionCode: versionCode" + versionCode);
        return versionCode;
    }

}
