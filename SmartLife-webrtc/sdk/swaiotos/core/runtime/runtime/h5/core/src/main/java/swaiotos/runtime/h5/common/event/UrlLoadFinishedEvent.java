package swaiotos.runtime.h5.common.event;

import swaiotos.runtime.h5.common.base.BaseEvent;

public class UrlLoadFinishedEvent extends BaseEvent {
    public UrlLoadFinishedEvent(){
    }
    public UrlLoadFinishedEvent(String typeString, String id) {
        setTypeString(typeString);
        setEventId(id);
    }
}
