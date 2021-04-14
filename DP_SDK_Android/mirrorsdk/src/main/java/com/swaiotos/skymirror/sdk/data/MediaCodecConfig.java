package com.swaiotos.skymirror.sdk.data;

public class MediaCodecConfig {
    //硬件编解码器信息
    private boolean isSupport;
    private int maxBitrate;
    private int maxWidth;
    private int maxHeight;


    public boolean isSupport() {
        return isSupport;
    }

    public void setSupport(boolean support) {
        isSupport = support;
    }

    public int getMaxBitrate() {
        return maxBitrate;
    }

    public void setMaxBitrate(int maxBitrate) {
        this.maxBitrate = maxBitrate;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }


    @Override
    public String toString() {
        return "MediaCodecConfig{" +
                "isSupport=" + isSupport +
                ", maxBitrate=" + maxBitrate +
                ", maxWidth=" + maxWidth +
                ", maxHeight=" + maxHeight +
                '}';
    }
}
