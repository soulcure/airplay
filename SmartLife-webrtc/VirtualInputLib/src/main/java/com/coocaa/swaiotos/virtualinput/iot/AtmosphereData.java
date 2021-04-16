package com.coocaa.swaiotos.virtualinput.iot;

public class AtmosphereData {

    private String playCmd;

    private String mediaTitle;
    private String type;

    public AtmosphereData(String playCmd, String mediaTitle, String type) {
        this.playCmd = playCmd;
        this.mediaTitle = mediaTitle;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
