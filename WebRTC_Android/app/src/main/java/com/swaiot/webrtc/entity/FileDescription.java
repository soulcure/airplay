package com.swaiot.webrtc.entity;

import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;
import com.swaiot.webrtc.util.AppUtils;

import java.io.File;

public class FileDescription {
    private String msgId;
    private int dataType;
    private String mimeType;
    private String fileName;
    private String md5;
    private long fileSize;
    private String sendFilePath;
    private String receiveFilePath;

    public boolean isStart() {
        return dataType == 1;
    }

    public boolean isEnd() {
        return dataType == 2;
    }

    public void setCheckFile() {
        dataType = -1;
    }

    public boolean isCheckFile() {
        return dataType == -1;
    }

    public void setNoFile() {
        dataType = -2;
    }


    public void setHasFile() {
        dataType = -3;
    }


    public void setStart() {
        dataType = 1;
    }


    public void setEnd() {
        dataType = 2;
    }

    public boolean needSendFile() {
        return dataType == -2;
    }

    public boolean noNeedSendFile() {
        return dataType == -3;
    }


    public void setFile(File file) {
        mimeType = getMimeType(file.getPath());
        fileName = file.getName();
        md5 = AppUtils.md5(file);
        fileSize = file.length();
    }


    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getSendFilePath() {
        return sendFilePath;
    }

    public void setSendFilePath(String sendFilePath) {
        this.sendFilePath = sendFilePath;
    }

    public String getReceiveFilePath() {
        return receiveFilePath;
    }

    public void setReceiveFilePath(String receiveFilePath) {
        this.receiveFilePath = receiveFilePath;
    }


    // url = file path or whatever suitable URL you want.
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }


    public String toJson() {
        return new Gson().toJson(this);
    }


    public String getMimeType() {
        return mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMd5() {
        return md5;
    }

    public long getFileSize() {
        return fileSize;
    }


    public File getDownLoadFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir.getAbsolutePath(), fileName);
    }


    public boolean checkHasFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir.getAbsolutePath(), fileName);
        receiveFilePath = file.getPath();
        if (file.exists()) {
            return AppUtils.checkFileMd5(file, md5);//true 已经有此文件，无须下载 ,false 需要发送
        }
        return false;
    }
}
