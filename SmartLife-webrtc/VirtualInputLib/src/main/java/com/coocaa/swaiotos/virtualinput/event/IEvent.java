package com.coocaa.swaiotos.virtualinput.event;

import java.util.Map;

/**
 * @Author: yuzhan
 */
public interface IEvent {
    void onClick(String appletId, String appletName, String btnName);

    void onEvent(String eventId, Map<String, String> params);
}
