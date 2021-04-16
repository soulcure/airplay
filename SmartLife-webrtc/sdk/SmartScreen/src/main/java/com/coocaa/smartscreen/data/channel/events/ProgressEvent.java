package com.coocaa.smartscreen.data.channel.events;

import android.os.Parcelable;

import java.io.Serializable;

import swaiotos.channel.iot.ss.channel.im.IMMessage;

/**
 * @author chenaojun
 */
public class ProgressEvent implements Serializable {
    private IMMessage.TYPE type;
    public boolean result;
    public int progress;
    public String info;

    public ProgressEvent() {
    }

    public boolean isResultSuccess() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public IMMessage.TYPE getType() {
        return type;
    }

    public void setType(IMMessage.TYPE type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ProgressEvent{" +
                "type=" + type +
                ", result=" + result +
                ", progress=" + progress +
                ", info='" + info + '\'' +
                '}';
    }
}
