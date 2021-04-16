package com.coocaa.tvpi.module.local.document;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.coocaa.smartscreen.data.clientconfig.ClientConfigHttpData;
import com.coocaa.smartscreen.repository.http.home.HomeHttpMethod;
import com.coocaa.smartscreen.utils.SpUtil;
import com.coocaa.tvpi.module.io.HomeIOThread;

import java.io.File;
import java.io.Serializable;

/**
 * @Description: 文档服务端视频资源
 * @Author: wzh
 * @CreateDate: 1/12/21
 */
public class DocumentResManager {

    private final static String TAG = "DocumentResManager";
    private final static String SP_KEY_DOC_VIDEO_INFO = "doc_video_info";
    public final static String VIDEO_TYPE_QQ = "qq";
    public final static String VIDEO_TYPE_WECHAT = "wechat";
    public final static String VIDEO_TYPE_WEIXINWORK = "weixinwork";
    public final static String VIDEO_TYPE_DINGDING = "dingding";
    private Context mContext;
    private VideoInfo mVideoInfo;
    private VideoResDownloadListener mVideoResDownloadListener;
    private static DocumentResManager instance;

    private DocumentResManager() {

    }

    public static DocumentResManager getInstance() {
        if (instance == null) {
            instance = new DocumentResManager();
        }
        return instance;
    }

    public void setVideoResDownloadListener(VideoResDownloadListener listener) {
        mVideoResDownloadListener = listener;
    }

    public void clearListener() {
        mVideoResDownloadListener = null;
    }

