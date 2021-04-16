package com.coocaa.tvpi.module.local.document;

import android.content.Context;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsDownloadConfig;
import com.tencent.smtt.sdk.TbsDownloader;
import com.tencent.smtt.sdk.TbsListener;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 2020/10/20
 */
public class DocumentBrowser {
    private final static String TAG = DocumentBrowser.class.getSimpleName();

    private static OnInitListener mListener;
    private static boolean mTbsX5InitFisish = false;//是否初始化完成
    private static boolean mTbsX5InitSuccess = false;//tbs内核加载是否成功

    public static void setInitListener(OnInitListener listener) {
        mListener = listener;
        if (mTbsX5InitFisish) {
            callbackResult();
        }
    }

    public static void clearListener() {
        mListener = null;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public static void init(Context context, String from) {
        try {
            //首次初始化冷启动优化
//            dex2oat();
            initTbs(context, from);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void callbackResult() {
        if (mListener != null) {
            mListener.onInitFinish(mTbsX5InitSuccess);
        }
    }

//    private static void dex2oat() {
//        //TBS内核首次使用和加载时，ART虚拟机会将Dex文件转为Oat，该过程由系统底层触发且耗时较长，很容易引起anr问题，解决方法是使用TBS的 ”dex2oat优化方案“。
//        HashMap map = new HashMap();
//        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
//        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
//        QbSdk.initTbsSettings(map);
//    }

    public static boolean isInited() {
        return QbSdk.isTbsCoreInited();
    }

    private static void initTbs(final Context context, String from) {
        Log.i(TAG, "initTbs X5 core preInit start....from:" + from);
        //清除本地配置缓存，越过TBS 24小时只能下载一次内核的限制
        TbsDownloadConfig.getInstance(context).clear();
        //非wifi条件下允许下载X5内核
        QbSdk.setDownloadWithoutWifi(true);
        QbSdk.PreInitCallback mPreInitCallback = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                //这里被回调，并且arg0=true说明内核初始化并可以使用
                //如果arg0=false,内核会尝试安装，你可以通过下面监听接口获知
                Log.d(TAG, "initTbs onViewInitFinished is : " + arg0);
                mTbsX5InitFisish = true;
                mTbsX5InitSuccess = arg0;
                callbackResult();
                if (!mTbsX5InitSuccess) {
                    QbSdk.preInit(context, this);
                }
            }

            @Override
            public void onCoreInitFinished() {
                //x5内核初始化完成回调接口，此接口回调并表示已经加载起来了x5，有可能特殊情况下x5内核加载失败，切换到系统内核。
                Log.d(TAG, "initTbs onCoreInitFinished.");
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(context, mPreInitCallback);
        //判断是否要自行下载内核
        boolean needDownload = TbsDownloader.needDownload(context, TbsDownloader.DOWNLOAD_OVERSEA_TBS);
        Log.d(TAG, "initTbs needDownload：" + needDownload);
        if (needDownload) {
            // 启动下载
            TbsDownloader.startDownload(context);
        }
        //下载监听
        QbSdk.setTbsListener(mTbsListener);
        Log.i(TAG, "initTbs: QbSdk.isTbsCoreInited():" + QbSdk.isTbsCoreInited());
        Log.i(TAG, "initTbs: QbSdk.canLoadX5():" + QbSdk.canLoadX5(context));
        Log.i(TAG, "initTbs: QbSdk.canLoadX5FirstTimeThirdApp():" + QbSdk.canLoadX5FirstTimeThirdApp(context));
    }

    private final static TbsListener mTbsListener = new TbsListener() {
        @Override
        public void onDownloadFinish(int i) {
            //tbs内核下载完成回调
            Log.i(TAG, "initTbs onDownloadFinish: " + i);
        }

        @Override
        public void onInstallFinish(int i) {
            //内核安装完成回调，
            Log.i(TAG, "initTbs onInstallFinish: " + i);
        }

        @Override
        public void onDownloadProgress(int i) {
            //下载进度监听
            Log.i(TAG, "initTbs onDownloadProgress: " + i);
        }
    };

    public interface OnInitListener {
        void onInitFinish(boolean ret);
    }
}
