package com.coocaa.swaiotos.virtualinput.iot;

public class VideoStateData {


    private String cachePercents;

    private String mediaCurrent;

    private String playCmd;

    private String mediaTitle;

    private String mediaTime;

    private String currentVolume;
    private String mobile;
    private String type;
    private String uri;

    public VideoStateData(String cachePercents, String mediaCurrent, String playCmd, String mediaTitle, String mediaTime, String currentVolume, String mobile, String type, String uri) {
        this.cachePercents = cachePercents;
        this.mediaCurrent = mediaCurrent;
        this.playCmd = playCmd;
        this.mediaTitle = mediaTitle;
        this.mediaTime = mediaTime;
        this.currentVolume = currentVolume;
        this.mobile = mobile;
        this.type = type;
        this.uri = uri;
    }

    public String getCachePercents() {
        return cachePercents;
    }

    public void setCachePercents(String cachePercents) {
        this.cachePercents = cachePercents;
    }

    public String getMediaCurrent() {
        return mediaCurrent;
    }

    public void setMediaCurrent(String mediaCurrent) {
        this.mediaCurrent = mediaCurrent;
    }

    public String getPlayCmd() {
        return playCmd;
    }

    public void setPlayCmd(String playCmd) {
        this.playCmd = playCmd;
    }

    public String getMediaTitle() {
        return mediaTitle;
    }

    public void setMediaTitle(String mediaTitle) {
        this.mediaTitle = mediaTitle;
    }

    public String getMediaTime() {
        return mediaTime;
    }

    public void setMediaTime(String mediaTime) {
        this.mediaTime = mediaTime;
    }

    public String getCurrentVolume() {
        return currentVolume;
    }

    public void setCurrentVolume(String currentVolume) {
        this.currentVolume = currentVolume;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
