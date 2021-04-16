package com.coocaa.smartscreen.data.voice;

public class VoiceDescription {

    private String description;

    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "VoiceDescription{" +
                "description='" + description + '\'' +
                '}';
    }
}
