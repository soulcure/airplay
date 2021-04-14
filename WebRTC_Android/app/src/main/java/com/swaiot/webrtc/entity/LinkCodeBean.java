package com.swaiot.webrtc.entity;

public class LinkCodeBean {
    private String linkCode;
    private String expiresIn;
    private String typeLoopTime;

    public String getLinkCode() {
        return linkCode;
    }

    public void setLinkCode(String linkCode) {
        this.linkCode = linkCode;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTypeLoopTime() {
        return typeLoopTime;
    }

    public void setTypeLoopTime(String typeLoopTime) {
        this.typeLoopTime = typeLoopTime;
    }
}
