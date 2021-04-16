package com.coocaa.swaiotos.virtualinput.event;

import java.util.Map;

/**
 * @Author: yuzhan
 */
public class GlobalEvent {

    private static IEvent event;

    public static void onClick(String appletId, String appletName, String btnName) {
        if(event != null)
            event.onClick(appletId, appletName, btnName);
    }

    public static void setEvent(IEvent iEvent) {
        if(iEvent != null) {
            event = iEvent;
        }
    }

    public static void onEvent(String eventId, Map<String, String> params) {
        if (event != null)
            event.onEvent(eventId, params);
    }
}
