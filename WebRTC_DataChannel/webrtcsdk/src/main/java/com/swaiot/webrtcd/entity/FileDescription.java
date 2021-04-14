package com.swaiot.webrtcd.entity;

import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;
import com.swaiot.webrtcd.util.AppUtils;

import java.io.File;

public class FileDescription {
    private int dataType;
    private String mimeType;
    private String fileName;
    private String md5;
    private long fileSize;


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
            dir.mkdir();
        }
        return new File(dir.getAbsolutePath(), fileName);
    }


    public boolean checkHasFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dir.getAbsolutePath(), fileName);

        return AppUtils.checkFileMd5(file, md5);//true 已经有此文件，无须下载 ,false 需要发送
    }
}
