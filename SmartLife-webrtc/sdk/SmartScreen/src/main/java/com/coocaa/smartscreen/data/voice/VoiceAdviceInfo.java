package com.coocaa.smartscreen.data.voice;

import com.google.gson.Gson;

public class VoiceAdviceInfo {

    private String error;
    private String value;
    private String key;


    public void setError(String error) {
        this.error = error;
    }
    public String getError() {
        return error;
    }


    public void setValue(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return "VoiceAdviceInfo{" +
                "error='" + error + '\'' +
                ", value='" + value + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
