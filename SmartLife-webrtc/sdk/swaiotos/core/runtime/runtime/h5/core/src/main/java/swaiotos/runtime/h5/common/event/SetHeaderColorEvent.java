package swaiotos.runtime.h5.common.event;

import swaiotos.runtime.h5.common.base.BaseEvent;

public class SetHeaderColorEvent extends BaseEvent {

    public SetHeaderColorEvent(String color) {
        super(color);
    }

    private String id;
    public SetHeaderColorEvent setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }
}
