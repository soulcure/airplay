package com.coocaa.tvpi.module.local.document;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 2020/10/20
 */
public class FileDownloader {

    private final static String TAG = "FileDownloader";
    private OkHttpClient mOkHttpClient;
    private Call mCall;
    private String mFilePath = "";
    private boolean isCancel = false;

    public FileDownloader() {
        mOkHttpClient = new OkHttpClient();
    }

    public void download(final String url, final String fileName, final OnDownloadListener downloadListener) {
        isCancel = false;
        mFilePath = "";
        final Request request = new Request.Builder().url(url).build();
        mCall = mOkHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: " + e.getMessage());
                if (isCancel || mCall.isCanceled()) {
                    Log.i(TAG, "onFailure: task is cancel. -> " + e.getMessage());
                    return;
                }
                downloadListener.onDownloadFailed(url, error(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "onResponse: " + response.code());
                if (response.code() != 200) {
                    ResultEnum error = ResultEnum.ERROR_CUSTOM;
                    error.setCode(response.code());
                    error.setMsg(response.message());
                    downloadListener.onDownloadFailed(url, error);
                    return;
                }
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                String savePath = isExistDir(fileName);
                Log.w(TAG, "savePath：" + savePath);
                File file = null;
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    file = new File(savePath, fileName);
                    Log.w(TAG, "final path：" + file);
                    mFilePath = file.getAbsolutePath();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        if (isCancel || mCall.isCanceled()) {
                            Log.i(TAG, "onResponse: download task is cancel, break. --> " + url);
                            throw new Exception("task is cancel.");
                        }
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        // 下载中
                        downloadListener.onDownloading(progress);
                    }
                    fos.flush();
                    //下载完成
                    downloadListener.onDownloadSuccess(file.getPath());
                } catch (Exception e) {
                    delete();
                    if (isCancel || mCall.isCanceled()) {
                        Log.i(TAG, "onResponse Exception: task is cancel. -> " + e.getMessage());
                        return;
                    }
                    Log.i(TAG, "onResponse: Exception:" + e.getMessage());
                    downloadListener.onDownloadFailed(url, error(e));
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    public void cancel() {
        Log.i(TAG, "cancel: " + mFilePath);
        isCancel = true;
        delete();
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private void delete() {
        try {
            if (!TextUtils.isEmpty(mFilePath)) {
                File file = new File(mFilePath);
                if (file.exists()) {
                    Log.i(TAG, "delete: " + mFilePath);
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ResultEnum error(Throwable t) {
        ResultEnum error;
        if (t instanceof SocketTimeoutException) {
            error = ResultEnum.ERROR_TIMEOUT;
        } else if (t instanceof SocketException) {
            if (t instanceof ConnectException) {
                error = ResultEnum.ERROR_CONNECT;
            } else error = ResultEnum.ERROR_SOCKET;
        } else if (t instanceof RuntimeException) {
            if (t instanceof JSONException) {
                error = ResultEnum.ERROR_PARSE_JSON;
            } else error = ResultEnum.ERROR_RUNTIME;
        } else if (t instanceof UnknownHostException) {
            error = ResultEnum.ERROR_UNKNOWNHOST;
        } else {
            error = ResultEnum.ERROR_UNKNOW;
        }
        return error;
    }

    /**
     * @return
     * @throws IOException 判断下载目录是否存在
     */
    private String isExistDir(String fileName) throws IOException {
        // 下载位置
        String saveDir = isVideo(fileName) ? DocumentUtil.SAVE_DOC_VIDEO_DIR : DocumentUtil.SAVE_DOC_FILE_DIR;
        File downloadFile = new File(DocumentUtil.SAVE_DOC_PATH, saveDir);
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile();
        }
        return downloadFile.getAbsolutePath();
    }

    private final static String[] videoTypes = {"mp4", "avi", "mov", "rmvb", "flv", "3gp"};

    private boolean isVideo(String fileName) {
        for (String suffix : videoTypes) {
            if (fileName.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }


    public interface OnDownloadListener {
        /**
         * 下载成功
         */
        void onDownloadSuccess(String saveDir);

        /**
         * @param progress 下载进度
         */
        void onDownloading(int progress);

        /**
         * 下载失败
         */
        void onDownloadFailed(String url, ResultEnum result);
    }

}
