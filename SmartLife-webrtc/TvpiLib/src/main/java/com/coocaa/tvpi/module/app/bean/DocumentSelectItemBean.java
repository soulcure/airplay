package com.coocaa.tvpi.module.app.bean;

public class DocumentSelectItemBean {
    private String sourceName;
    private String source;
    private int resId;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public DocumentSelectItemBean(String sourceName, String source, int resId) {
        this.sourceName = sourceName;
        this.source = source;
        this.resId = resId;
    }
}
