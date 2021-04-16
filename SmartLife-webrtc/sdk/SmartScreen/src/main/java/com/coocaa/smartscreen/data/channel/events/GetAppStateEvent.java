package com.coocaa.smartscreen.data.channel.events;

public class GetAppStateEvent {
    public String result;

    public GetAppStateEvent(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "GetAppStateEvent{" +
                "result='" + result + '\'' +
                '}';
    }
}
