package com.coocaa.tvpi.event;

/**
 * @ClassName StartPushEvent
 * @Description TODO (write something)
 * @User heni
 * @Date 2021/1/18
 */
public class StartPushEvent {

    public String pushPageType;

    public StartPushEvent(String pushPageType) {
        this.pushPageType = pushPageType;
    }

    @Override
    public String toString() {
        return "StartPushEvent{" +
                "pushPageType='" + pushPageType + '\'' +
                '}';
    }
}
