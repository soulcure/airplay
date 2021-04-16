package swaiotos.runtime.h5.common.event;

public class OnUISafeDistanceCBData {
    public String event;
    public int top;
    public int bottom;
    public int versionCode;

    public OnUISafeDistanceCBData(String event,int top,int bottom) {
        this.event = event;
        this.top = top;
        this.bottom = bottom;
        this.versionCode = 1;
    }
    public OnUISafeDistanceCBData(String event) {
        this.event = event;
        this.versionCode = 1;
    }

}