    public void init(Context context) {
        mContext = context;
        //Android 11的设备从服务器获取文档教程视频，下载到本地备用
//        if (DocumentUtil.isAndroidR()) {//所有Android版本都要下载
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ClientConfigHttpData.ClientConfigData data = HomeHttpMethod.getInstance().getClientConfig();
                    if (data != null && data.video != null) {
                        Log.i(TAG, "init getClientConfig  url --> " + JSON.toJSONString(data.video));
                        mVideoInfo = new VideoInfo();
                        VideoInfo cacheVideoInfo = SpUtil.getObject(mContext, SP_KEY_DOC_VIDEO_INFO);
                        if (cacheVideoInfo != null) {
                            mVideoInfo = cacheVideoInfo;
                        }
                        boolean qqNeedUpdate = isNeedUpdate(mVideoInfo.qqUrl, data.video.qq, mVideoInfo.qqSavePath);
                        boolean wechatNeedUpdate = isNeedUpdate(mVideoInfo.wechatUrl, data.video.wechat, mVideoInfo.wechatSavePath);
                        boolean weixinWorkNeedUpdate = isNeedUpdate(mVideoInfo.weixinWorkUrl, data.video.enterpriseWeChat, mVideoInfo.weixinWorkSavePath);
                        boolean dingdingNeedUpdate = isNeedUpdate(mVideoInfo.dingdingUrl, data.video.dingding, mVideoInfo.dingdingSavePath);
                        Log.i(TAG, "init qqNeedUpdate:" + qqNeedUpdate + "--wechatNeedUpdate:" + wechatNeedUpdate + "--weixinWorkNeedUpdate:" + weixinWorkNeedUpdate + "--dingdingNeedUpdate:" + dingdingNeedUpdate);
                        if (qqNeedUpdate) {
                            mVideoInfo.qqUrl = data.video.qq;
                            downloadVideo(VIDEO_TYPE_QQ, data.video.qq, mVideoInfo.qqSavePath);
                        }
                        if (wechatNeedUpdate) {
                            mVideoInfo.wechatUrl = data.video.wechat;
                            downloadVideo(VIDEO_TYPE_WECHAT, data.video.wechat, mVideoInfo.wechatSavePath);
                        }
                        if (weixinWorkNeedUpdate) {
                            mVideoInfo.weixinWorkUrl = data.video.enterpriseWeChat;
                            downloadVideo(VIDEO_TYPE_WEIXINWORK, data.video.enterpriseWeChat, mVideoInfo.weixinWorkSavePath);
                        }
                        if (dingdingNeedUpdate) {
                            mVideoInfo.dingdingUrl = data.video.dingding;
                            downloadVideo(VIDEO_TYPE_DINGDING, data.video.dingding, mVideoInfo.dingdingSavePath);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
//        }
    }

    private boolean isNeedUpdate(String oldUrl, String newUrl, String savePath) {
        boolean isNeedUpdate;
        if (urlIsValid(oldUrl) && urlIsValid(newUrl) && oldUrl.equals(newUrl)) {
            isNeedUpdate = !isExists(savePath);
        } else {
            isNeedUpdate = urlIsValid(newUrl);
        }
        return isNeedUpdate;
    }

    private boolean urlIsValid(String url) {
        return !TextUtils.isEmpty(url) && (url.startsWith("http") || url.startsWith("https"));
    }

    private boolean isExists(String path) {
        return !TextUtils.isEmpty(path) && new File(path).exists();
    }

    private void downloadVideo(String type, String url, String lastSavePath) {
        String fileName = DocumentUtil.getFileNameFromPath(url);
        new FileDownloader().download(url, fileName, new FileDownloader.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(String saveDir) {
                Log.i(TAG, "onDownloadSuccess: " + saveDir);
                if (mVideoInfo == null) {
                    mVideoInfo = new VideoInfo();
                }
                switch (type) {
                    case VIDEO_TYPE_WECHAT:
                        mVideoInfo.wechatSavePath = saveDir;
                        break;
                    case VIDEO_TYPE_WEIXINWORK:
                        mVideoInfo.weixinWorkSavePath = saveDir;
                        break;
                    case VIDEO_TYPE_DINGDING:
                        mVideoInfo.dingdingSavePath = saveDir;
                        break;
                    case VIDEO_TYPE_QQ:
                        mVideoInfo.qqSavePath = saveDir;
                        break;
                }
                SpUtil.putObject(mContext, SP_KEY_DOC_VIDEO_INFO, mVideoInfo);
                if (mVideoResDownloadListener != null && mVideoResDownloadListener.getVideoType().equals(type)) {
                    mVideoResDownloadListener.onSuccess(saveDir);
                    mVideoResDownloadListener = null;
                }
                if (!saveDir.equals(lastSavePath) && isExists(lastSavePath)) {
                    Log.i(TAG, "onDownloadSuccess delete last:" + lastSavePath);
                    DocumentUtil.deleteFile(lastSavePath);
                }
            }

            @Override
            public void onDownloading(int progress) {
                Log.i(TAG, "onDownloading: " + progress);
            }

            @Override
            public void onDownloadFailed(String url, ResultEnum result) {
                Log.i(TAG, "onDownloadFailed: " + result.getMsg());
                if (mVideoResDownloadListener != null && mVideoResDownloadListener.getVideoType().equals(type)) {
                    mVideoResDownloadListener.onFailed(result.getMsg());
                    mVideoResDownloadListener = null;
                }
            }
        });
    }

    public String getVideoPath(String type) {
        String path = "";
        if (mVideoInfo == null) {
            mVideoInfo = SpUtil.getObject(mContext, SP_KEY_DOC_VIDEO_INFO);
        }
        if (mVideoInfo != null) {
            switch (type) {
                case VIDEO_TYPE_WECHAT:
                    path = mVideoInfo.wechatSavePath;
                    break;
                case VIDEO_TYPE_WEIXINWORK:
                    path = mVideoInfo.weixinWorkSavePath;
                    break;
                case VIDEO_TYPE_DINGDING:
                    path = mVideoInfo.dingdingSavePath;
                    break;
                case VIDEO_TYPE_QQ:
                    path = mVideoInfo.qqSavePath;
                    break;
            }
            if (!TextUtils.isEmpty(path)) {
                if (!isExists(path)) {
                    path = getHttpUrl(type);
                }
            } else {
                path = getHttpUrl(type);
            }
        }
        return path;
    }

    private String getHttpUrl(String type) {
        if (mVideoInfo != null) {
            switch (type) {
                case VIDEO_TYPE_WECHAT:
                    return mVideoInfo.wechatUrl;
                case VIDEO_TYPE_WEIXINWORK:
                    return mVideoInfo.weixinWorkUrl;
                case VIDEO_TYPE_DINGDING:
                    return mVideoInfo.dingdingUrl;
                case VIDEO_TYPE_QQ:
                    return mVideoInfo.qqUrl;
            }
        }
        return "";
    }

    public static class VideoInfo implements Serializable {
        private static final long serialVersionUID = 5977770573261900388L;
        public String wechatUrl;//微信视频http下载地址
        public String wechatSavePath;//微信视频本地保存的地址
        public String qqUrl;//QQ视频http下载地址
        public String qqSavePath;//QQ微信视频本地保存的地址
        public String weixinWorkUrl;
        public String weixinWorkSavePath;
        public String dingdingUrl;
        public String dingdingSavePath;
    }

    public interface VideoResDownloadListener {
        void onSuccess(String url);

        void onFailed(String msg);

        String getVideoType();
    }
}
