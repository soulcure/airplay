package com.coocaa.smartscreen.data.device;


import java.io.Serializable;

public class BindCodeMsg implements Serializable {
    private String bindCode;
    private String expiresIn;
    private String typeLoopTime;
    private String url;

    public String getBindCode() {
        return bindCode;
    }

    public void setBindCode(String bindCode) {
        this.bindCode = bindCode;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "BindCodeMsg{" +
                "bindCode='" + bindCode + '\'' +
                ", expiresIn='" + expiresIn + '\'' +
                ", typeLoopTime='" + typeLoopTime + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
